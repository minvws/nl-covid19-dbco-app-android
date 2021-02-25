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
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.data.entity.SealedData
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import timber.log.Timber

@ExperimentalSerializationApi
class TasksRepository(
    context: Context,
    private val userRepository: IUserRepository
) : ITaskRepository {

    private val api = DbcoApi.create(context)

    private var case: Case

    private var encryptedSharedPreferences: SharedPreferences =
        LocalStorageRepository.getInstance(context).getSharedPreferences()

    private var caseChanged = false

    init {
        val savedCase = encryptedSharedPreferences.getString(
            ITaskRepository.CASE_KEY,
            null
        )
        case = if (savedCase != null) {
            Defaults.json.decodeFromString(savedCase)
        } else {
            Case()
        }
    }

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

            if (case.dateOfSymptomOnset == null) {
                case = case.copy(dateOfSymptomOnset = remoteCase.dateOfSymptomOnset)
            }
            if (case.symptoms.isEmpty()) {
                case = case.copy(symptoms = remoteCase.symptoms)
            }
            if (case.windowExpiresAt == null) {
                case = case.copy(windowExpiresAt = remoteCase.windowExpiresAt)
            }
            val merged = case.tasks.toMutableList()
            remoteCase.tasks.forEach { remoteTask ->
                var found = false
                merged.forEach { currentTask ->
                    if (remoteTask.uuid == currentTask.uuid) {
                        found = true
                    }
                }
                if (!found) {
                    merged.add(remoteTask)
                }
            }
            case = case.copy(tasks = merged)
            persistCase()
        }
        return case
    }

    override fun saveChangesToTask(updatedTask: Task) {
        caseChanged = true
        val tasks = case.tasks.toMutableList()
        var found = false
        if (updatedTask.communication == null || updatedTask.communication == CommunicationType.None) {
            updatedTask.communication = CommunicationType.Index
        }
        tasks.forEachIndexed { index, currentTask ->
            if (updatedTask.uuid == currentTask.uuid || updatedTask.label!!.contentEquals(
                    currentTask.label!!
                )
            ) {
                // Only update if the new date is either later or equal to the currently stored date
                // Used for SelfBCO -> Roommates can be contacts on timeline too, but Roommate data takes priority in this case
                if (updatedTask.getExposureDateAsDateTime()
                        .isAfter(currentTask.getExposureDateAsDateTime()) || currentTask.getExposureDateAsDateTime()
                        .isEqual(updatedTask.getExposureDateAsDateTime())
                ) {
                    tasks[index] = updatedTask
                }
                found = true
            }
        }
        if (!found) {
            tasks.add(updatedTask)
        }
        case = case.copy(tasks = tasks)
        persistCase()
    }

    override fun deleteTask(taskToDelete: Task) {
        caseChanged = true
        val tasks = case.tasks.toMutableList()
        var indexToDelete = -1
        tasks.forEachIndexed { index, task ->
            if (task.uuid == taskToDelete.uuid) {
                indexToDelete = index
            }
        }
        if (indexToDelete != -1) {
            tasks.removeAt(indexToDelete)
        }
        case = case.copy(tasks = tasks)
    }

    override fun getCase(): Case = case

    override suspend fun uploadCase() {
        val caseString = Json {
            encodeDefaults = false
            serializersModule = SerializersModule {
                // we don't want to send LocalContact to server, so we nullify it. TODO would be perfect to remove key as well
                contextual(LocalContact::class, object : KSerializer<LocalContact> {
                    override val descriptor: SerialDescriptor
                        get() = LocalContact.serializer().descriptor

                    override fun deserialize(decoder: Decoder): LocalContact {
                        return LocalContact.serializer().deserialize(decoder)
                    }

                    override fun serialize(encoder: Encoder, value: LocalContact) {
                        encoder.encodeNull()
                    }
                })
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

    override fun ifCaseWasChanged(): Boolean = caseChanged

    override fun getSymptomOnsetDate(): String? = case.dateOfSymptomOnset

    override fun updateSymptomOnsetDate(dateOfSymptomOnset: String) {
        case = case.copy(dateOfSymptomOnset = dateOfSymptomOnset)
        persistCase()
    }

    override fun addSymptom(symptom: String) {
        val symptoms = case.symptoms.toMutableSet()
        symptoms.add(symptom)
        case = case.copy(symptoms = symptoms)
        persistCase()
    }

    override fun removeSymptom(symptom: String) {
        val symptoms = case.symptoms.toMutableSet()
        symptoms.remove(symptom)
        case = case.copy(symptoms = symptoms)
        persistCase()
    }

    override fun getSymptoms(): List<String> = case.symptoms.toList()

    private fun persistCase() {
        val storeString = Defaults.json.encodeToString(case)
        encryptedSharedPreferences.edit().putString(ITaskRepository.CASE_KEY, storeString).apply()
    }
}