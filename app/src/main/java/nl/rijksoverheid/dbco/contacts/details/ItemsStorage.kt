/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.content.Context
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
                            AnswerOption(context.getString(R.string.answer_no), null, "false"),
                            AnswerOption(context.getString(R.string.answer_yes), null, "true")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.livedTogetherRisk.value = false
                    "true" -> viewModel.livedTogetherRisk.value = true
                }
                viewModel.updateCategory()
            },
            "livedTogetherRisk",
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, "true"),
                            AnswerOption(context.getString(R.string.answer_think_no), null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.durationRisk.value = false
                    "true" -> viewModel.durationRisk.value = true
                }
                viewModel.updateCategory()
            },
            "durationRisk",
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, "true"),
                            AnswerOption(context.getString(R.string.answer_think_no), null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.distanceRisk.value = false
                    "true" -> viewModel.distanceRisk.value = true
                }
                viewModel.updateCategory()
            },
            "distanceRisk",
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
                            AnswerOption(context.getString(R.string.answer_think_yes), null, "true"),
                            AnswerOption(context.getString(R.string.answer_think_no), null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.otherRisk.value = false
                    "true" -> viewModel.otherRisk.value = true
                }
                viewModel.updateCategory()
            },
            "otherRisk",
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
}