/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import org.joda.time.DateTime

@Serializable
@Parcelize
class Task(
    val taskType: TaskType? = null,
    var taskContext: String? = null,
    val source: Source? = null,
    var label: String? = null, // this field comes from API, we show it only if $linkedContact.displayName is empty
    var category: Category? = null,
    var communication: CommunicationType? = null,
    var uuid: String? = null,
    var dateOfLastExposure: String? = null,
) : Parcelable {

    @IgnoredOnParcel
    var questionnaireResult: QuestionnaireResult? = null

    @IgnoredOnParcel
    var didInform = false

    @IgnoredOnParcel
    @Contextual
    var linkedContact: LocalContact? = null

    fun getStatus(): Int {
        // check for essential data first, if any of these are missing always return 0
        val hasEmailOrPhone = linkedContact?.hasValidEmailOrPhone() ?: false
        if(category == null || (linkedContact?.firstName.isNullOrEmpty() && linkedContact?.lastName.isNullOrEmpty()) || !hasEmailOrPhone || dateOfLastExposure == null ){
            return 0
        }

        if(questionnaireResult != null && questionnaireResult?.answers != null){
            val totalQuestions = questionnaireResult?.answers?.size ?: 1 // 1 to make sure we don't divide by 0
            var filledInAnswers = 0
            questionnaireResult?.answers?.forEach {
                if(it.value!!.size > 0) { // isNotEmpty with a nullable JsonObject seems to always return false, hence checking size instead
                   filledInAnswers++
                }
            }

            // Calculate percentage filled out of 100%
            val filledDouble = filledInAnswers.toDouble()
            val totalDouble = totalQuestions.toDouble()
            val percentageFilled : Double = (filledDouble / totalDouble) * 100

            return percentageFilled.toInt()
        }

        // Should never come here, return 0 to show warning
        return 0
    }

    fun getExposureDateAsDateTime() : DateTime{
        return if(dateOfLastExposure != null){
            DateTime.parse(dateOfLastExposure, DateFormats.dateInputData).withTimeAtStartOfDay()
        }else{
            DateTime.now().withTimeAtStartOfDay()
        }

    }

    override fun toString(): String {
        return "Task(taskType=$taskType, taskContext=$taskContext, source=$source, label=$label, category=$category, communication=$communication, uuid=$uuid, dateOfLastExposure=$dateOfLastExposure, linkedContact=$linkedContact, questionnaireResult=$questionnaireResult)"
    }
}


