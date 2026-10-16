package com.asta.cherrytimer

import com.asta.cherrytimer.viewmodel.PomodoroSessionRecord
import com.asta.cherrytimer.viewmodel.PomodoroUiState
import com.asta.cherrytimer.viewmodel.TimerMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class PomodoroTimerUnitTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testTimerModes() {
        assertEquals(3, TimerMode.entries.size)
        assertTrue(TimerMode.entries.contains(TimerMode.FOCUS))
        assertTrue(TimerMode.entries.contains(TimerMode.SHORT_BREAK))
        assertTrue(TimerMode.entries.contains(TimerMode.LONG_BREAK))
    }

    @Test
    fun testDefaultUiState() {
        val state = PomodoroUiState()
        assertEquals(TimerMode.FOCUS, state.currentMode)
        assertEquals(25 * 60, state.timeLeftSeconds)
        assertEquals(25 * 60, state.totalTimeSeconds)
        assertFalse(state.isRunning)
        assertEquals(1, state.sessionCount)
        assertEquals(25, state.focusDurationMinutes)
        assertEquals(5, state.shortBreakDurationMinutes)
        assertEquals(15, state.longBreakDurationMinutes)
        assertTrue(state.soundEnabled)
        assertFalse(state.autoSwitchModes)
    }

    @Test
    fun testSessionRecordSerialization() {
        val record = PomodoroSessionRecord(
            taskName = "Writing Tests",
            durationMinutes = 25,
            mode = TimerMode.FOCUS,
            timestamp = 1700000000000L
        )

        val encoded = json.encodeToString(record)
        assertTrue(encoded.contains("Writing Tests"))
        assertTrue(encoded.contains("FOCUS"))

        val decoded = json.decodeFromString<PomodoroSessionRecord>(encoded)
        assertEquals(record.taskName, decoded.taskName)
        assertEquals(record.durationMinutes, decoded.durationMinutes)
        assertEquals(record.mode, decoded.mode)
        assertEquals(record.timestamp, decoded.timestamp)
    }

    @Test
    fun testTimeFormatting() {
        fun formatTime(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        assertEquals("25:00", formatTime(25 * 60))
        assertEquals("04:59", formatTime(4 * 60 + 59))
        assertEquals("00:00", formatTime(0))
        assertEquals("60:00", formatTime(3600))
    }

    @Test
    fun testProgressCalculation() {
        val state = PomodoroUiState(timeLeftSeconds = 12 * 60, totalTimeSeconds = 25 * 60)
        val progress = state.timeLeftSeconds.toFloat() / state.totalTimeSeconds.toFloat()
        assertEquals(0.48f, progress, 0.001f)
    }
}
