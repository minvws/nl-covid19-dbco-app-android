/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse

class ContactsRepository(val context: Context) {

    suspend fun fetchDeviceContacts(): ArrayList<LocalContact> {
        return withContext(Dispatchers.IO) {
            // Blocking network request code
            val contactsListAsync = async { getPhoneContacts() }
            val contactNumbersAsync = async { getContactNumbers() }
            val contactEmailAsync = async { getContactEmails() }

            val contacts = contactsListAsync.await()
            val contactNumbers = contactNumbersAsync.await()
            val contactEmails = contactEmailAsync.await()

            contacts.forEach {
                contactNumbers[it.id]?.let { numbers ->
                    it.number = numbers[0]
                }
                contactEmails[it.id]?.let { emails ->
                    it.email = emails[0]
                }
            }

            return@withContext contacts
        }
    }


//region Local contacts

    private fun getPhoneContacts(): ArrayList<LocalContact> {
        val contactsList = ArrayList<LocalContact>()
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
            while (contactsCursor.moveToNext()) {
                val id = contactsCursor.getString(idIndex)
                val displayName = contactsCursor.getString(nameIndex)
                if (displayName != null) {
                    contactsList.add(LocalContact(id, displayName))
                }
            }
            contactsCursor.close()
        }
        return contactsList
    }

    private fun getContactNumbers(): HashMap<String, ArrayList<String>> {
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

    private fun getContactEmails(): HashMap<String, ArrayList<String>> {
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

    //endregion local

    //region Tasks from backend

    suspend fun retrieveTasksForUUID(uuid: String = ""): TasksResponse {
        return Json.decodeFromString(MOCK_TASKS)
    }

    //endregion


    companion object {
        const val MOCK_TASKS = "{\n" +
                "  \"tasks\": [\n" +
                "    {\n" +
                "      \"uuid\": \"123e4567-e89b-12d3-a456-426614172000\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"source\": \"portal\",\n" +
                "      \"label\": \"Lia B\",\n" +
                "      \"taskContext\": \"Partner\",\n" +
                "      \"category\": \"1\",\n" +
                "      \"communication\": \"index\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"uuid\": \"123e4567-e89b-22d3-a456-426614172000\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"source\": \"portal\",\n" +
                "      \"label\": \"Aziz F.\",\n" +
                "      \"taskContext\": \"Voetbaltrainer\",\n" +
                "      \"category\": \"2a\",\n" +
                "      \"communication\": \"index\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"uuid\": \"123e4567-e89b-32d3-a456-426614172000\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"source\": \"portal\",\n" +
                "      \"label\": \"Job J.\",\n" +
                "      \"taskContext\": \"Collega\",\n" +
                "      \"category\": \"2b\",\n" +
                "      \"communication\": \"staff\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"uuid\": \"123e4567-e89b-42d3-a456-426614172000\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"source\": \"portal\",\n" +
                "      \"label\": \"Joris L.\",\n" +
                "      \"taskContext\": \"null\",\n" +
                "      \"category\": \"3\",\n" +
                "      \"communication\": \"index\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"uuid\": \"123e4567-e89b-52d3-a456-426614172000\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"source\": \"portal\",\n" +
                "      \"label\": \"Peter V.\",\n" +
                "      \"taskContext\": \"Zakenrelatie\",\n" +
                "      \"category\": \"3\",\n" +
                "      \"communication\": \"none\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
    }

}