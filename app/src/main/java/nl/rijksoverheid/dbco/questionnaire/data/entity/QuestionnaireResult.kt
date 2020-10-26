/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnaire.data.entity


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
@Parcelize
class QuestionnaireResult(
    val questionnaireUuid: String,
    var answers: ArrayList<@RawValue JsonObject>
) : Parcelable {

    override fun toString(): String {
        return "QuestionnaireResult(questionnaireUuid='$questionnaireUuid', answers=${answers.size})"
    }
}