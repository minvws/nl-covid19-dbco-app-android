/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import nl.rijksoverheid.dbco.user.data.entity.SealedData
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants


class TasksRepository(context: Context, private val userRepository: IUserRepository) :
    ITaskRepository {
    private val api = StubbedAPI.create(context)
    private var cachedCase: Case? = null

    override suspend fun fetchCase(): Case? {
        if (cachedCase == null) {
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
                cachedCase = Json.decodeFromString(caseString)
            }
        }
        return cachedCase
    }

    override fun saveChangesToTask(updatedTask: Task) {
        val currentTasks = cachedCase?.tasks as ArrayList
        currentTasks.forEachIndexed { index, currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                currentTasks[index] = updatedTask
            }
        }
    }

    override fun getCachedCase(): Case? {
        return cachedCase
    }

    override suspend fun uploadCase() {
        cachedCase?.let { case ->
            val caseString = ITaskRepository.JSON_SERIALIZER.encodeToString(case)
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
                withContext(Dispatchers.IO) { api.uploadCase(token, requestBody) }
            }
        }
    }
}
