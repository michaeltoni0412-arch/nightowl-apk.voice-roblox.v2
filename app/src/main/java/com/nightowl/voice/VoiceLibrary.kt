package com.nightowl.voice

enum class EffectType { NORMAL, PITCH, ROBOT, ECHO, RADIO, DISTORT, REVERB, WHISPER }

data class VoicePreset(
    val name: String,
    val type: EffectType,
    val p1: Float = 0f,
    val p2: Float = 0f
)

object VoiceLibrary {

    val presets: List<VoicePreset> = buildList {
        add(VoicePreset("Normal", EffectType.NORMAL))

        val pitchSteps = listOf(
            "Barely Deep" to 0.94f, "Slight Deep" to 0.88f, "Deep" to 0.80f,
            "Deeper" to 0.72f, "Very Deep" to 0.65f, "Giant" to 0.55f,
            "Titan" to 0.45f, "Abyss" to 0.38f,
            "Slight High" to 1.08f, "High" to 1.15f, "Higher" to 1.25f,
            "Chipmunk" to 1.4f, "Helium" to 1.55f, "Extreme Helium" to 1.75f,
            "Squeaky Mouse" to 2.0f, "Ultrasonic" to 2.3f
        )
        pitchSteps.forEach { (n, v) -> add(VoicePreset(n, EffectType.PITCH, v)) }

        val robotSteps = listOf(
            "Soft Robot" to 60f, "Robot" to 110f, "Heavy Robot" to 180f,
            "Fast Robot" to 260f, "Buzzy Robot" to 340f, "Dalek" to 420f,
            "Alien" to 90f, "Alien Drone" to 150f, "Cyborg" to 220f,
            "Machine Voice" to 300f, "Transformer" to 380f
        )
        robotSteps.forEach { (n, v) -> add(VoicePreset(n, EffectType.ROBOT, v)) }

        val echoSteps = listOf(
            Triple("Light Echo", 80f, 0.25f), Triple("Echo", 150f, 0.35f),
            Triple("Hall Echo", 220f, 0.45f), Triple("Cave", 320f, 0.55f),
            Triple("Canyon", 420f, 0.6f), Triple("Stadium", 500f, 0.5f),
            Triple("Tunnel", 260f, 0.5f), Triple("Distant Voice", 380f, 0.4f),
            Triple("Ghost Echo", 300f, 0.65f), Triple("Haunted Hall", 450f, 0.7f)
        )
        echoSteps.forEach { (n, d, dec) -> add(VoicePreset(n, EffectType.ECHO, d, dec)) }

        val radioSteps = listOf(
            "Old Radio" to 1200f, "Walkie Talkie" to 1500f, "Phone Call" to 1800f,
            "Megaphone" to 900f, "Intercom" to 1400f, "Broken Speaker" to 700f,
            "AM Radio" to 1000f, "Police Scanner" to 1600f
        )
        radioSteps.forEach { (n, v) -> add(VoicePreset(n, EffectType.RADIO, v)) }

        val distortSteps = listOf(
            "Gritty" to 0.6f, "Crunchy" to 0.45f, "Demon" to 0.3f,
            "Growl" to 0.35f, "Monster" to 0.25f, "Static Voice" to 0.5f,
            "Broken Mic" to 0.4f, "Overdrive" to 0.55f
        )
        distortSteps.forEach { (n, v) -> add(VoicePreset(n, EffectType.DISTORT, v)) }

        val reverbSteps = listOf(
            "Small Room" to 0.2f, "Big Room" to 0.35f, "Church" to 0.5f,
            "Auditorium" to 0.55f, "Underwater" to 0.45f, "Dreamy" to 0.4f
        )
        reverbSteps.forEach { (n, v) -> add(VoicePreset(n, EffectType.REVERB, v)) }

        add(VoicePreset("Whisper", EffectType.WHISPER, 0.35f))
        add(VoicePreset("Soft Whisper", EffectType.WHISPER, 0.25f))
        add(VoicePreset("Raspy Whisper", EffectType.WHISPER, 0.45f))
    }
}
