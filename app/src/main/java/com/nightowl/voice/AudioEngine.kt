package com.nightowl.voice

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.min

class AudioEngine {

    private val sampleRate = 44100
    private val minBufSize = AudioRecord.getMinBufferSize(
        sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )

    private var recorder: AudioRecord? = null
    private var track: AudioTrack? = null
    private var engineThread: Thread? = null
    @Volatile private var running = false

    @Volatile var currentPreset: VoicePreset = VoiceLibrary.presets[0]

    private var echoBuffer: ShortArray = ShortArray(0)
    private var echoWritePos = 0
    private var robotPhase = 0.0
    private var reverbBuffer: ShortArray = ShortArray(0)
    private var reverbWritePos = 0

    @SuppressLint("MissingPermission")
    fun start() {
        if (running) return
        running = true

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufSize * 2
        )

        track = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        echoBuffer = ShortArray(sampleRate)
        reverbBuffer = ShortArray(sampleRate / 2)

        recorder?.startRecording()
        track?.play()

        engineThread = thread(start = true) { loop() }
    }

    fun stop() {
        running = false
        engineThread?.join(300)
        engineThread = null
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        try {
            track?.stop()
            track?.release()
        } catch (_: Exception) {}
        recorder = null
        track = null
    }

    fun isRunning() = running

    private fun loop() {
        val buf = ShortArray(minBufSize)
        while (running) {
            val rec = recorder ?: break
            val read = rec.read(buf, 0, buf.size)
            if (read <= 0) continue
            val processed = applyEffect(buf, read, currentPreset)
            track?.write(processed, 0, read)
        }
    }

    private fun applyEffect(input: ShortArray, len: Int, preset: VoicePreset): ShortArray {
        return when (preset.type) {
            EffectType.NORMAL -> input
            EffectType.PITCH -> pitchShift(input, len, preset.p1)
            EffectType.ROBOT -> ringMod(input, len, preset.p1)
            EffectType.ECHO -> echo(input, len, preset.p1, preset.p2)
            EffectType.RADIO -> bandLimit(input, len, preset.p1)
            EffectType.DISTORT -> distort(input, len, preset.p1)
            EffectType.REVERB -> reverb(input, len, preset.p1)
            EffectType.WHISPER -> whisper(input, len, preset.p1)
        }
    }

    private fun pitchShift(input: ShortArray, len: Int, rate: Float): ShortArray {
        val out = ShortArray(len)
        for (i in 0 until len) {
            val srcIndex = (i * rate).toInt()
            out[i] = if (srcIndex in 0 until len) input[srcIndex] else 0
        }
        return out
    }

    private fun ringMod(input: ShortArray, len: Int, carrierHz: Float): ShortArray {
        val out = ShortArray(len)
        val increment = 2.0 * PI * carrierHz / sampleRate
        for (i in 0 until len) {
            val mod = sin(robotPhase)
            robotPhase += increment
            if (robotPhase > 2 * PI) robotPhase -= 2 * PI
            out[i] = (input[i] * mod).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    private fun echo(input: ShortArray, len: Int, delayMs: Float, decay: Float): ShortArray {
        val delaySamples = ((delayMs / 1000f) * sampleRate).toInt().coerceIn(1, echoBuffer.size - 1)
        val out = ShortArray(len)
        for (i in 0 until len) {
            val delayed = echoBuffer[(echoWritePos - delaySamples + echoBuffer.size) % echoBuffer.size]
            val mixed = (input[i] + delayed * decay).toInt().coerceIn(-32768, 32767)
            out[i] = mixed.toShort()
            echoBuffer[echoWritePos] = mixed.toShort()
            echoWritePos = (echoWritePos + 1) % echoBuffer.size
        }
        return out
    }

    private fun bandLimit(input: ShortArray, len: Int, centerHz: Float): ShortArray {
        val out = ShortArray(len)
        val lowAlpha = min(1f, centerHz / (sampleRate / 2f))
        var lp = 0f
        var hp = 0f
        var prevIn = 0f
        for (i in 0 until len) {
            val x = input[i].toFloat()
            lp += lowAlpha * (x - lp)
            hp = 0.95f * (hp + x - prevIn)
            prevIn = x
            val mixed = (lp * 0.5f + hp * 0.5f)
            out[i] = mixed.toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    private fun distort(input: ShortArray, len: Int, threshold: Float): ShortArray {
        val clip = (32767 * threshold).toInt()
        val out = ShortArray(len)
        for (i in 0 until len) {
            val boosted = input[i] * 2
            out[i] = boosted.coerceIn(-clip, clip).toShort()
        }
        return out
    }

    private fun reverb(input: ShortArray, len: Int, decay: Float): ShortArray {
        val out = ShortArray(len)
        for (i in 0 until len) {
            val tap = reverbBuffer[reverbWritePos]
            val mixed = (input[i] + tap * decay).toInt().coerceIn(-32768, 32767)
            out[i] = mixed.toShort()
            reverbBuffer[reverbWritePos] = mixed.toShort()
            reverbWritePos = (reverbWritePos + 1) % reverbBuffer.size
        }
        return out
    }

    private fun whisper(input: ShortArray, len: Int, mix: Float): ShortArray {
        val out = ShortArray(len)
        for (i in 0 until len) {
            val quiet = (input[i] * 0.3f)
            val noise = ((Math.random() - 0.5) * 2000 * mix).toFloat()
            out[i] = (quiet + noise).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }
}
