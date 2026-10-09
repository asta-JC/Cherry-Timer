package com.asta.cherrytimer.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.asta.cherrytimer.util.CherryAudioUtil
import com.asta.cherrytimer.util.CherryHapticsUtil
import java.util.Calendar

@Serializable
enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK
}

@Serializable
data class PomodoroSessionRecord(
    val id: Long = System.currentTimeMillis(),
    val taskName: String,
    val durationMinutes: Int,
    val mode: TimerMode,
    val timestamp: Long = System.currentTimeMillis()
)

data class PomodoroUiState(
    val currentMode: TimerMode = TimerMode.FOCUS,
    val timeLeftSeconds: Int = 25 * 60,
    val totalTimeSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val sessionCount: Int = 1, // 1 to 4
    val taskName: String = "",
    val autoSwitchModes: Boolean = false,
    val soundEnabled: Boolean = true,
    val focusDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val history: List<PomodoroSessionRecord> = emptyList(),
    val sessionsCompletedToday: Int = 0,
    val totalFocusMinutesToday: Int = 0,
    val streak: Int = 0
)

class PomodoroTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("cherry_timer_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (e: Exception) {
            // Ignore if audio not available
        }
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val focus = prefs.getInt("pref_focus_dur", 25)
        val shortBreak = prefs.getInt("pref_short_dur", 5)
        val longBreak = prefs.getInt("pref_long_dur", 15)
        val autoSwitch = prefs.getBoolean("pref_auto_switch", false)
        val sound = prefs.getBoolean("pref_sound", true)
        val taskName = prefs.getString("pref_task_name", "") ?: ""
        val sessionCount = prefs.getInt("pref_session_count", 1)
        val modeStr = prefs.getString("pref_current_mode", TimerMode.FOCUS.name) ?: TimerMode.FOCUS.name
        val currentMode = try { TimerMode.valueOf(modeStr) } catch (e: Exception) { TimerMode.FOCUS }
        val historyJson = prefs.getString("pref_history", "[]") ?: "[]"

        val historyList = try {
            json.decodeFromString<List<PomodoroSessionRecord>>(historyJson)
        } catch (e: Exception) {
            emptyList()
        }

        val defaultDurationSec = when (currentMode) {
            TimerMode.FOCUS -> focus * 60
            TimerMode.SHORT_BREAK -> shortBreak * 60
            TimerMode.LONG_BREAK -> longBreak * 60
        }

        val isRunning = prefs.getBoolean("pref_is_running", false)
        val targetEndTime = prefs.getLong("pref_target_end_time", 0L)
        val savedTimeLeft = prefs.getInt("pref_time_left_seconds", defaultDurationSec)
        val savedTotalTime = prefs.getInt("pref_total_time_seconds", defaultDurationSec)

        var finalTimeLeft = savedTimeLeft
        var finalTotalTime = savedTotalTime
        var finalRunning = isRunning

        if (isRunning && targetEndTime > 0L) {
            val now = System.currentTimeMillis()
            val remainingMillis = targetEndTime - now
            val remainingSeconds = ((remainingMillis + 999L) / 1000L).toInt()

            if (remainingSeconds <= 0) {
                finalTimeLeft = 0
                finalRunning = false
                handleBackgroundCompletion(currentMode, focus, taskName, autoSwitch)
            } else {
                finalTimeLeft = remainingSeconds
                finalTotalTime = savedTotalTime
                finalRunning = true
            }
        }

        _uiState.update {
            val updated = it.copy(
                focusDurationMinutes = focus,
                shortBreakDurationMinutes = shortBreak,
                longBreakDurationMinutes = longBreak,
                autoSwitchModes = autoSwitch,
                soundEnabled = sound,
                history = historyList,
                currentMode = currentMode,
                sessionCount = sessionCount,
                taskName = taskName,
                timeLeftSeconds = finalTimeLeft,
                totalTimeSeconds = finalTotalTime,
                isRunning = finalRunning
            )
            calculateStats(updated)
        }

        if (finalRunning) {
            startTimerJob(targetEndTime)
        }
    }

    private fun handleBackgroundCompletion(mode: TimerMode, focusDuration: Int, task: String, autoSwitch: Boolean) {
        if (mode == TimerMode.FOCUS) {
            val record = PomodoroSessionRecord(
                taskName = task.ifBlank { "Focus Session" },
                durationMinutes = focusDuration,
                mode = TimerMode.FOCUS
            )
            _uiState.update {
                val updated = it.copy(history = listOf(record) + it.history)
                calculateStats(updated)
            }
        }
        advanceToNextModeInternal()
        if (autoSwitch) {
            startTimer()
        }
    }

    private fun advanceToNextModeInternal() {
        val state = _uiState.value
        when (state.currentMode) {
            TimerMode.FOCUS -> {
                if (state.sessionCount >= 4) {
                    switchModeInternal(TimerMode.LONG_BREAK)
                    _uiState.update { it.copy(sessionCount = 1) }
                } else {
                    switchModeInternal(TimerMode.SHORT_BREAK)
                }
            }
            TimerMode.SHORT_BREAK -> {
                switchModeInternal(TimerMode.FOCUS)
                _uiState.update { it.copy(sessionCount = (state.sessionCount + 1).coerceAtMost(4)) }
            }
            TimerMode.LONG_BREAK -> {
                switchModeInternal(TimerMode.FOCUS)
                _uiState.update { it.copy(sessionCount = 1) }
            }
        }
    }

    private fun switchModeInternal(mode: TimerMode) {
        val durationMinutes = when (mode) {
            TimerMode.FOCUS -> _uiState.value.focusDurationMinutes
            TimerMode.SHORT_BREAK -> _uiState.value.shortBreakDurationMinutes
            TimerMode.LONG_BREAK -> _uiState.value.longBreakDurationMinutes
        }
        val durationSeconds = durationMinutes * 60
        _uiState.update {
            it.copy(
                currentMode = mode,
                timeLeftSeconds = durationSeconds,
                totalTimeSeconds = durationSeconds,
                isRunning = false
            )
        }
        saveToPrefs()
    }

    private fun saveToPrefs() {
        val state = _uiState.value
        try {
            val historyString = json.encodeToString(state.history)
            val targetEndTime = if (state.isRunning) {
                System.currentTimeMillis() + (state.timeLeftSeconds * 1000L)
            } else 0L

            prefs.edit()
                .putInt("pref_focus_dur", state.focusDurationMinutes)
                .putInt("pref_short_dur", state.shortBreakDurationMinutes)
                .putInt("pref_long_dur", state.longBreakDurationMinutes)
                .putBoolean("pref_auto_switch", state.autoSwitchModes)
                .putBoolean("pref_sound", state.soundEnabled)
                .putString("pref_history", historyString)
                .putString("pref_task_name", state.taskName)
                .putInt("pref_session_count", state.sessionCount)
                .putString("pref_current_mode", state.currentMode.name)
                .putBoolean("pref_is_running", state.isRunning)
                .putLong("pref_target_end_time", targetEndTime)
                .putInt("pref_time_left_seconds", state.timeLeftSeconds)
                .putInt("pref_total_time_seconds", state.totalTimeSeconds)
                .putLong("pref_last_saved_time", System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun calculateStats(state: PomodoroUiState): PomodoroUiState {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        val todaySessions = state.history.filter { record ->
            val recordCal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            recordCal.get(Calendar.YEAR) == todayYear && recordCal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
        }

        val sessionsCompletedToday = todaySessions.size
        val totalFocusMinutesToday = todaySessions.sumOf { it.durationMinutes }
        val streak = calculateStreak(state.history)

        return state.copy(
            sessionsCompletedToday = sessionsCompletedToday,
            totalFocusMinutesToday = totalFocusMinutesToday,
            streak = streak
        )
    }

    private fun calculateStreak(history: List<PomodoroSessionRecord>): Int {
        if (history.isEmpty()) return 0

        val distinctDays = history.map { record ->
            Calendar.getInstance().apply {
                timeInMillis = record.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSet().sortedDescending()

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var expectedDayTime = todayCalendar.timeInMillis
        var streak = 0

        if (!distinctDays.contains(expectedDayTime)) {
            val yesterdayCalendar = Calendar.getInstance().apply {
                timeInMillis = todayCalendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -1)
            }
            expectedDayTime = yesterdayCalendar.timeInMillis
            if (!distinctDays.contains(expectedDayTime)) {
                return 0
            }
        }

        for (dayTime in distinctDays) {
            if (dayTime == expectedDayTime) {
                streak++
                val cal = Calendar.getInstance().apply { timeInMillis = expectedDayTime }
                cal.add(Calendar.DAY_OF_YEAR, -1)
                expectedDayTime = cal.timeInMillis
            } else if (dayTime < expectedDayTime) {
                break
            }
        }

        return streak.coerceAtLeast(if (distinctDays.isNotEmpty()) 1 else 0)
    }

    fun setTaskName(name: String) {
        _uiState.update { it.copy(taskName = name) }
        saveToPrefs()
    }

    fun switchMode(mode: TimerMode) {
        pauseTimer()
        val durationMinutes = when (mode) {
            TimerMode.FOCUS -> _uiState.value.focusDurationMinutes
            TimerMode.SHORT_BREAK -> _uiState.value.shortBreakDurationMinutes
            TimerMode.LONG_BREAK -> _uiState.value.longBreakDurationMinutes
        }
        val durationSeconds = durationMinutes * 60
        _uiState.update {
            it.copy(
                currentMode = mode,
                timeLeftSeconds = durationSeconds,
                totalTimeSeconds = durationSeconds
            )
        }
        saveToPrefs()
    }

    fun setDurations(focus: Int, shortBreak: Int, longBreak: Int) {
        _uiState.update { state ->
            val newFocus = focus.coerceIn(1, 120)
            val newShort = shortBreak.coerceIn(1, 30)
            val newLong = longBreak.coerceIn(1, 60)

            val currentDuration = when (state.currentMode) {
                TimerMode.FOCUS -> newFocus * 60
                TimerMode.SHORT_BREAK -> newShort * 60
                TimerMode.LONG_BREAK -> newLong * 60
            }
            val newTimeLeft = if (!state.isRunning) currentDuration else state.timeLeftSeconds
            val newTotal = if (!state.isRunning) currentDuration else state.totalTimeSeconds

            val updated = state.copy(
                focusDurationMinutes = newFocus,
                shortBreakDurationMinutes = newShort,
                longBreakDurationMinutes = newLong,
                timeLeftSeconds = newTimeLeft,
                totalTimeSeconds = newTotal
            )
            calculateStats(updated)
        }
        saveToPrefs()
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update {
            val updated = it.copy(soundEnabled = enabled)
            calculateStats(updated)
        }
        saveToPrefs()
    }

    fun toggleAutoSwitch(enabled: Boolean) {
        _uiState.update {
            val updated = it.copy(autoSwitchModes = enabled)
            calculateStats(updated)
        }
        saveToPrefs()
    }

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        val targetEndTime = System.currentTimeMillis() + (_uiState.value.timeLeftSeconds * 1000L)
        _uiState.update { it.copy(isRunning = true) }
        saveToPrefs()
        startTimerJob(targetEndTime)
    }

    private fun startTimerJob(targetEndTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                val now = System.currentTimeMillis()
                val remainingMillis = targetEndTime - now
                val remainingSeconds = ((remainingMillis + 999L) / 1000L).toInt()

                if (remainingSeconds <= 0) {
                    _uiState.update { it.copy(timeLeftSeconds = 0, isRunning = false) }
                    saveToPrefs()
                    onTimerFinished()
                    break
                } else {
                    _uiState.update { it.copy(timeLeftSeconds = remainingSeconds) }
                    saveToPrefs()
                }
                delay(100L)
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
        saveToPrefs()
    }

    fun resetTimer() {
        pauseTimer()
        val state = _uiState.value
        val durationMinutes = when (state.currentMode) {
            TimerMode.FOCUS -> state.focusDurationMinutes
            TimerMode.SHORT_BREAK -> state.shortBreakDurationMinutes
            TimerMode.LONG_BREAK -> state.longBreakDurationMinutes
        }
        val durationSeconds = durationMinutes * 60
        _uiState.update {
            it.copy(
                timeLeftSeconds = durationSeconds,
                totalTimeSeconds = durationSeconds
            )
        }
        saveToPrefs()
    }

    fun skipTimer() {
        pauseTimer()
        advanceToNextMode()
        saveToPrefs()
    }

    private fun onTimerFinished() {
        val state = _uiState.value
        if (state.soundEnabled) {
            CherryAudioUtil.playChime(getApplication())
        }
        CherryHapticsUtil.triggerSessionComplete(getApplication())

        if (state.currentMode == TimerMode.FOCUS) {
            val record = PomodoroSessionRecord(
                taskName = state.taskName.ifBlank { "Focus Session" },
                durationMinutes = state.focusDurationMinutes,
                mode = TimerMode.FOCUS
            )
            _uiState.update {
                val updated = it.copy(history = listOf(record) + it.history)
                calculateStats(updated)
            }
            saveToPrefs()
        }

        advanceToNextMode()

        if (_uiState.value.autoSwitchModes) {
            startTimer()
        }
    }

    private fun advanceToNextMode() {
        val state = _uiState.value
        when (state.currentMode) {
            TimerMode.FOCUS -> {
                if (state.sessionCount >= 4) {
                    switchMode(TimerMode.LONG_BREAK)
                    _uiState.update { it.copy(sessionCount = 1) }
                } else {
                    switchMode(TimerMode.SHORT_BREAK)
                }
            }
            TimerMode.SHORT_BREAK -> {
                switchMode(TimerMode.FOCUS)
                _uiState.update { it.copy(sessionCount = (state.sessionCount + 1).coerceAtMost(4)) }
            }
            TimerMode.LONG_BREAK -> {
                switchMode(TimerMode.FOCUS)
                _uiState.update { it.copy(sessionCount = 1) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            toneGenerator?.release()
        } catch (e: Exception) {}
    }
}
