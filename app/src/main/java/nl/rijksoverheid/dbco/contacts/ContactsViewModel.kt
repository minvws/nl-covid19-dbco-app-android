/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.DefaultDispatcherProvider
import nl.rijksoverheid.dbco.DispatcherProvider
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.util.SingleLiveEvent

/**
 * ViewModel which exposes local contacts to attach to a Task in the current Case
 */
class ContactsViewModel(
    private val repository: ContactsRepository,
    private val caseRepository: ICaseRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _localContactsLiveData = SingleLiveEvent<ArrayList<LocalContact>>()

    /**
     * Exposes a list of contacts in the users phone book
     */
    val localContactsLiveDataItem: LiveData<ArrayList<LocalContact>> = _localContactsLiveData

    private val fullLocalContactItems: ArrayList<LocalContact> = ArrayList()

    fun fetchLocalContacts() {
        viewModelScope.launch(dispatchers.main()) {
            val contacts = repository.fetchDeviceContacts()
            fullLocalContactItems.clear()
            fullLocalContactItems.addAll(contacts)
            _localContactsLiveData.postValue(contacts)
        }
    }

    fun filterLocalContactsOnName(name: String) {
        val filteredList = fullLocalContactItems.filter {
            it.getDisplayName().contains(name, true)
        } as ArrayList<LocalContact>
        _localContactsLiveData.postValue(filteredList)
    }

    fun filterSuggestedContacts(name: String): ArrayList<LocalContact> {
        val firstName = name.split(" ")[0]
        return fullLocalContactItems.filter {
            it.getDisplayName().contains(firstName, true)
        } as ArrayList<LocalContact>
    }

    fun getLocalContactNames(): ArrayList<String> {
        return fullLocalContactItems.map {
            it.getDisplayName()
        } as ArrayList<String>
    }

    fun getTaskLabel(uuid: String): String? = caseRepository.getTask(uuid).label

    fun onContactPicked(taskUuid: String, contact: LocalContact) {
        val task = caseRepository.getTask(taskUuid)
        task.linkedContact = contact
        caseRepository.saveTask(
            task,
            shouldMerge = { current -> current.uuid == task.uuid },
            shouldUpdate = { current -> task != current }
        )
    }

    fun onNoContactPicked(indexTaskUuid: String) {
        val task = caseRepository.getTask(indexTaskUuid)
        if (!task.hasCategoryOrExposure()) {
            caseRepository.deleteTask(uuid = indexTaskUuid)
        }
    }
}