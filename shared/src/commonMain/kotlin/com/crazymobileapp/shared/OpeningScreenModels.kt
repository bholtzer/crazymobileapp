package com.crazymobileapp.shared

data class EmergencyNumberOption(
    val label: String,
    val number: String,
)

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
)

enum class LocationShareStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SENT,
    FAILED,
}

data class OpeningScreenState(
    val availableNumbers: List<EmergencyNumberOption>,
    val selectedNumber: EmergencyNumberOption?,
    val locationShareStatus: LocationShareStatus,
    val lastSharedLocation: GeoCoordinate?,
)

data class LocationShareResult(
    val status: LocationShareStatus,
    val message: String? = null,
)

interface OpeningScreenController {
    suspend fun sendStartupLocation(
        selectedNumber: EmergencyNumberOption,
        location: GeoCoordinate,
    ): LocationShareResult
}
