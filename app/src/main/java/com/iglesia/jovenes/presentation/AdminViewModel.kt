package com.iglesia.jovenes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iglesia.jovenes.domain.AdminActivityItem
import com.iglesia.jovenes.domain.AdminLevelItem
import com.iglesia.jovenes.domain.AdminMissionItem
import com.iglesia.jovenes.domain.AdminRepository
import com.iglesia.jovenes.domain.AdminStats
import com.iglesia.jovenes.domain.AdminUserItem
import com.iglesia.jovenes.domain.CreateActivityParams
import com.iglesia.jovenes.domain.CreateMissionParams
import com.iglesia.jovenes.domain.EnrolledUserItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AdminTab { OVERVIEW, USERS, MISSIONS, ACTIVITIES, SETTINGS }

class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val adminUserId: Int,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AdminTab.OVERVIEW)
    val selectedTab: StateFlow<AdminTab> = _selectedTab.asStateFlow()

    private val _stats = MutableStateFlow<AdminStats?>(null)
    val stats: StateFlow<AdminStats?> = _stats.asStateFlow()

    private val _users = MutableStateFlow<List<AdminUserItem>>(emptyList())
    val users: StateFlow<List<AdminUserItem>> = _users.asStateFlow()

    private val _missions = MutableStateFlow<List<AdminMissionItem>>(emptyList())
    val missions: StateFlow<List<AdminMissionItem>> = _missions.asStateFlow()

    private val _activities = MutableStateFlow<List<AdminActivityItem>>(emptyList())
    val activities: StateFlow<List<AdminActivityItem>> = _activities.asStateFlow()

    private val _levels = MutableStateFlow<List<AdminLevelItem>>(emptyList())
    val levels: StateFlow<List<AdminLevelItem>> = _levels.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showCreateMissionDialog = MutableStateFlow(false)
    val showCreateMissionDialog: StateFlow<Boolean> = _showCreateMissionDialog.asStateFlow()

    private val _showCreateActivityDialog = MutableStateFlow(false)
    val showCreateActivityDialog: StateFlow<Boolean> = _showCreateActivityDialog.asStateFlow()

    private val _selectedActivity = MutableStateFlow<AdminActivityItem?>(null)
    val selectedActivity: StateFlow<AdminActivityItem?> = _selectedActivity.asStateFlow()

    private val _enrolledUsers = MutableStateFlow<List<EnrolledUserItem>>(emptyList())
    val enrolledUsers: StateFlow<List<EnrolledUserItem>> = _enrolledUsers.asStateFlow()

    private val _showEnrolledUsersDialog = MutableStateFlow(false)
    val showEnrolledUsersDialog: StateFlow<Boolean> = _showEnrolledUsersDialog.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(tab: AdminTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowCreateMissionDialog(show: Boolean) {
        _showCreateMissionDialog.value = show
    }

    fun setShowCreateActivityDialog(show: Boolean) {
        _showCreateActivityDialog.value = show
    }

    fun setShowEnrolledUsersDialog(show: Boolean) {
        _showEnrolledUsersDialog.value = show
        if (!show) {
            _selectedActivity.value = null
            _enrolledUsers.value = emptyList()
        }
    }

    fun openEnrolledUsersDialog(activity: AdminActivityItem) {
        _selectedActivity.value = activity
        _showEnrolledUsersDialog.value = true
        loadEnrolledUsers(activity.id)
    }

    private fun loadEnrolledUsers(activityId: Int) {
        viewModelScope.launch {
            adminRepository.getEnrolledUsersForActivity(activityId)
                .onSuccess { _enrolledUsers.value = it }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun markAttendance(userId: Int, activityId: Int, points: Int) {
        viewModelScope.launch {
            adminRepository.markAttendance(userId, activityId, points)
                .onSuccess {
                    loadEnrolledUsers(activityId)
                    loadData()
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getStats().onSuccess { _stats.value = it }
            adminRepository.getUsers().onSuccess { _users.value = it }
            adminRepository.getMissions().onSuccess { _missions.value = it }
            adminRepository.getActivities().onSuccess { _activities.value = it }
            adminRepository.getLevels().onSuccess { _levels.value = it }

            _isLoading.value = false
        }
    }

    fun toggleUserStatus(userId: Int, currentStatus: String) {
        viewModelScope.launch {
            adminRepository.toggleUserStatus(userId, currentStatus)
                .onSuccess { loadData() }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun createMission(name: String, description: String, bonusPoints: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val params = CreateMissionParams(name, description, bonusPoints)
            adminRepository.createMission(params)
                .onSuccess {
                    _showCreateMissionDialog.value = false
                    loadData()
                }
                .onFailure {
                    _isLoading.value = false
                    _errorMessage.value = it.message
                }
        }
    }

    fun toggleMissionStatus(missionId: Int, currentStatus: String) {
        viewModelScope.launch {
            adminRepository.toggleMissionStatus(missionId, currentStatus)
                .onSuccess { loadData() }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun createActivity(
        title: String,
        description: String,
        location: String,
        maxCapacity: Int,
        basePoints: Int,
        bonusPoints: Int,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val params = CreateActivityParams(title, description, location, maxCapacity, basePoints, bonusPoints)
            adminRepository.createActivity(adminUserId, params)
                .onSuccess {
                    _showCreateActivityDialog.value = false
                    loadData()
                }
                .onFailure {
                    _isLoading.value = false
                    _errorMessage.value = it.message
                }
        }
    }

    fun toggleActivityStatus(activityId: Int, currentStatus: String) {
        viewModelScope.launch {
            adminRepository.toggleActivityStatus(activityId, currentStatus)
                .onSuccess { loadData() }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun updateLevelPoints(levelId: Int, points: Int) {
        viewModelScope.launch {
            adminRepository.updateLevelPoints(levelId, points)
                .onSuccess { loadData() }
                .onFailure { _errorMessage.value = it.message }
        }
    }
}

class AdminViewModelFactory(
    private val adminRepository: AdminRepository,
    private val adminUserId: Int,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AdminViewModel(adminRepository, adminUserId) as T
}
