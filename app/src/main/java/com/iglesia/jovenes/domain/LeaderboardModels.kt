package com.iglesia.jovenes.domain

data class LeaderboardEntryItem(
    val position: Int,
    val userId: Int,
    val fullName: String,
    val church: String?,
    val levelName: String,
    val points: Int,
    val isCurrentUser: Boolean = false,
)

data class LeaderboardScreenData(
    val top3: List<LeaderboardEntryItem>,
    val fullList: List<LeaderboardEntryItem>,
    val currentUserEntry: LeaderboardEntryItem?,
)
