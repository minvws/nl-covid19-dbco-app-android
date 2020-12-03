/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.applifecycle.config.FeatureFlags
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.input.ContactNameItem
import nl.rijksoverheid.dbco.items.input.DateInputItem
import nl.rijksoverheid.dbco.items.input.EmailAddressItem
import nl.rijksoverheid.dbco.items.input.NoRiskItem
import nl.rijksoverheid.dbco.items.input.PhoneNumberItem
import nl.rijksoverheid.dbco.items.input.QuestionMultipleOptionsItem
import nl.rijksoverheid.dbco.items.input.QuestionTwoOptionsItem
import nl.rijksoverheid.dbco.items.input.SingleInputItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.util.removeAllChildren
import nl.rijksoverheid.dbco.util.removeHtmlTags
import org.joda.time.Days
import org.joda.time.LocalDate
import kotlin.math.absoluteValue


/**
 * Created by Dima Kovalenko.
 */
class TaskDetailItemsStorage(
    val viewModel: TasksDetailViewModel,
    val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val featureFlags: FeatureFlags
) {

    var classificationQuestion: Question? = null

    // Classification

    val classificationSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_typeofcontact_header,
                    R.string.contact_section_typeofcontact_subtext,
                    1
            ), true
    )

    private val livedTogetherRiskItem = QuestionTwoOptionsItem(
            context,
            Question(
                    null,
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

    private val durationRiskItem = QuestionTwoOptionsItem(
            context,
            Question(
                    null,
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

    private val distanceRiskItem = QuestionTwoOptionsItem(
            context,
            Question(
                    context.getString(R.string.distance_risk_description),
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

    private val otherRiskItem = QuestionTwoOptionsItem(
            context,
            Question(
                    null,
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

    private val noRiskItem = NoRiskItem()

    fun addClassificationQuestions(
        question: Question,
        section: QuestionnaireSection?
    ) {
            classificationQuestion = question
            section?.add(livedTogetherRiskItem) // always added

            viewModel.livedTogetherRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(durationRiskItem)
                } else {
                    section?.remove(distanceRiskItem)
                    section?.remove(durationRiskItem)
                    section?.remove(otherRiskItem)
                    section?.remove(noRiskItem)
                }
            })

            viewModel.durationRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(distanceRiskItem)
                } else {
                    section?.remove(distanceRiskItem)
                    section?.remove(otherRiskItem)
                    section?.remove(noRiskItem)
                }
            })

            viewModel.distanceRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(otherRiskItem)
                } else {
                    section?.remove(otherRiskItem)
                    section?.remove(noRiskItem)
                }
            })

            viewModel.otherRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(noRiskItem)
                } else {
                    section?.remove(noRiskItem)
                }
            })

            listOf(
                livedTogetherRiskItem,
                distanceRiskItem,
                durationRiskItem,
                otherRiskItem
            ).forEach {
                it.question?.uuid = question.uuid
            }
    }

    fun getClassificationAnswerValue(): JsonObject {
        val map = HashMap<String, JsonElement> ()
        map["category1Risk"] = JsonPrimitive(viewModel.livedTogetherRisk.value ?: false)
        map["category2ARisk"] = JsonPrimitive(viewModel.durationRisk.value ?: false)
        map["category2BRisk"] = JsonPrimitive(viewModel.distanceRisk.value ?: false)
        map["category3Risk"] = JsonPrimitive(viewModel.otherRisk.value ?: false)
        return JsonObject(map)
    }

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
                                val value = date.toString(DateFormats.dateInputData)
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

    fun refreshContactDetailsSection() {
        contactDetailsSection.removeAllChildren()
        var communicationTypeQuestionFound = false
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            if (question.group == Group.ContactDetails && question.isRelevantForCategory(viewModel.category.value)) {
                // add hardcoded "date of last exposure" question before communication type question
                if (isCommunicationTypeQuestion(question)) {
                    let {
                        it.contactDetailsSection.add(it.dateOfLastExposureItem)
                        communicationTypeQuestionFound = true
                    }
                }
                when (question.questionType) {
                    QuestionType.Multiplechoice -> {
                        addMultiChoiceItem(question, contactDetailsSection)
                    }
                    QuestionType.Open -> {
                        contactDetailsSection.add(
                            SingleInputItem(
                                context,
                                question,
                                viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                            )
                        )
                    }
                    QuestionType.Date -> {
                        contactDetailsSection.add(
                            DateInputItem(
                                context,
                                question,
                                viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                            )
                        )
                    }
                    QuestionType.ContactDetails -> {
                        addContactDetailsItems(contactDetailsSection, question)
                    }
                }
            }
        }
        if (!communicationTypeQuestionFound) { // fallback, shouldn't happen
            let {
                contactDetailsSection?.add(it.dateOfLastExposureItem)
            }
        }
    }

    private fun addMultiChoiceItem(
        question: Question,
        sectionToAddTo: QuestionnaireSection?
    ) {
        question.answerOptions?.size?.let { size ->
            when {
                size > 2 -> {
                    sectionToAddTo?.add(
                        QuestionMultipleOptionsItem(
                            context,
                            question,
                            {},
                            viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                        )
                    )
                }
                size == 2 -> {
                    var previousAnswerValue =
                        viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                    if (isCommunicationTypeQuestion(question)) {
                        // if it is communication type question - we override previous answer so we can set communicationType from viewmodel
                        previousAnswerValue = JsonObject(
                            HashMap<String, JsonElement>().apply {
                                val trigger = when (viewModel.communicationType.value) {
                                    CommunicationType.Index -> ContactDetailsInputFragment.COMMUNICATION_INDEX
                                    CommunicationType.Staff -> ContactDetailsInputFragment.COMMUNICATION_STUFF
                                    else -> null
                                }
                                trigger?.let {
                                    put("trigger", JsonPrimitive(it))
                                }
                            }
                        )
                    }
                    sectionToAddTo?.add(
                        QuestionTwoOptionsItem(
                            context,
                            question,
                            {
                                when (it.trigger) {
                                    ContactDetailsInputFragment.COMMUNICATION_STUFF -> viewModel.communicationType.value =
                                        CommunicationType.Staff
                                    ContactDetailsInputFragment.COMMUNICATION_INDEX -> viewModel.communicationType.value =
                                        CommunicationType.Index
                                }
                            },
                            previousAnswerValue
                        )
                    )
                }
                else -> {
                }
            }
        }
    }

    private fun addContactDetailsItems(
        sectionToAddTo: QuestionnaireSection?,
        question: Question
    ) {
        sectionToAddTo?.addAll(
            listOf(
                ContactNameItem(
                    viewModel.selectedContact?.firstName,
                    viewModel.selectedContact?.lastName,
                    question
                ) { newFirstName, newLastName ->
                    viewModel.selectedContact?.firstName = newFirstName
                    viewModel.selectedContact?.lastName = newLastName
                },
                PhoneNumberItem(viewModel.selectedContact?.number, question) {
                    viewModel.selectedContact?.number = it
                    viewModel.hasEmailOrPhone.value =
                        viewModel.selectedContact?.hasValidEmailOrPhone()
                },
                EmailAddressItem(viewModel.selectedContact?.email, question) {
                    viewModel.selectedContact?.email = it
                    viewModel.hasEmailOrPhone.value =
                        viewModel.selectedContact?.hasValidEmailOrPhone()
                }
            )
        )
    }

    // Inform

    val informSection = QuestionnaireSection(
            QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
            ), false
    )

    fun refreshInformSection() {

        val isEnabled = when (viewModel.category.value) {
            Category.LIVED_TOGETHER, Category.DURATION, Category.DISTANCE -> viewModel.communicationType.value != null
            Category.OTHER -> true
            else -> false  // in those cases inform section is disabled and thus hidden
        }

        val header = when(viewModel.communicationType.value) {
            CommunicationType.Staff -> context.getString(R.string.inform_contact_title_staff, viewModel.selectedContact?.firstName)
            else -> context.getString(R.string.inform_contact_title_index, viewModel.selectedContact?.firstName)
        }

        var message = when (viewModel.category.value) {
            Category.LIVED_TOGETHER -> context.getString(R.string.inform_contact_guidelines_category1)
            Category.OTHER -> context.getString(R.string.inform_contact_guidelines_category3)
            Category.DURATION, Category.DISTANCE -> {
                val dateLastExposure = viewModel.dateOfLastExposure.value
                if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
                    context.getString(R.string.inform_contact_guidelines_category2, "", "")
                } else {
                    val date = LocalDate.parse(dateLastExposure, DateFormats.dateInputData)
                    val untilDate = date.plusDays(10)
                    val untilDateString = context.getString(R.string.inform_contact_guidelines_category2_until_date, untilDate.toString(DateFormats.informContactGuidelinesUI))

                    val daysRemaining = Days.daysBetween(LocalDate.now(), untilDate).days.absoluteValue

                    val daysRemainingString = when (daysRemaining) {
                        1 -> context.getString(R.string.inform_contact_guidelines_category2_day_remaining)
                        else -> context.getString(R.string.inform_contact_guidelines_category2_days_remaining, daysRemaining.toString())
                    }

                    context.getString(
                        R.string.inform_contact_guidelines_category2,
                        untilDateString,
                        daysRemainingString
                    )
                }
            }
            else -> ""
        }

        val link = when (viewModel.category.value) {
            Category.LIVED_TOGETHER -> context.getString(R.string.inform_contact_link_category1)
            Category.DURATION -> context.getString(R.string.inform_contact_link_category2a)
            Category.DISTANCE -> context.getString(R.string.inform_contact_link_category2b)
            Category.OTHER -> context.getString(R.string.inform_contact_link_category3)
            else -> ""
        }

        message += "<br/>$link"

        val plainMessage = message.removeHtmlTags()

        informSection.apply {

            if (!isExpanded) {
                onToggleExpanded()
            }

            removeAllChildren()

            setEnabled(isEnabled)
            if (!isEnabled) {
                return
            }

            add(SubHeaderItem(header))
            add(ParagraphItem(message, clickable = true))
            if(featureFlags.enablePerspectiveCopy) {
                add(ButtonItem(
                    context.getString(R.string.contact_section_inform_copy),
                    {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newHtmlText("Copied Text", plainMessage, message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            context.getString(R.string.contact_section_inform_copied),
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    type = ButtonType.LIGHT
                ))
            }

            // add "Call $name" button if phone is set
            if(featureFlags.enableContactCalling) {
                viewModel.selectedContact?.number?.let {
                    add(
                        ButtonItem(
                            context.getString(
                                R.string.contact_section_inform_call,
                                viewModel.selectedContact?.firstName ?: "contact"
                            ),
                            {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it}"))
                                context.startActivity(intent)
                            },
                            type = if (viewModel.communicationType.value == CommunicationType.Index) {
                                ButtonType.DARK
                            } else {
                                ButtonType.LIGHT
                            }
                        )
                    )
                }
            }
        }
    }

    // Misc

    private fun isCommunicationTypeQuestion(question: Question): Boolean {
        var foundTrigger = false
        question.answerOptions?.forEach {
            if (it?.trigger == ContactDetailsInputFragment.COMMUNICATION_STUFF || it?.trigger == ContactDetailsInputFragment.COMMUNICATION_INDEX) {
                foundTrigger = true
            }
        }
        return foundTrigger
    }

    companion object {
        const val ANSWER_EARLIER = "earlier"
    }
}