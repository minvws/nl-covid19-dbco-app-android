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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.network.request.CaseRequest
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TaskType
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.data.entity.SealedData
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import org.joda.time.LocalDate
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import java.util.*

@ExperimentalSerializationApi
class TasksRepository(
    context: Context,
    private val userRepository: IUserRepository
) : ITaskRepository {

    private val api = DbcoApi.create(context)

    private val _case: Case
        get() {
            val savedCase = encryptedSharedPreferences.getString(
                ITaskRepository.CASE_KEY,
                null
            )
            return if (savedCase != null) {
                Defaults.json.decodeFromString(savedCase)
            } else {
                Case()
            }
        }

    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    override suspend fun fetchCase(): Case {
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

            val old = _case

            var new = old.copy(
                reference = remoteCase.reference,
                contagiousPeriodKnown = remoteCase.contagiousPeriodKnown
            )

            if (old.dateOfTest == null) {
                new = new.copy(dateOfTest = remoteCase.dateOfTest)
            }
            if (old.dateOfSymptomOnset == null) {
                new = new.copy(dateOfSymptomOnset = remoteCase.dateOfSymptomOnset)
            }
            if (old.symptoms.isEmpty()) {
                new = new.copy(symptoms = remoteCase.symptoms)
            }
            if (old.windowExpiresAt == null) {
                new = new.copy(windowExpiresAt = remoteCase.windowExpiresAt)
            }
            val mergedTasks = old.tasks.toMutableList()
            remoteCase.tasks.forEach { remoteTask ->
                var found = false
                mergedTasks.forEach { currentTask ->
                    if (remoteTask.uuid == currentTask.uuid) {
                        found = true
                    }
                }
                if (!found) {
                    mergedTasks.add(remoteTask)
                }
            }
            new = new.copy(tasks = mergedTasks)
            persistCase(new)
        }
        return _case
    }

    override fun getCaseReference(): String? = _case.reference

    override fun saveTask(task: Task, shouldMerge: (Task) -> Boolean) {
        val old = _case
        val tasks = old.tasks.toMutableList()
        var found = false
        var canCaseBeUploaded = old.canBeUploaded
        if (task.communication == null) {
            task.communication = CommunicationType.None
        }
        if (task.uuid.isNullOrEmpty()) {
            task.uuid = UUID.randomUUID().toString()
        }
        tasks.forEachIndexed { index, currentTask ->
            if (shouldMerge(currentTask)) {
                // Only update if the new date is either later or equal to the currently stored date
                // Used for SelfBCO -> Roommates can be contacts on timeline too, but Roommate data takes priority in this case
                if (task != currentTask && (task.getExposureDate()
                        .isAfter(currentTask.getExposureDate()) ||
                            currentTask.getExposureDate().isEqual(task.getExposureDate()) ||
                            currentTask.dateOfLastExposure == null)
                ) {
                    tasks[index] = task.apply {
                        canBeUploaded = true
                    }
                    canCaseBeUploaded = true
                }
                found = true
            }
        }
        if (!found) {
            tasks.add(task)
        }
        val new = old.copy(tasks = tasks, canBeUploaded = canCaseBeUploaded)
        persistCase(new)
    }

    override fun getContactsByCategory(category: Category): List<Task> {
        return _case.tasks.filter { task ->
            task.category == category && task.taskType == TaskType.Contact
        }
    }

    override fun deleteTask(uuid: String) {
        val old = _case
        val tasks = old.tasks.toMutableList()
        var indexToDelete = -1
        tasks.forEachIndexed { index, task ->
            if (task.uuid == uuid) {
                indexToDelete = index
            }
        }
        if (indexToDelete != -1) {
            tasks.removeAt(indexToDelete)
        }
        val new = old.copy(tasks = tasks, canBeUploaded = true)
        persistCase(new)
    }

    override fun getCase(): Case = _case

    override suspend fun uploadCase() {
        val caseString = Json { encodeDefaults = true }.encodeToString(CaseRequest.fromCase(_case))
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
                markCaseAsUploaded()
            }
        }
    }

    private fun markCaseAsUploaded() {
        val old = _case
        val new = old.copy(
            canBeUploaded = false,
            isUploaded = true,
            tasks = old.tasks.map { it.apply { canBeUploaded = false } }
        )
        persistCase(new)
    }

    override fun getSymptomOnsetDate(): String? = _case.dateOfSymptomOnset

    override fun getTestDate(): String? = _case.dateOfTest

    override fun getStartOfContagiousPeriod(): LocalDate? {
        val case = _case
        return if (case.dateOfSymptomOnset == null && case.dateOfTest == null) {
            null
        } else {
            case.dateOfSymptomOnset?.let {
                LocalDate.parse(it, DateFormats.dateInputData).minusDays(2)
            } ?: LocalDate.parse(case.dateOfTest, DateFormats.dateInputData)
        }
    }

    override fun updateSymptomOnsetDate(dateOfSymptomOnset: String) {
        val new = _case.copy(dateOfSymptomOnset = dateOfSymptomOnset)
        persistCase(new)
    }

    override fun updateTestDate(testDate: String) {
        val new = _case.copy(dateOfTest = testDate)
        persistCase(new)
    }

    override fun addSymptom(symptom: String) {
        val old = _case
        val symptoms = old.symptoms.toMutableSet()
        symptoms.add(symptom)
        val new = old.copy(symptoms = symptoms)
        persistCase(new)
    }

    override fun removeSymptom(symptom: String) {
        val old = _case
        val symptoms = _case.symptoms.toMutableSet()
        symptoms.remove(symptom)
        val new = old.copy(symptoms = symptoms)
        persistCase(new)
    }

    override fun getSymptoms(): List<String> = _case.symptoms.toList()

    private fun persistCase(case: Case) {
        val storeString = Defaults.json.encodeToString(case)
        encryptedSharedPreferences.edit().putString(ITaskRepository.CASE_KEY, storeString).apply()
    }
}