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
 * individual exponential decay envelopes).
 *
 * All 12 [AudioTrack]s are pre-built once in the background at construction time:
 * samples are synthesized, written into MODE_STATIC tracks, and kept alive.
 * Each [play] call then only does stop → reloadStaticData → play, which is
 * effectively instantaneous (no allocation, no data copy on the hot path).
 *
 * Call [release] (e.g. from a DisposableEffect) when the player is no longer needed.
 *
 * The [scope] is used only for the background prewarm; it may be cancelled after
 * prewarm completes without affecting playback.
 */
class PianoSoundPlayer(private val scope: CoroutineScope) {

    private val tracks = arrayOfNulls<AudioTrack>(12)

    // Volatile flag for safe publication: once true, all tracks[] entries are visible.
    @Volatile private var ready = false
    @Volatile private var released = false

    init {
        scope.launch(Dispatchers.IO) {
            for (i in 0..11) {
                tracks[i] = buildTrack(synthesize(FREQUENCIES[i]))
            }
            ready = true
        }
    }

    fun play(noteIndex: Int) {
        if (released) return
        val idx = noteIndex.coerceIn(0, 11)
        scope.launch(Dispatchers.IO) {
            if (released) return@launch
            val track = if (ready) tracks[idx] else null
            if (track != null) {
                // Hot path: just rewind and play — no allocation, no data copy.
                try {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
                    if (track.reloadStaticData() == AudioTrack.SUCCESS) {
                        track.play()
                    } else {
                        // Rare: reload failed; rebuild this track in place.
                        track.release()
                        tracks[idx] = buildTrack(synthesize(FREQUENCIES[idx]))
                        tracks[idx]?.play()
                    }
                } catch (_: Exception) {}
            } else {
                // Cold path: prewarm not finished yet; synthesize + play a one-shot track.
                try {
                    val t = buildTrack(synthesize(FREQUENCIES[idx]))
                    t.play()
                    Thread.sleep((DURATION_SECONDS * 1000 + 50).toLong())
                    t.stop()
                    t.release()
                } catch (_: Exception) {}
            }
        }
    }

    /** Stop all playing notes and release audio resources. Safe to call from any thread. */
    fun release() {
        released = true
        scope.launch(Dispatchers.IO) {
            for (idx in 0..11) {
                try {
                    val t = tracks[idx] ?: continue
                    tracks[idx] = null
                    if (t.playState == AudioTrack.PLAYSTATE_PLAYING) t.stop()
                    t.release()
                } catch (_: Exception) {}
            }
        }
    }

    private fun buildTrack(samples: ShortArray): AudioTrack {
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
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        return track
    }

    private fun synthesize(freq: Float): ShortArray {
        val numSamples = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sum = 0.0
            for (h in HARMONICS) {
                sum += h.amplitude * exp(-h.decay * t) * sin(2.0 * PI * freq * h.multiple * t)
            }
            samples[i] = (sum / TOTAL_AMPLITUDE * Short.MAX_VALUE * 0.85)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return samples
    }

    private data class Harmonic(val multiple: Float, val amplitude: Float, val decay: Float)

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val DURATION_SECONDS = 0.8f

        private val FREQUENCIES = floatArrayOf(
            261.63f, 277.18f, 293.66f, 311.13f,
            329.63f, 349.23f, 369.99f, 392.00f,
            415.30f, 440.00f, 466.16f, 493.88f,
        )

        private val HARMONICS = listOf(
            Harmonic(1f, 1.00f, 3.0f),
            Harmonic(2f, 0.50f, 5.0f),
            Harmonic(3f, 0.25f, 7.0f),
            Harmonic(4f, 0.12f, 10.0f),
        )

        private val TOTAL_AMPLITUDE = HARMONICS.sumOf { it.amplitude.toDouble() }
    }
}
