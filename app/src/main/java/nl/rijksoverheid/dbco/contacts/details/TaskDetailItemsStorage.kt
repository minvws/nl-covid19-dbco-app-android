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
import nl.rijksoverheid.dbco.items.input.*
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
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.util.removeAllChildren
import nl.rijksoverheid.dbco.util.removeHtmlTags
import org.joda.time.Days
import org.joda.time.LocalDate


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

    private val sameHouseholdRiskItem = QuestionTwoOptionsItem(
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
            viewModel.sameHouseholdRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.sameHouseholdRisk.value))
            }
        ),
        isLocked = viewModel.task.value?.source == Source.Portal
    )

    private val distanceRiskItem = QuestionThreeOptionsItem(
        context,
        Question(
            null,
            context.getString(R.string.distance_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(
                    context.getString(R.string.distance_risk_answer_long),
                    null,
                    "true, true"
                ),
                AnswerOption(
                    context.getString(R.string.distance_risk_answer_short),
                    null,
                    "true, false"
                ),
                AnswerOption(context.getString(R.string.distance_risk_answer_no), null, "false, false")
            )
        ),
        {
            when (it.value) {
                "true, true" -> viewModel.distanceRisk.value = Pair(first = true, second = true)
                "true, false" -> viewModel.distanceRisk.value = Pair(first = true, second = false)
                else -> viewModel.distanceRisk.value = Pair(first = false, second = false)
            }
            viewModel.updateCategoryFromRiskFlags()
        },
        JsonObject(
            HashMap<String, JsonElement>().apply {
                put(
                    "value", JsonPrimitive(
                        "${viewModel.distanceRisk.value?.first}, ${viewModel.distanceRisk.value?.second}"
                    )
                )
            }
        ),
        isLocked = viewModel.task.value?.source == Source.Portal
    )

    private val physicalContactRiskItem = QuestionTwoOptionsItem(
        context,
        Question(
            context.getString(R.string.physical_risk_description),
            context.getString(R.string.physical_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(context.getString(R.string.answer_think_yes), null, true.toString()),
                AnswerOption(context.getString(R.string.answer_know_no), null, false.toString())
            )
        ),
        {
            viewModel.physicalContactRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.physicalContactRisk.value))
            }
        ),
        isLocked = viewModel.task.value?.source == Source.Portal
    )

    private val sameRoomRiskItem = QuestionTwoOptionsItem(
        context,
        Question(
            null,
            context.getString(R.string.same_room_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(context.getString(R.string.answer_think_yes), null, true.toString()),
                AnswerOption(context.getString(R.string.answer_think_no), null, false.toString())
            )
        ),
        {
            viewModel.sameRoomRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.sameRoomRisk.value))
            }
        )
    )

    private val noRiskItem = NoRiskItem()
    private val noExposureRiskItem = NoExposureRiskItem()

    fun addClassificationQuestions(
        question: Question,
        section: QuestionnaireSection?
    ) {
        classificationQuestion = question
        section?.add(sameHouseholdRiskItem) // always added

        viewModel.sameHouseholdRisk.observe(viewLifecycleOwner, {
            if (it == false) {
                if (section?.getPosition(distanceRiskItem) == -1) {
                    section.add(distanceRiskItem)
                }
            } else {
                section?.remove(distanceRiskItem.apply { clearPreviousAnswer() })
                section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
                section?.remove(sameRoomRiskItem.apply { clearPreviousAnswer() })
                section?.remove(noRiskItem)
            }
        })

        viewModel.distanceRisk.observe(viewLifecycleOwner, {
            if (it != null && it.first == false) {
                section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
                if (section?.getPosition(sameRoomRiskItem) == -1) {
                    section.add(sameRoomRiskItem)
                }
            } else if (it != null && it.second == false) {
                section?.remove(sameRoomRiskItem.apply { clearPreviousAnswer() })
                section?.remove(noRiskItem)
                if (section?.getPosition(physicalContactRiskItem) == -1) {
                    section.add(physicalContactRiskItem)
                }
            } else {
                section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
                section?.remove(sameRoomRiskItem.apply { clearPreviousAnswer() })
                section?.remove(noRiskItem)
            }
        })

        viewModel.physicalContactRisk.observe(viewLifecycleOwner, {
            section?.remove(noRiskItem)
        })

        viewModel.sameRoomRisk.observe(viewLifecycleOwner, {
            if (it == false) {
                if (section?.getPosition(noRiskItem) == -1) {
                    section.add(noRiskItem)
                }
            } else {
                section?.remove(noRiskItem)
            }
        })

        listOf(
            sameHouseholdRiskItem,
            distanceRiskItem,
            physicalContactRiskItem,
            sameRoomRiskItem
        ).forEach {
            it.question?.uuid = question.uuid
        }
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

                    add(
                        AnswerOption(
                            context.getString(R.string.contact_information_last_exposure_earlier),
                            null,
                            ANSWER_EARLIER
                        )
                    )

                    val twoDaysBeforeSymptoms = it.minusDays(2)
                    val interval = Days.daysBetween(twoDaysBeforeSymptoms, LocalDate.now()).days
                    for (i in 0..interval) {
                        val date = twoDaysBeforeSymptoms.plusDays(i)
                        val label = date.toString(DateFormats.exposureUI)
                        val value = date.toString(DateFormats.dateInputData)
                        add(AnswerOption(label, null, value))
                    }

                    add(
                        AnswerOption(
                            context.getString(R.string.contact_information_last_exposure_every_day),
                            null,
                            LocalDate.now().toString(DateFormats.dateInputData)
                        )
                    )
                }
            }
        ),
        {
            viewModel.dateOfLastExposure.postValue(it.value)
        },
        JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.dateOfLastExposure.value))
            }
        ),
        // Hide input if a date has been set through the GGD portal
        isHidden = viewModel.dateOfLastExposure.value != null && viewModel.task.value?.source == Source.Portal
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
                        if (!it.dateOfLastExposureItem.isHidden) {
                            it.contactDetailsSection.add(it.dateOfLastExposureItem)
                            communicationTypeQuestionFound = true
                        }
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
            if (!dateOfLastExposureItem.isHidden) {
                contactDetailsSection?.add(dateOfLastExposureItem)
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
                    val shouldStaffContact =
                        viewModel.task.value?.source == Source.Portal && viewModel.task.value?.communication == CommunicationType.Staff

                    sectionToAddTo?.add(
                        QuestionTwoOptionsItem(
                            context,
                            question,
                            {
                                if (!shouldStaffContact) {
                                    when (it.trigger) {
                                        ContactDetailsInputFragment.COMMUNICATION_STAFF -> viewModel.communicationType.value =
                                            CommunicationType.Staff
                                        ContactDetailsInputFragment.COMMUNICATION_INDEX -> viewModel.communicationType.value =
                                            CommunicationType.Index
                                    }
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

        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, {
            if (it == ANSWER_EARLIER) {
                contactDetailsSection.add(
                    contactDetailsSection.getPosition(dateOfLastExposureItem),
                    noExposureRiskItem
                )
                informSection.setEnabled(false)
            } else {
                contactDetailsSection.remove(noExposureRiskItem)
            }
        })
    }

    // Inform

    val informSection = QuestionnaireSection(
        QuestionnaireSectionHeader(
            R.string.contact_section_inform_header,
            R.string.contact_section_inform_subtext,
            3
        ), true
    )

    fun refreshInformSection() {

        val isEnabled = viewModel.category.value != Category.NO_RISK
        val contactName =
            if (!viewModel.selectedContact?.firstName.isNullOrEmpty()) viewModel.selectedContact?.firstName else context.getString(
                R.string.inform_header_this_person
            )
        val header = context.getString(R.string.inform_header, contactName)
        val footer = when (viewModel.communicationType.value) {
            CommunicationType.Staff -> context.getString(
                R.string.inform_contact_title_staff,
                contactName
            )
            else -> context.getString(
                R.string.inform_contact_title_index,
                contactName
            )
        }

        val dateLastExposure = viewModel.dateOfLastExposure.value

        var message = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            // Handle generic texts
            if (!viewModel.selectedContact?.firstName.isNullOrEmpty()) {
                when (viewModel.category.value) {
                    Category.ONE -> {
                        context.getString(R.string.inform_contact_guidelines_category1_no_date)
                    }
                    Category.TWO_B -> {
                        context.getString(R.string.inform_contact_guidelines_category2a_no_date)
                    }
                    Category.TWO_A -> {
                        context.getString(R.string.inform_contact_guidelines_category2b_no_date)
                    }
                    Category.THREE_A, Category.THREE_B -> {
                        context.getString(R.string.inform_contact_guidelines_category3_no_date)
                    }
                    else -> ""
                }
            } else {
                context.getString(R.string.inform_contact_guidelines_no_name)
            }
        } else {
            // Handle with dates
            val exposureDate = LocalDate.parse(dateLastExposure, DateFormats.dateInputData)
            val exposureDatePlusTen =
                exposureDate.plusDays(10).toString(DateFormats.informContactGuidelinesUI)
            val exposureDatePlusEleven =
                exposureDate.plusDays(11).toString(DateFormats.informContactGuidelinesUI)
            val exposureDatePlusFive =
                exposureDate.plusDays(5).toString(DateFormats.informContactGuidelinesUI)
            val exposureDatePlusFourteen =
                exposureDate.plusDays(14).toString(DateFormats.informContactGuidelinesUI)

            if (!viewModel.selectedContact?.firstName.isNullOrEmpty()) {
                when (viewModel.category.value) {
                    Category.ONE -> {
                        context.getString(
                            R.string.inform_contact_guidelines_category1_with_date,
                            exposureDatePlusEleven
                        )
                    }
                    Category.TWO_B -> {
                        context.getString(
                            R.string.inform_contact_guidelines_category2a_with_date,
                            exposureDatePlusFive,
                            exposureDatePlusTen
                        )
                    }
                    Category.TWO_A -> {
                        context.getString(
                            R.string.inform_contact_guidelines_category2b_with_date,
                            exposureDatePlusFive,
                            exposureDatePlusTen
                        )
                    }
                    Category.THREE_A, Category.THREE_B -> {
                        context.getString(
                            R.string.inform_contact_guidelines_category3_with_date,
                            exposureDatePlusFourteen
                        )
                    }
                    else -> ""
                }
            } else {
                context.getString(R.string.inform_contact_guidelines_no_name)
            }

        }


        val link = when (viewModel.category.value) {
            Category.ONE -> context.getString(R.string.inform_contact_link_category1)
            Category.TWO_A -> context.getString(R.string.inform_contact_link_category2a)
            Category.TWO_B -> context.getString(R.string.inform_contact_link_category2b)
            Category.THREE_A, Category.THREE_B -> context.getString(R.string.inform_contact_link_category3)
            else -> ""
        }

        message += "<br/>$link"

        // To be shown above the copied message
        val introMessage = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            when (viewModel.category.value) {
                Category.ONE -> context.getString(R.string.inform_contact_intro_category1)
                Category.TWO_A -> context.getString(R.string.inform_contact_intro_category2, "")
                Category.TWO_B -> context.getString(R.string.inform_contact_intro_category2, "")
                Category.THREE_A, Category.THREE_B -> context.getString(
                    R.string.inform_contact_intro_category3,
                    ""
                )
                else -> ""
            }
        } else {
            val exposureDate = LocalDate.parse(dateLastExposure, DateFormats.dateInputData)
            val exposureDateFormatted = "${
                context.getString(
                    R.string.inform_contact_intro_date,
                    exposureDate.toString(DateFormats.informContactGuidelinesUI)
                )
            } "
            when (viewModel.category.value) {
                Category.ONE -> context.getString(R.string.inform_contact_intro_category1)
                Category.TWO_A -> context.getString(
                    R.string.inform_contact_intro_category2,
                    exposureDateFormatted
                )
                Category.TWO_B -> context.getString(
                    R.string.inform_contact_intro_category2,
                    exposureDateFormatted
                )
                Category.THREE_A, Category.THREE_B -> context.getString(
                    R.string.inform_contact_intro_category3,
                    exposureDateFormatted
                )
                else -> ""
            }
        }

        val fullMessage = "$introMessage<br/>$message"
        val plainMessage = fullMessage.removeHtmlTags()

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

            // Only add the footer if the source is Portal.
            if (viewModel.task.value?.source == Source.Portal) {
                add(SubHeaderItem(footer))
            }

            if (featureFlags.enablePerspectiveCopy) {
                add(
                    ButtonItem(
                        context.getString(R.string.contact_section_inform_copy),
                        {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip =
                                ClipData.newHtmlText("Copied Text", plainMessage, fullMessage)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                context.getString(R.string.contact_section_inform_copied),
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        // If contact calling is off, make button dark. If calling is on but number isn't valid turn dark as well
                        // Otherwise always light
                        type = if (!featureFlags.enableContactCalling || (featureFlags.enableContactCalling && viewModel.selectedContact?.hasValidPhoneNumber() == false)) {
                            ButtonType.DARK
                        } else {
                            ButtonType.LIGHT
                        }
                    )
                )
            }

            // add "Call $name" button if phone is set and config has calling on
            if (featureFlags.enableContactCalling && viewModel.selectedContact?.hasValidPhoneNumber() == true) {
                add(
                    ButtonItem(
                        context.getString(
                            R.string.contact_section_inform_call,
                            viewModel.selectedContact?.firstName ?: "contact"
                        ),
                        {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${viewModel.selectedContact?.number}")
                            )
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

    // Misc

    private fun isCommunicationTypeQuestion(question: Question): Boolean {
        var foundTrigger = false
        question.answerOptions?.forEach {
            if (it?.trigger == ContactDetailsInputFragment.COMMUNICATION_STAFF || it?.trigger == ContactDetailsInputFragment.COMMUNICATION_INDEX) {
                foundTrigger = true
            }
        }
        return foundTrigger
    }

    companion object {
        const val ANSWER_EARLIER = "earlier"
    }
}