package com.asta.cherrytimer.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import android.media.ToneGenerator

object CherryAudioUtil {
    fun playChime(context: Context) {
        try {
            val sampleRate = 44100
            val numSamples = (sampleRate * 0.5).toInt() // 500ms
            val generatedSnd = ByteArray(numSamples * 2)

            // Pleasant chime chord frequencies (C5 = 523.25, E5 = 659.25, G5 = 783.99)
            val freqs = doubleArrayOf(523.25, 659.25, 783.99)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var valSamp = 0.0
                val envelope = Math.exp(-t * 5.0)
                for (f in freqs) {
                    valSamp += Math.sin(2.0 * Math.PI * f * t) * envelope
                }
                valSamp /= freqs.size
                val sampleVal = (valSamp * 32767.0).toInt().toShort()
                generatedSnd[2 * i] = (sampleVal.toInt() and 0x00ff).toByte()
                generatedSnd[2 * i + 1] = ((sampleVal.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(generatedSnd.size)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
        } catch (e: Exception) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }
}
