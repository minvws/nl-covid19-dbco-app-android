/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.data.entity.SealedData
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants


class TasksRepository(context: Context, private val userRepository: IUserRepository) :
    ITaskRepository {
    private val api = StubbedAPI.create(context)
    private var cachedCase: Case? = null
    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    private var caseChanged = false

    private val nullLocalContactSerializer = object : KSerializer<LocalContact> {
        override val descriptor: SerialDescriptor
            get() = LocalContact.serializer().descriptor

        override fun deserialize(decoder: Decoder): LocalContact {
            return LocalContact.serializer().deserialize(decoder)
        }

        override fun serialize(encoder: Encoder, value: LocalContact) {
            encoder.encodeNull()
        }
    }

    override suspend fun fetchCase(): Case? {
        // restore saved case
        encryptedSharedPreferences.getString(
            ITaskRepository.CASE_KEY,
            null
        )?.apply {
            cachedCase = Defaults.json.decodeFromString(this)
        }

        userRepository.getToken()?.let {
            val data = withContext(Dispatchers.IO) { api.getCase(it) }
            val sealedCase = data.body()?.sealedCase
            val cipherBytes =
                Base64.decode(sealedCase?.ciphertext, IUserRepository.BASE64_FLAGS)
            val nonceBytes = Base64.decode(sealedCase?.nonce, IUserRepository.BASE64_FLAGS)
            val rxBytes = Base64.decode(userRepository.getRx(), IUserRepository.BASE64_FLAGS)
            val caseBodyBytes = ByteArray(cipherBytes.size - Sodium.crypto_secretbox_macbytes())
            Sodium.crypto_secretbox_open_easy(
                caseBodyBytes,
                cipherBytes,
                cipherBytes.size,
                nonceBytes,
                rxBytes
            )
            val caseString = String(caseBodyBytes)
            val remoteCase: Case = Defaults.json.decodeFromString(caseString)

            if (cachedCase == null) {
                // it is first time we fetch case, save it in cache
                cachedCase = remoteCase
            } else {
                // case was already fetched and stored, we just need to check for new tasks (by uuid)
                remoteCase.tasks?.forEach { remoteTask ->
                    var found = false
                    cachedCase?.tasks?.forEach { currentTask ->
                        if (remoteTask.uuid == currentTask.uuid) {
                            found = true
                        }
                    }
                    if (!found) {
                        cachedCase?.tasks?.add(remoteTask)
                    }
                }
            }

            val storeString = Defaults.json.encodeToString(remoteCase)
            encryptedSharedPreferences.edit().putString(ITaskRepository.CASE_KEY, storeString)
                .apply()
        }
        return cachedCase
    }

    override fun saveChangesToTask(updatedTask: Task) {
        caseChanged = true
        val currentTasks = cachedCase?.tasks as ArrayList
        var found = false
        currentTasks.forEachIndexed { index, currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                currentTasks[index] = updatedTask
                found = true
            }
        }
        if (!found) {
            currentTasks.add(updatedTask)
        }
        // save whole task in prefs
        val storeString = Defaults.json.encodeToString(cachedCase)
        encryptedSharedPreferences.edit().putString(ITaskRepository.CASE_KEY, storeString).apply()
    }

    override fun deleteTask(taskToDelete: Task) {
        caseChanged = true
        val currentTasks = cachedCase?.tasks as ArrayList
        var indexToDelete = -1
        currentTasks.forEachIndexed { index, task ->
            if (task.uuid == taskToDelete.uuid) {
                indexToDelete = index
            }
        }
        if (indexToDelete != -1) {
            currentTasks.removeAt(indexToDelete)
        }
    }

    override fun getCachedCase(): Case? {
        return cachedCase
    }

    override suspend fun uploadCase() {
        cachedCase?.let { case ->
            val caseString = Json {
                encodeDefaults = false
                serializersModule = SerializersModule {
                    // we don't want to send LocalContact to server, so we nullify it. TODO would be perfect to remove key as well
                    contextual(LocalContact::class, nullLocalContactSerializer)
                }
            }.encodeToString(case)
            userRepository.getToken()?.let { token ->
                val caseBytes = caseString.toByteArray()
                val txBytes = Base64.decode(userRepository.getTx(), IUserRepository.BASE64_FLAGS)
                val nonceBytes = ByteArray(SodiumConstants.NONCE_BYTES)
                Sodium.randombytes(nonceBytes, nonceBytes.size)
                val cipherBytes = ByteArray(caseBytes.size + Sodium.crypto_secretbox_macbytes())
                Sodium.crypto_secretbox_easy(
                    cipherBytes,
                    caseBytes,
                    caseBytes.size,
                    nonceBytes,
                    txBytes
                )
                val cipherText = Base64.encodeToString(cipherBytes, IUserRepository.BASE64_FLAGS)
                val nonceText = Base64.encodeToString(nonceBytes, IUserRepository.BASE64_FLAGS)
                val sealedCase = SealedData(cipherText, nonceText)
                val requestBody = UploadCaseBody(sealedCase)
                withContext(Dispatchers.IO) {
                    api.uploadCase(token, requestBody)
                    caseChanged = false
                }
            }
        }
    }

    override fun ifCaseWasChanged(): Boolean = caseChanged
}
