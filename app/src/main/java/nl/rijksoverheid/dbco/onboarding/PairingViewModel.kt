/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.user.IUserRepository
import retrofit2.HttpException

@ExperimentalSerializationApi
class PairingViewModel(
    private val userRepository: IUserRepository,
    private val tasksRepository: ITaskRepository
) : ViewModel() {

    private val _pairingResult = MutableLiveData<PairingResult>()
    val pairingResult: LiveData<PairingResult> = _pairingResult

    fun pair(pin: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    userRepository.pair(pin)
                    val case = tasksRepository.fetchCase()
                    _pairingResult.postValue(PairingResult.Success(case))
                } catch (ex: Throwable) {
                    if (ex is HttpException && ex.code() == 400) {
                        _pairingResult.postValue(PairingResult.Invalid)
                    } else {
                        _pairingResult.postValue(PairingResult.Error(ex))
                    }
                }
            }
        }
    }

    sealed class PairingResult {
        data class Success(val case: Case) : PairingResult()
        object Invalid : PairingResult()
        data class Error(val exception: Throwable) : PairingResult()
    }
}