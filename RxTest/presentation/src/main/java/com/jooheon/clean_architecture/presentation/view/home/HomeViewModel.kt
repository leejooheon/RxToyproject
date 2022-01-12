package com.jooheon.clean_architecture.presentation.view.home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.jooheon.clean_architecture.domain.common.Resource
import com.jooheon.clean_architecture.domain.entity.Entity
import com.jooheon.clean_architecture.domain.usecase.github.GithubUseCase
import com.jooheon.clean_architecture.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val githubUseCase: GithubUseCase
): BaseViewModel() {
    private val TAG = HomeViewModel::class.java.simpleName

    private val _repositoryResponse = mutableStateOf<Resource<List<Entity.Repository>>>(Resource.Default)  // viewModel에서 값에 대한 변경권을 갖고 (private),
    val repositoryResponse = _repositoryResponse // view에서는 State를 활용해 참조만 가능하게 한다.

    private val _commitResponse = mutableStateOf<Resource<List<Entity.Commit>>>(Resource.Default)
    val commitResponse = _commitResponse

    private val _branchResponse = mutableStateOf<Resource<List<Entity.Branch>>>(Resource.Default)
    val branchResponse = _branchResponse

    private val _loadingResponse = mutableStateOf(false)
    val loadingResponse = _loadingResponse

    private var lastSearchedOwner: String? = null

    fun onNavigationClicked() {
        Log.d(TAG, "onNavigationClicked")
    }

    fun onFavoriteClicked() {
        Log.d(TAG, "onFavoriteClicked")
    }

    fun onSearchClicked() {
        Log.d(TAG, "onSearchClicked")
    }

    fun onSettingClicked() {
        Log.d(TAG, "onSettingClicked")
    }

    fun callRepositoryApi(owner: String) {
        lastSearchedOwner = owner
        githubUseCase.getRepository(owner)
            .onEach {
                Log.d(TAG, "result: ${it}")

                _loadingResponse.value = it is Resource.Loading
                _repositoryResponse.value = it
            }
            .launchIn(viewModelScope)
    }

    fun callBranchApi(repository: String) {
        lastSearchedOwner?.let { owner ->
            githubUseCase.getBranch(owner, repository)
                .onEach {
                    Log.d(TAG, "result: ${it}")

                    _loadingResponse.value = it is Resource.Loading
                    _branchResponse.value = it
                }.launchIn(viewModelScope)
        }
    }

    fun callCommitApi(repository: String) {
        lastSearchedOwner?.let { owner ->
            githubUseCase.getCommit(owner, repository)
                .map { it }
                .onEach {
                    _loadingResponse.value = it is Resource.Loading
                    _commitResponse.value = it
                }.catch {
                    // If an error happens
                }
                .launchIn(viewModelScope)
        }?.run {
            // TODO: notify to view
        }
    }

    fun multipleApiTest(repository: String) {
        githubUseCase.getBranchAndCommit(lastSearchedOwner!!, repository)
            .onEach {
                val branchResponse = it.first
                val commitResponse = it.second
                Log.d(TAG, "result: ${commitResponse is Resource.Loading}")

                _loadingResponse.value = commitResponse is Resource.Loading

                if(commitResponse is Resource.Success) {
                    Log.d(TAG, "commitResponse: ${commitResponse.value}")
                    _commitResponse.value = commitResponse
                }
                if(branchResponse is Resource.Success) {
                    Log.d(TAG, "branchResponse: ${branchResponse.value}")
                    _branchResponse.value = branchResponse
                }

            }.catch {

            }.launchIn(viewModelScope)
    }

}