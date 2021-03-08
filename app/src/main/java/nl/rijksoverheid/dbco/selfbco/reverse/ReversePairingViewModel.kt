package nl.rijksoverheid.dbco.selfbco.reverse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus.Success
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus
import nl.rijksoverheid.dbco.user.IUserRepository

class ReversePairingViewModel(val userRepository: IUserRepository) : ViewModel() {

    private val _pairingCode = MutableLiveData<String>()
    val pairingCode: LiveData<String> = _pairingCode

    private val _userHasSharedCode = MutableLiveData(false)
    val userHasSharedCode: LiveData<Boolean> = _userHasSharedCode

    private val _pairingStatus = MutableLiveData<ReversePairingStatus>()
    val pairingStatus: LiveData<ReversePairingStatus> = _pairingStatus

    private var pollingJob: Job? = null

    fun retrievePairingCode() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pairingResponse = userRepository.retrieveReversePairingCode()
                if (pairingResponse.isSuccessful) {
                    val code = pairingResponse.body()?.code
                    val token = pairingResponse.body()?.token
                    if (code != null && token != null) {
                        _pairingCode.postValue(code)
                        startPairing(token)
                    }
                }
            }
        }
    }

    private fun startPairing(token: String) {
        cancelPollingForChanges()
        pollingJob = viewModelScope.launch {
            val poller = ReversePairingStatePoller(userRepository, Dispatchers.IO)
            val flow = poller.poll(POLLING_DELAY, token).onEach { result ->
                _pairingStatus.postValue(result)
                if (result is Success) {
                    _userHasSharedCode.postValue(true)
                }
                poller.close()
                cancelPollingForChanges()
            }
            flow.collect()
        }
    }

    fun setUserHasSharedCode(hasShared: Boolean) {
        _userHasSharedCode.postValue(hasShared)
    }

    fun cancelPollingForChanges() {
        pollingJob?.cancel()
    }

    companion object {
        private const val POLLING_DELAY = 10_000L
    }
}