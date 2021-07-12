/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.bcocase.data.entity

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import org.joda.time.LocalDate
import java.util.*

typealias JavaSerializable = java.io.Serializable

@Serializable
data class Task(
    var canBeUploaded: Boolean = true, // used only locally
    val taskType: TaskType? = null,
    var taskContext: String? = null,
    val source: Source? = null,
    var label: String? = null, // this field comes from API, we show it only if $linkedContact.displayName is empty
    var category: Category? = null,
    var communication: CommunicationType? = null,
    var uuid: String? = null,
    var dateOfLastExposure: String? = null,
    var questionnaireResult: QuestionnaireResult? = null,
    var informedByIndexAt: String? = null,
    var linkedContact: LocalContact? = null
) : JavaSerializable {

    val didInform: Boolean
        get() = informedByIndexAt != null

    fun hasEssentialData(): Boolean {
        val hasEmailOrPhone = linkedContact?.hasValidEmailOrPhone() ?: false
        val hasNames =
            !linkedContact?.firstName.isNullOrEmpty() && !linkedContact?.lastName.isNullOrEmpty()
        val hasExposureDate = dateOfLastExposure != null
        val hasCategory = category != null
        return hasCategory && hasNames && hasEmailOrPhone && hasExposureDate
    }

    @Suppress("ReplaceSizeCheckWithIsNotEmpty")
    fun getPercentageCompletion(): Int {
        if (questionnaireResult != null && questionnaireResult?.answers != null) {
            // 1 to make sure we don't divide by 0
            val totalQuestions = questionnaireResult?.answers?.size ?: 1

            var filledInAnswers = 0
            questionnaireResult?.answers?.forEach {
                // isNotEmpty with a nullable JsonObject seems to always return false,
                // hence checking size instead
                if (it.value!!.size > 0) {
                    filledInAnswers++
                }
            }

            // Calculate percentage filled out of 100%
            val filledDouble = filledInAnswers.toDouble()
            val totalDouble = totalQuestions.toDouble()
            val percentageFilled: Double = (filledDouble / totalDouble) * 100

            return percentageFilled.toInt()
        }

        // Should never come here, return 0 to show warning
        return 0
    }

    fun getExposureDate(): LocalDate {
        return if (dateOfLastExposure != null) {
            LocalDate.parse(dateOfLastExposure, DateFormats.dateInputData)
        } else {
            LocalDate.now()
        }
    }

    fun getDisplayName(fallback: String): String {
        val contactDisplayName = linkedContact?.getDisplayName()
        var displayName: String = when {
            contactDisplayName?.isNotEmpty() == true -> contactDisplayName
            !label.isNullOrEmpty() -> label!!
            else -> fallback
        }
        if (!taskContext.isNullOrEmpty()) {
            displayName = "$displayName (${taskContext})"
        }
        return displayName
    }

    fun hasCategoryOrExposure(): Boolean = category != null || dateOfLastExposure != null

    fun isLocal(): Boolean = source == Source.App

    fun isSaved(): Boolean = questionnaireResult != null

    override fun toString(): String {
        return "Task(taskType=$taskType, taskContext=$taskContext, source=$source, label=$label, category=$category, communication=$communication, uuid=$uuid, dateOfLastExposure=$dateOfLastExposure, linkedContact=$linkedContact, questionnaireResult=$questionnaireResult)"
    }

    companion object {
        fun createAppContact(): Task = Task(
            uuid = UUID.randomUUID().toString(),
            taskType = TaskType.Contact,
            source = Source.App
        )
    }
}


