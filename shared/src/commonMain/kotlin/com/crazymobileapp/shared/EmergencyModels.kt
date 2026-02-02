package com.crazymobileapp.shared

data class EmergencySettings(
    val trigger: TriggerOption,
    val emergencyNumber: String,
    val policeNumber: String = "911",
    val sirenSound: SirenSound = SirenSound.POLICE_SIREN,
    val flashlightEnabled: Boolean = true,
    val callEnabled: Boolean = true,
    val sirenEnabled: Boolean = true,
)

enum class TriggerOption {
    VOLUME_UP_DOWN,
    POWER_TRIPLE_PRESS,
    CUSTOM_GESTURE,
}

enum class SirenSound {
    POLICE_SIREN,
    LOUD_ALARM,
}

data class EmergencyStatus(
    val isActive: Boolean,
    val lastTriggeredAtMillis: Long?,
)
