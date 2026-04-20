package com.example.cof.ui.screen

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesizes piano-like tones using additive synthesis (fundamental + harmonics with
 * individual exponential decay envelopes). Each [play] call is non-blocking and
 * dispatched to [Dispatchers.IO].
 *
 * The [scope] is expected to be a composition-scoped coroutine scope so that in-flight
 * notes are cancelled automatically when the composable leaves the composition.
 */
class PianoSoundPlayer(private val scope: CoroutineScope) {

    fun play(noteIndex: Int) {
        val freq = FREQUENCIES[noteIndex.coerceIn(0, 11)]
        scope.launch(Dispatchers.IO) {
            synthesizeAndPlay(freq)
        }
    }

    private fun synthesizeAndPlay(freq: Float) {
        val numSamples = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0
            for (h in HARMONICS) {
                sum += h.amplitude * exp(-h.decay * t) * sin(2.0 * PI * freq * h.multiple * t)
            }
            // Normalize by total peak amplitude and scale to 16-bit range
            samples[i] = (sum / TOTAL_AMPLITUDE * Short.MAX_VALUE * 0.85)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(numSamples * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, numSamples)
        track.play()
        Thread.sleep((DURATION_SECONDS * 1000 + 50).toLong())
        track.stop()
        track.release()
    }

    private data class Harmonic(val multiple: Float, val amplitude: Float, val decay: Float)

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val DURATION_SECONDS = 0.8f

        // C4 through B4 frequencies
        private val FREQUENCIES = floatArrayOf(
            261.63f, 277.18f, 293.66f, 311.13f,
            329.63f, 349.23f, 369.99f, 392.00f,
            415.30f, 440.00f, 466.16f, 493.88f,
        )

        // Fundamental + 3 overtones; higher harmonics decay faster to mimic piano string behaviour
        private val HARMONICS = listOf(
            Harmonic(1f, 1.00f, 3.0f),
            Harmonic(2f, 0.50f, 5.0f),
            Harmonic(3f, 0.25f, 7.0f),
            Harmonic(4f, 0.12f, 10.0f),
        )

        private val TOTAL_AMPLITUDE = HARMONICS.sumOf { it.amplitude.toDouble() }
    }
}
