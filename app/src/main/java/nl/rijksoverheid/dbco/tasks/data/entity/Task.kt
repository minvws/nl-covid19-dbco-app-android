/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult

@Serializable
@Parcelize
class Task(
    val taskType: String? = null,
    var taskContext: String? = null,
    val source: String? = null,
    var label: String? = null,
    var category: Category? = null,
    val communication: CommunicationType? = null,
    var uuid: String? = null,
    var dateOfLastExposure: String? = null,
    var linkedContact: LocalContact? = null,
) : Parcelable {
    @IgnoredOnParcel
    var questionnaireResult: QuestionnaireResult? = null
    var status: Int? = 0 // number from 0 to 3
    var contactIsInformedAlready = false

    override fun toString(): String {
        return "Task(taskType=$taskType, taskContext=$taskContext, source=$source, label=$label, category=$category, communication=$communication, uuid=$uuid, dateOfLastExposure=$dateOfLastExposure, linkedContact=$linkedContact, questionnaireResult=$questionnaireResult)"
    }
}

@Serializable
@Keep
enum class CommunicationType {
    @SerialName("index")
    Index,

    @SerialName("staff")
    Staff,

    @SerialName("none")
    None
}



