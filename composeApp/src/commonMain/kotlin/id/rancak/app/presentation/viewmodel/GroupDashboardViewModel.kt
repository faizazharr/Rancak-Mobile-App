package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.BranchReport
import id.rancak.app.domain.model.Group
import id.rancak.app.domain.model.GroupOverview
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.GroupsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class GroupDashboardUiState(
    val groups: ImmutableList<Group> = persistentListOf(),
    val selectedGroup: Group? = null,
    val overview: GroupOverview? = null,
    val branches: ImmutableList<BranchReport> = persistentListOf(),
    val isLoadingGroups: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val error: String? = null
)

class GroupDashboardViewModel(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDashboardUiState())
    val uiState: StateFlow<GroupDashboardUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGroups = true, error = null) }
            when (val result = groupsRepository.getGroups()) {
                is Resource.Success -> {
                    val groups = result.data.toImmutableList()
                    _uiState.update { it.copy(groups = groups, isLoadingGroups = false) }
                    // Auto-select first group
                    groups.firstOrNull()?.let { selectGroup(it) }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoadingGroups = false, error = result.message)
                }
            }
        }
    }

    fun selectGroup(group: Group) {
        _uiState.update { it.copy(selectedGroup = group, isLoadingDetail = true) }
        viewModelScope.launch {
            val overviewResult = groupsRepository.getGroupOverview(group.uuid)
            val branchesResult = groupsRepository.getGroupBranches(group.uuid)

            _uiState.update { state ->
                state.copy(
                    isLoadingDetail = false,
                    overview = (overviewResult as? Resource.Success)?.data ?: state.overview,
                    branches = (branchesResult as? Resource.Success)?.data?.toImmutableList()
                        ?: state.branches,
                    error = (overviewResult as? Resource.Error)?.message
                        ?: (branchesResult as? Resource.Error)?.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
