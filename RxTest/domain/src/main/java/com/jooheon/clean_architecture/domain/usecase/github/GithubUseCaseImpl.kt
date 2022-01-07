package com.jooheon.clean_architecture.domain.usecase.github

import com.jooheon.clean_architecture.domain.common.Resource
import com.jooheon.clean_architecture.domain.entity.Entity
import com.jooheon.clean_architecture.domain.repository.GithubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.Flow

class GithubUseCaseImpl(
    private val githubRepository: GithubRepository
): GithubUseCase {

    override fun getRepository(owner: String): Flow<Resource<List<Entity.Repository>>> {
        return flow {
//            Log.d(TAG, "usecase start")
            emit(Resource.Loading)
            val result = githubRepository.getRepository(owner)

            when(result) {
                is Resource.Success -> {
                    // do something ...
//                    Log.d(TAG, result.value.toString())
                }

                is Resource.Failure -> {
                    // do something ...
//                    Log.d(TAG, "code: ${result.code}, msg: ${result.message}, status: {$result.failureStatus}")
                }

                is Resource.Default -> {
                    // do something ...
                }
            }
//            Log.d(TAG, "usecase end")
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    override fun getBranch(owner: String, repository: String): Flow<Resource<List<Entity.Branch>>> {
        return flow {
            emit(Resource.Loading)
            val result = githubRepository.getBranch(owner, repository)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    companion object {
        val TAG = GithubUseCaseImpl::class.simpleName
    }
}