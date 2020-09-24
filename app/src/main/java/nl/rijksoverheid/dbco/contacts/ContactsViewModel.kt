/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts

import android.R.id
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.contacts.data.Contact
import nl.rijksoverheid.dbco.contacts.data.ContactName
import timber.log.Timber
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.arrayListOf
import kotlin.collections.forEach
import kotlin.collections.set


class ContactsViewModel(val context: Context) : ViewModel() {

    private val _contactsLiveData = MutableLiveData<ArrayList<Contact>>()
    val contactsLiveData: LiveData<ArrayList<Contact>> = _contactsLiveData

    fun fetchContacts() {
        viewModelScope.launch {
            val contactsListAsync = async { getPhoneContacts() }
            val contactNumbersAsync = async { getContactNumbers() }
            val contactEmailAsync = async { getContactEmails() }

            val contacts = contactsListAsync.await()
            val contactNumbers = contactNumbersAsync.await()
            val contactEmails = contactEmailAsync.await()

            contacts.forEach {
                contactNumbers[it.id]?.let { numbers ->
                    it.numbers = numbers
                }
                contactEmails[it.id]?.let { emails ->
                    it.emails = emails
                }

//                val contactNameAsync = async { getContactFirstLastname(it.id) }
//                val contactName = contactNameAsync.await()
//                contactName?.let { name ->
//                    it.name = name
//                }
            }
            _contactsLiveData.postValue(contacts)
        }
    }

    private suspend fun getPhoneContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val contactsCursor = context.contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (contactsCursor != null && contactsCursor.count > 0) {
            val idIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
//            val firstNameIndex =
//                contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
//            val lastNameIndex =
//                contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
            while (contactsCursor.moveToNext()) {
                val id = contactsCursor.getString(idIndex)
                val displayName = contactsCursor.getString(nameIndex)
//                val firstName = contactsCursor.getString(firstNameIndex)
//                val lastName = contactsCursor.getString(lastNameIndex)
                if (displayName != null) {
                    contactsList.add(Contact(id, displayName))
                }
            }
            contactsCursor.close()
        }
        return contactsList
    }

    private suspend fun getContactNumbers(): HashMap<String, ArrayList<String>> {
        val contactsNumberMap = HashMap<String, ArrayList<String>>()
        val phoneCursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (phoneCursor != null && phoneCursor.count > 0) {
            val contactIdIndex =
                phoneCursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex =
                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (phoneCursor.moveToNext()) {
                val contactId = phoneCursor.getString(contactIdIndex)
                val number: String = phoneCursor.getString(numberIndex)
                //check if the map contains key or not, if not then create a new array list with number
                if (contactsNumberMap.containsKey(contactId)) {
                    contactsNumberMap[contactId]?.add(number)
                } else {
                    contactsNumberMap[contactId] = arrayListOf(number)
                }
            }
            //contact contains all the number of a particular contact
            phoneCursor.close()
        }
        return contactsNumberMap
    }

    private suspend fun getContactEmails(): HashMap<String, ArrayList<String>> {
        val contactsEmailMap = HashMap<String, ArrayList<String>>()
        val emailCursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (emailCursor != null && emailCursor.count > 0) {
            val contactIdIndex =
                emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val emailIndex =
                emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
            while (emailCursor.moveToNext()) {
                val contactId = emailCursor.getString(contactIdIndex)
                val email = emailCursor.getString(emailIndex)
                //check if the map contains key or not, if not then create a new array list with email
                if (contactsEmailMap.containsKey(contactId)) {
                    contactsEmailMap[contactId]?.add(email)
                } else {
                    contactsEmailMap[contactId] = arrayListOf(email)
                }
            }
            //contact contains all the emails of a particular contact
            emailCursor.close()
        }
        return contactsEmailMap
    }

    private suspend fun getContactFirstLastname(contactID: String): ContactName? {
        var contactName: ContactName? = null
        Timber.d("Retrieving data for user with id $contactID")

        val whereName =
            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
        val whereNameParams = arrayOf(
            contactID,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        )

        val namesCursor: Cursor? = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            whereName,
            whereNameParams,
            null
        )
        if (namesCursor != null && namesCursor.count > 0) {
            val indexGivenName =
                namesCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
            val indexFamilyName =
                namesCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
            //val indexMiddleName = namesCursor.getColumnIndexOrThrow (ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
            //contact contains all the number of a particular contact
            Timber.d("Checking for $contactID with index $indexGivenName and index $indexFamilyName")
            val givenName = if (indexGivenName > 0) {
                namesCursor.getString(indexGivenName)
            } else {
                ""
            }

            val familyName = if (indexFamilyName > 0) {
                namesCursor.getString(indexFamilyName)
            } else {
                ""
            }

            contactName =
                (ContactName(
                    givenName,
                    familyName
                ))

            namesCursor.close()
        }


        return contactName
    }

}