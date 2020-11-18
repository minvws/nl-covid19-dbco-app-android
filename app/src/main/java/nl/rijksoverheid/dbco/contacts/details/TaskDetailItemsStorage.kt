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
import nl.rijksoverheid.dbco.items.input.NoRiskItem
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
                            AnswerOption(context.getString(R.string.answer_no), null, false.toString()),
                            AnswerOption(context.getString(R.string.answer_yes), null, true.toString())
                    )
            ),
            {
                when (it.value) {
                    false.toString() -> viewModel.livedTogetherRisk.value = false
                    true.toString() -> viewModel.livedTogetherRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        put("value", JsonPrimitive(viewModel.livedTogetherRisk.value))
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, true.toString()),
                            AnswerOption(context.getString(R.string.answer_think_no), null, false.toString())
                    )
            ),
            {
                when (it.value) {
                    false.toString() -> viewModel.durationRisk.value = false
                    true.toString() -> viewModel.durationRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        put("value", JsonPrimitive(viewModel.durationRisk.value))
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, true.toString()),
                            AnswerOption(context.getString(R.string.answer_think_no), null, false.toString())
                    )
            ),
            {
                when (it.value) {
                    false.toString() -> viewModel.distanceRisk.value = false
                    true.toString() -> viewModel.distanceRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        put("value", JsonPrimitive(viewModel.distanceRisk.value))
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, true.toString()),
                            AnswerOption(context.getString(R.string.answer_think_no), null, false.toString())
                    )
            ),
            {
                when (it.value) {
                    false.toString() -> viewModel.otherRisk.value = false
                    true.toString() -> viewModel.otherRisk.value = true
                }
                viewModel.updateCategoryFromRiskFlags()
            },
            JsonObject(
                    HashMap<String, JsonElement>().apply {
                        put("value", JsonPrimitive(viewModel.otherRisk.value))
                    }
            )
    )

    val noRiskItem = NoRiskItem()

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
                        put("value", JsonPrimitive(viewModel.dateOfLastExposure.value))
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
        const val ANSWER_EARLIER = "earlier"
    }
}