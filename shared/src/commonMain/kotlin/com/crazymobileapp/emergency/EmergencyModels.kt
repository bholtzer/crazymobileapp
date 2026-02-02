package com.crazymobileapp.emergency

data class EmergencyContact(
    val label: String,
    val phoneNumber: String
)

data class EmergencySettings(
    val policeNumber: String = "911",
    val emergencyContact: EmergencyContact? = null,
    val activationMode: ActivationMode = ActivationMode.VolumeButtons
)

enum class ActivationMode {
    VolumeButtons,
    CustomGesture,
    InAppButton
}
