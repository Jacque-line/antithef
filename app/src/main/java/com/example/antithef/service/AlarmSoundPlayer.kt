package com.example.antithef.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AlarmSoundPlayer(private val context: Context) {

    private var audioTrack: AudioTrack? = null
    private var isPlaying: Boolean = false
    private var alarmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Starts siren alarm sound and vibration.
     * @param volumeLevel 0 to 100
     */
    @Synchronized
    fun startAlarm(volumeLevel: Int = 80) {
        if (isPlaying) return
        isPlaying = true

        startVibration()

        alarmJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(sampleRate)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            val gain = (volumeLevel.coerceIn(0, 100) / 100f).coerceIn(0f, 1f)
            track.setVolume(gain)
            track.play()
            audioTrack = track

            val buffer = ShortArray(1024)
            var phase = 0.0
            var time = 0.0

            while (isActive && isPlaying) {
                for (i in buffer.indices) {
                    // Siren modulation: sweeps between 700 Hz and 1600 Hz over a 1.2 second period
                    val sweepProgress = sin(2.0 * PI * 0.8 * time)
                    val currentFreq = 1150.0 + 450.0 * sweepProgress

                    val angularFreq = 2.0 * PI * currentFreq / sampleRate
                    phase += angularFreq
                    if (phase > 2.0 * PI) phase -= 2.0 * PI

                    // Square-ish / rich harmonic wave for piercing alarm sound
                    val sine = sin(phase)
                    val sample = (sine * 0.7 + sin(3 * phase) * 0.3) * Short.MAX_VALUE * 0.8
                    buffer[i] = sample.toInt().toShort()

                    time += 1.0 / sampleRate
                }
                track.write(buffer, 0, buffer.size)
            }

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    @Synchronized
    fun stopAlarm() {
        isPlaying = false
        alarmJob?.cancel()
        alarmJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        stopVibration()
    }

    fun playTestTone(volumeLevel: Int = 80, onComplete: (() -> Unit)? = null) {
        scope.launch {
            startAlarm(volumeLevel)
            kotlinx.coroutines.delay(2000)
            stopAlarm()
            onComplete?.invoke()
        }
    }

    private fun startVibration() {
        vibrator?.let { v ->
            if (!v.hasVibrator()) return
            val pattern = longArrayOf(0, 400, 200, 400, 200)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0)
                v.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(pattern, 0)
            }
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
    }
}
