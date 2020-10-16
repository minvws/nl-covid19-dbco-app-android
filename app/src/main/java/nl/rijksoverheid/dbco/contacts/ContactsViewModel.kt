/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.contacts.data.LocalContact
import nl.rijksoverheid.dbco.contacts.data.entity.TasksResponse


class ContactsViewModel(val context: Context) : ViewModel() {

    private val _localContactsLiveData = MutableLiveData<ArrayList<LocalContact>>()
    val localContactsLiveData: LiveData<ArrayList<LocalContact>> = _localContactsLiveData


    private val repository = ContactsRepository(context)

    private val fullLocalContacts: ArrayList<LocalContact> = ArrayList<LocalContact>()

    private val _indexTasks = MutableLiveData<TasksResponse>()
    val indexTasksLivedata: LiveData<TasksResponse> = _indexTasks

    fun fetchLocalContacts() {
        viewModelScope.launch {
            val contacts = repository.fetchDeviceContacts()
            fullLocalContacts.clear()
            fullLocalContacts.addAll(contacts)
            _localContactsLiveData.postValue(contacts)
        }
    }

    fun filterLocalContactsOnName(name: String) {
        val filteredList = fullLocalContacts.filter {
            it.displayName.contains(name, true)
        } as ArrayList<LocalContact>
        _localContactsLiveData.postValue(filteredList)
    }


    fun fetchTasksForUUID(uuid: String = "") {
        viewModelScope.launch {
            val taskResponse = repository.retrieveTasksForUUID()
            _indexTasks.postValue(taskResponse)
        }
    }


}