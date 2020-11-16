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
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.CaseBody
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository
import org.libsodium.jni.Sodium
import kotlin.collections.ArrayList

class TasksRepository(context: Context, private val userRepository: IUserRepository) : ITaskRepository {
    private val api = StubbedAPI.create(context)
    private var cachedCase: Case? = null

    override suspend fun fetchCase(): Case? {
        if (cachedCase == null) {
            userRepository.getToken()?.let {
                val data = withContext(Dispatchers.IO) { api.getCase(it) }
                val sealedCaseBodyString = data.body()?.sealedCase
                val sealedCaseBodyBytes = Base64.decode(sealedCaseBodyString, IUserRepository.BASE64_FLAGS)
                val nonceBytes = Base64.decode(data.body()?.nonce, IUserRepository.BASE64_FLAGS)
                val rxBytes = Base64.decode(userRepository.getRx(), IUserRepository.BASE64_FLAGS)
                val caseBodyBytes = ByteArray(Sodium.crypto_box_macbytes() + sealedCaseBodyBytes.size)
                Sodium.crypto_secretbox_open_easy(caseBodyBytes, sealedCaseBodyBytes, sealedCaseBodyBytes.size, nonceBytes, rxBytes)
                val caseBodyString = Base64.encodeToString(caseBodyBytes, IUserRepository.BASE64_FLAGS)
                val caseBody: CaseBody = Json.decodeFromString(caseBodyString)
                cachedCase = caseBody.case
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
}
