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
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.items.input.QuestionMultipleOptionsItem
import nl.rijksoverheid.dbco.items.input.QuestionTwoOptionsItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import org.joda.time.Days
import org.joda.time.LocalDate


/**
 * Created by Dima Kovalenko.
 */
class TaskDetailItemsStorage(viewModel: TasksDetailViewModel, context: Context) {

    // Classification

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
            LIVED_TOGETHER_RISK_LABEL,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.livedTogetherRisk.value) {
                            true -> put(LIVED_TOGETHER_RISK_LABEL, JsonPrimitive(ANSWER_TRUE))
                            false -> put(LIVED_TOGETHER_RISK_LABEL, JsonPrimitive(ANSWER_FALSE))
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
            DURATION_RISK_LABEL,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.durationRisk.value) {
                            true -> put(DURATION_RISK_LABEL, JsonPrimitive(ANSWER_TRUE))
                            false -> put(DURATION_RISK_LABEL, JsonPrimitive(ANSWER_FALSE))
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
            DISTANCE_RISK_LABEL,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.distanceRisk.value) {
                            true -> put(DISTANCE_RISK_LABEL, JsonPrimitive(ANSWER_TRUE))
                            false -> put(DISTANCE_RISK_LABEL, JsonPrimitive(ANSWER_FALSE))
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
            OTHER_RISK_LABEL,
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        when (viewModel.otherRisk.value) {
                            true -> put(OTHER_RISK_LABEL, JsonPrimitive(ANSWER_TRUE))
                            false -> put(OTHER_RISK_LABEL, JsonPrimitive(ANSWER_FALSE))
                        }
                    }
            )
    )

    // Contact details

    val contactDetailsSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_contactdetails_header,
                    R.string.contact_section_contactdetails_subtext,
                    2
            ), false
    )

    val dateOfLastExposureItem = QuestionMultipleOptionsItem(
            context,
            Question(
                    null,
                    null,
                    "",
                    context.getString(R.string.contact_information_last_exposure_label),
                    QuestionType.Multiplechoice,
                    Group.ContactDetails,
                    mutableListOf<AnswerOption>().apply {
                        viewModel.getDateOfSymptomOnset()?.let {

                            add(AnswerOption(context.getString(R.string.contact_information_last_exposure_earlier), null, ANSWER_EARLIER))

                            val twoDaysBeforeSymptoms = it.minusDays(2)
                            val interval = Days.daysBetween(twoDaysBeforeSymptoms, LocalDate.now()).days
                            for (i in 0 .. interval) {
                                val date = twoDaysBeforeSymptoms.plusDays(i)
                                val label = date.toString(DateFormats.exposureUI)
                                val value = date.toString(DateFormats.exposureData)
                                add(AnswerOption(label, null, value))
                            }
                        }
                    }
            ),
            {
                viewModel.dateOfLastExposure.value = it.value
            },
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        put(LAST_EXPOSURE, JsonPrimitive(viewModel.dateOfLastExposure.value))
                    }
            )
    )

    // Inform

    val informSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
            ), false
    )

    companion object {
        //        Classification
        const val LIVED_TOGETHER_RISK_LABEL = "livedTogetherRisk"
        const val DISTANCE_RISK_LABEL = "distanceRisk"
        const val DURATION_RISK_LABEL = "durationRisk"
        const val OTHER_RISK_LABEL = "otherRisk"

        const val ANSWER_FALSE = "false"
        const val ANSWER_TRUE = "true"

        //        Contact details
        const val LAST_EXPOSURE = "lastExposure"

        const val ANSWER_EARLIER = "earlier"
    }
}