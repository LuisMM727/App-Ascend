package com.iglesia.jovenes.domain

/**
 * Modelo de una entrada individual en la tabla de clasificación.
 */
data class LeaderboardEntryItem(
    val position: Int,
    val userId: Int,
    val fullName: String,
    val church: String?,
    val levelName: String,
    val points: Int,
    val isCurrentUser: Boolean = false,
)

/**
 * Modelo contenedor con los datos de la pantalla de clasificación (Podio Top 3, Lista General y Posición Propia).
 */
data class LeaderboardScreenData(
    val top3: List<LeaderboardEntryItem>,
    val fullList: List<LeaderboardEntryItem>,
    val currentUserEntry: LeaderboardEntryItem?,
)
