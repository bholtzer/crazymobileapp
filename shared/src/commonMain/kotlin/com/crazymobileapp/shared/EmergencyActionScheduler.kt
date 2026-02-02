package com.crazymobileapp.shared

interface EmergencyActionScheduler {
    suspend fun triggerEmergency(settings: EmergencySettings): EmergencyStatus
}
