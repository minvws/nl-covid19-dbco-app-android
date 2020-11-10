/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import androidx.lifecycle.LifecycleOwner
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
class ItemsStorage(viewModel: TasksDetailViewModel, viewLifecycleOwner: LifecycleOwner) {

    val classificationSection = QuestionnaireSection(
            viewLifecycleOwner,
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
                    "Woon je in hetzelfde huis of ben je langer dan 12 uur op dezelfde plek geweest?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(AnswerOption("Nee", null, "false"), AnswerOption("Ja", null, "true"))
            ),
            {
                when (it.value) {
                    "false" -> viewModel.livedTogetherRisk.value = false
                    "true" -> viewModel.livedTogetherRisk.value = true
                }
            },
            "livedTogetherRisk",
    )

    val durationRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    "Was je langer dan 15 minuten op minder dan 1,5 meter afstand van elkaar?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.durationRisk.value = false
                    "true" -> viewModel.durationRisk.value = true
                }
            },
            "durationRisk",
    )

    val distanceRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    "<ul><li>Binnen anderhalve meter van de ander gehoest of geniesd</li><li>Geknuffeld of gezoend</li><li>Ander lichamelijk contact</li><ul>",
                    "",
                    "Heb je een of meerdere van deze dingen tijdens jullie ontmoeting gedaan?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.distanceRisk.value = false
                    "true" -> viewModel.distanceRisk.value = true
                }
            },
            "distanceRisk",
    )

    val otherRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    "Was je langer dan 15 minuten in dezelfde ruimte?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                // TODO add "no risk" label
            },
            "otherRisk",
    )

    val contactDetailsSection = QuestionnaireSection(
            viewLifecycleOwner,
            QuestionnaireSectionHeader(
                    R.string.contact_section_contactdetails_header,
                    R.string.contact_section_contactdetails_subtext,
                    2
            ), false
    )

    val informSection = QuestionnaireSection(
            viewLifecycleOwner,
            QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
            ), false
    )
}