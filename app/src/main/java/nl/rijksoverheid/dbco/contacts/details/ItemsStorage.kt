/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.content.Context
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.items.input.QuestionTwoOptionsItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel


/**
 * Created by Dima Kovalenko.
 */
class ItemsStorage(viewModel: TasksDetailViewModel, context: Context) {

    val classificationSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_typeofcontact_header,
                    R.string.contact_section_typeofcontact_subtext,
                    1
            ), true
    )

    val livedTogetherRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    context.getString(R.string.lived_together_risk_label),
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption(context.getString(R.string.answer_no), null, ANSWER_FALSE),
                            AnswerOption(context.getString(R.string.answer_yes), null, ANSWER_TRUE)
                    )
            ),
            {
                when (it.value) {
                    ANSWER_FALSE -> viewModel.livedTogetherRisk.value = false
                    ANSWER_TRUE -> viewModel.livedTogetherRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            livedTogetherRiskLabel,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.livedTogetherRisk.value) {
                            true -> put(livedTogetherRiskLabel, JsonPrimitive(ANSWER_TRUE))
                            false -> put(livedTogetherRiskLabel, JsonPrimitive(ANSWER_FALSE))
                        }
                    }
            )
    )

    val durationRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    context.getString(R.string.duration_risk_label),
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption(context.getString(R.string.answer_think_yes), null, ANSWER_TRUE),
                            AnswerOption(context.getString(R.string.answer_think_no), null, ANSWER_FALSE)
                    )
            ),
            {
                when (it.value) {
                    ANSWER_FALSE -> viewModel.durationRisk.value = false
                    ANSWER_TRUE -> viewModel.durationRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            durationRiskLabel,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.durationRisk.value) {
                            true -> put(durationRiskLabel, JsonPrimitive(ANSWER_TRUE))
                            false -> put(durationRiskLabel, JsonPrimitive(ANSWER_FALSE))
                        }
                    }
            )
    )

    val distanceRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    context.getString(R.string.distance_risk_description),
                    "",
                    context.getString(R.string.distance_risk_label),
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption(context.getString(R.string.answer_think_yes), null, ANSWER_TRUE),
                            AnswerOption(context.getString(R.string.answer_think_no), null, ANSWER_FALSE)
                    )
            ),
            {
                when (it.value) {
                    ANSWER_FALSE -> viewModel.distanceRisk.value = false
                    ANSWER_TRUE -> viewModel.distanceRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            distanceRiskLabel,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.distanceRisk.value) {
                            true -> put(distanceRiskLabel, JsonPrimitive(ANSWER_TRUE))
                            false -> put(distanceRiskLabel, JsonPrimitive(ANSWER_FALSE))
                        }
                    }
            )
    )

    val otherRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    context.getString(R.string.other_risk_label),
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption(context.getString(R.string.answer_think_yes), null, ANSWER_TRUE),
                            AnswerOption(context.getString(R.string.answer_think_no), null, ANSWER_FALSE)
                    )
            ),
            {
                when (it.value) {
                    ANSWER_FALSE -> viewModel.otherRisk.value = false
                    ANSWER_TRUE -> viewModel.otherRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            otherRiskLabel,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.otherRisk.value) {
                            true -> put(otherRiskLabel, JsonPrimitive(ANSWER_TRUE))
                            false -> put(otherRiskLabel, JsonPrimitive(ANSWER_FALSE))
                        }
                    }
            )
    )

    val contactDetailsSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_contactdetails_header,
                    R.string.contact_section_contactdetails_subtext,
                    2
            ), false
    )

    val informSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
            ), false
    )

    companion object {
        const val livedTogetherRiskLabel = "livedTogetherRisk"
        const val distanceRiskLabel = "distanceRisk"
        const val durationRiskLabel = "durationRisk"
        const val otherRiskLabel = "otherRisk"

        const val ANSWER_FALSE = "false"
        const val ANSWER_TRUE = "true"
    }
}