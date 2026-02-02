package com.crazymobileapp.emergency

interface EmergencyController {
    suspend fun triggerEmergency(settings: EmergencySettings)
    suspend fun stopEmergency()
}

class EmergencySignalPlan(
    val sirenPattern: List<Long> = listOf(250L, 250L, 500L, 250L),
    val flashPattern: List<Long> = listOf(200L, 200L, 200L, 200L),
    val announcement: String = "Police are here. Emergency services are on the way."
)
