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
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult

@Serializable
@Parcelize
class Task(
    val taskType: String? = null,
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
    var status: Int? = 0 // number from 0 to 3

    @IgnoredOnParcel
    var didInform = false

    @IgnoredOnParcel
    @Contextual
    var linkedContact: LocalContact? = null

    override fun toString(): String {
        return "Task(taskType=$taskType, taskContext=$taskContext, source=$source, label=$label, category=$category, communication=$communication, uuid=$uuid, dateOfLastExposure=$dateOfLastExposure, linkedContact=$linkedContact, questionnaireResult=$questionnaireResult)"
    }
}


