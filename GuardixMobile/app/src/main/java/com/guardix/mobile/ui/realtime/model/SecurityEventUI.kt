package com.guardix.mobile.ui.realtime.model

data class SecurityEventUI(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: SecuritySeverity,
    val source: String
)

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
