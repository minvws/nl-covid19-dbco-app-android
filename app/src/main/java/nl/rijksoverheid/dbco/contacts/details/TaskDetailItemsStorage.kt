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
import nl.rijksoverheid.dbco.bcocase.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.config.GuidelinesContainer
import nl.rijksoverheid.dbco.util.removeAllChildren
import nl.rijksoverheid.dbco.util.removeHtmlTags
import org.joda.time.Days
import org.joda.time.LocalDate

class TaskDetailItemsStorage(
    val enabled: Boolean,
    val viewModel: TasksDetailViewModel,
    val context: Context,
    private val viewLifecycleOwner: LifecycleOwner,
    private val featureFlags: FeatureFlags,
    private val guidelines: GuidelinesContainer
) {

    var classificationQuestion: Question? = null

    val classificationSection = QuestionnaireSection(
        QuestionnaireSectionHeader(
            R.string.contact_section_typeofcontact_header,
            R.string.contact_section_typeofcontact_subtext,
            1
        ), true
    )

    private val sameHouseholdRiskItem = QuestionTwoOptionsItem(
        context = context,
        question = Question(
            null,
            context.getString(R.string.lived_together_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(context.getString(R.string.answer_no), false.toString()),
                AnswerOption(context.getString(R.string.answer_yes), true.toString())
            )
        ),
        answerSelectedListener = {
            viewModel.sameHouseholdRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.sameHouseholdRisk.value))
            }
        ),
        isLocked = viewModel.task.source == Source.Portal,
        isEnabled = enabled
    )

    private val distanceRiskItem = QuestionThreeOptionsItem(
        context = context,
        question = Question(
            null,
            context.getString(R.string.distance_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(
                    context.getString(R.string.distance_risk_answer_long),
                    "true, true"
                ),
                AnswerOption(
                    context.getString(R.string.distance_risk_answer_short),
                    "true, false"
                ),
                AnswerOption(context.getString(R.string.distance_risk_answer_no), "false, false")
            )
        ),
        answerSelectedListener = {
            when (it.value) {
                "true, true" -> viewModel.distanceRisk.value = Pair(first = true, second = true)
                "true, false" -> viewModel.distanceRisk.value = Pair(first = true, second = false)
                else -> viewModel.distanceRisk.value = Pair(first = false, second = false)
            }
            viewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = JsonObject(
            HashMap<String, JsonElement>().apply {
                put(
                    "value", JsonPrimitive(
                        "${viewModel.distanceRisk.value?.first}, ${viewModel.distanceRisk.value?.second}"
                    )
                )
            }
        ),
        isLocked = viewModel.task.source == Source.Portal,
        isEnabled = enabled
    )

    private val physicalContactRiskItem = QuestionTwoOptionsItem(
        context = context,
        question = Question(
            context.getString(R.string.physical_risk_description),
            context.getString(R.string.physical_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(context.getString(R.string.answer_think_yes), true.toString()),
                AnswerOption(context.getString(R.string.answer_know_no), false.toString())
            )
        ),
        answerSelectedListener = {
            viewModel.physicalContactRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.physicalContactRisk.value))
            }
        ),
        isLocked = viewModel.task.source == Source.Portal,
        isEnabled = enabled,
    )

    private val sameRoomRiskItem = QuestionTwoOptionsItem(
        context = context,
        question = Question(
            null,
            context.getString(R.string.same_room_risk_label),
            QuestionType.ClassificationDetails,
            Group.Classification,
            listOf(
                AnswerOption(context.getString(R.string.answer_think_yes), true.toString()),
                AnswerOption(context.getString(R.string.answer_think_no), false.toString())
            )
        ),
        answerSelectedListener = {
            viewModel.sameRoomRisk.value = it.value.toBoolean()
            viewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.sameRoomRisk.value))
            }
        ),
        isEnabled = enabled
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
        context = context,
        question = Question(
            null,
            context.getString(R.string.contact_information_last_exposure_label),
            QuestionType.Multiplechoice,
            Group.ContactDetails,
            mutableListOf<AnswerOption>().apply {
                viewModel.getStartOfContagiousPeriod()?.let {

                    add(
                        AnswerOption(
                            context.getString(R.string.contact_information_last_exposure_earlier),
                            ANSWER_EARLIER
                        )
                    )

                    val interval = Days.daysBetween(it, LocalDate.now()).days
                    for (i in 0..interval) {
                        val date = it.plusDays(i)
                        val label = date.toString(DateFormats.exposureUI)
                        val value = date.toString(DateFormats.dateInputData)
                        add(AnswerOption(label, value))
                    }

                    add(
                        AnswerOption(
                            context.getString(R.string.contact_information_last_exposure_every_day),
                            LocalDate.now().toString(DateFormats.dateInputData)
                        )
                    )
                }
            }
        ),
        answerSelectedListener = {
            viewModel.dateOfLastExposure.postValue(it.value)
        },
        previousAnswer = JsonObject(
            HashMap<String, JsonElement>().apply {
                put("value", JsonPrimitive(viewModel.dateOfLastExposure.value))
            }
        ),
        isEnabled = enabled
    )

    fun refreshContactDetailsSection() {
        contactDetailsSection.removeAllChildren()
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            if (question.group == Group.ContactDetails && question.isRelevantForCategory(viewModel.category.value)) {
                when (question.questionType) {
                    QuestionType.Multiplechoice -> {
                        addMultiChoiceItem(question, contactDetailsSection)
                    }
                    QuestionType.Open -> {
                        contactDetailsSection.add(
                            SingleInputItem(
                                context = context,
                                question = question,
                                previousAnswerValue = viewModel.task.questionnaireResult?.getAnswerByQuestionUuid(
                                    question.uuid
                                )?.value,
                                isEnabled = enabled
                            )
                        )
                    }
                    QuestionType.Date -> {
                        contactDetailsSection.add(
                            DateInputItem(
                                context = context,
                                question = question,
                                previousAnswerValue = viewModel.task.questionnaireResult?.getAnswerByQuestionUuid(
                                    question.uuid
                                )?.value,
                                isEnabled = enabled
                            )
                        )
                    }
                    QuestionType.ContactDetails -> {
                        addContactDetailsItems(contactDetailsSection, question)
                    }
                    else -> { /* NO-OP */
                    }
                }
            }
        }
    }

    private fun addMultiChoiceItem(
        question: Question,
        section: QuestionnaireSection
    ) {
        val size = question.answerOptions?.size ?: return
        if (size == 1) return
        val previousValue =
            viewModel.task.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
        section.add(
            when (size) {
                2 -> {
                    QuestionTwoOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = { /* NO-OP */ },
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                3 -> {
                    QuestionThreeOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = { /* NO-OP */ },
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                else -> {
                    QuestionMultipleOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = { /* NO-OP */ },
                        previousAnswer = previousValue,
                        isEnabled = enabled
                    )
                }
            }
        )
    }

    private fun addContactDetailsItems(
        section: QuestionnaireSection,
        question: Question
    ) {
        section.addAll(
            listOf(
                ContactNameItem(
                    firstName = viewModel.task.linkedContact?.firstName,
                    lastName = viewModel.task.linkedContact?.lastName,
                    question = question,
                    isEnabled = enabled
                ) { newFirstName, newLastName ->
                    viewModel.task.linkedContact?.firstName = newFirstName
                    viewModel.task.linkedContact?.lastName = newLastName
                    viewModel.name.value = newFirstName
                },
                PhoneNumberItem(
                    numbers = viewModel.task.linkedContact?.numbers ?: emptySet(),
                    question = question,
                    isEnabled = enabled
                ) {
                    viewModel.task.linkedContact?.numbers = it
                    viewModel.hasEmailOrPhone.value =
                        viewModel.task.linkedContact?.hasValidEmailOrPhone()
                },
                EmailAddressItem(
                    emailAddresses = viewModel.task.linkedContact?.emails ?: emptySet(),
                    question = question,
                    isEnabled = enabled
                ) {
                    viewModel.task.linkedContact?.emails = it
                    viewModel.hasEmailOrPhone.value =
                        viewModel.task.linkedContact?.hasValidEmailOrPhone()
                }
            )
        )

        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, {
            if (it == ANSWER_EARLIER) {
                classificationSection.remove(noExposureRiskItem)
                classificationSection.add(
                    classificationSection.getPosition(dateOfLastExposureItem),
                    noExposureRiskItem
                )
                informSection.setEnabled(false)
            } else {
                classificationSection.remove(noExposureRiskItem)
            }
        })
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

        val isEnabled =
            viewModel.category.value != null && viewModel.category.value != Category.NO_RISK
        val contactName = if (!viewModel.name.value.isNullOrEmpty()) {
            viewModel.name.value
        } else {
            context.getString(R.string.inform_header_this_person)
        }
        val header = context.getString(R.string.inform_header, contactName)
        val footer = when (viewModel.communicationType.value) {
            CommunicationType.Staff -> context.getString(
                R.string.inform_contact_title_staff,
                contactName
            )
            CommunicationType.Index -> context.getString(
                R.string.inform_contact_title_index,
                contactName
            )
            else -> context.getString(R.string.inform_contact_title_unknown, contactName)
        }

        val dateLastExposure = viewModel.dateOfLastExposure.value

        val referenceItem = if (viewModel.hasCaseReference()) {
            guidelines.getReferenceNumberItem(viewModel.getCaseReference()!!)
        } else null

        var message = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            // Handle generic texts
            when (viewModel.category.value) {
                Category.ONE -> guidelines.genericGuidelines.getCategory1(
                    referenceNumberItem = referenceItem
                )
                Category.TWO_A, Category.TWO_B -> guidelines.genericGuidelines.getCategory2(
                    referenceNumberItem = referenceItem
                )
                Category.THREE_A, Category.THREE_B -> guidelines.genericGuidelines.getCategory3(
                    referenceNumberItem = referenceItem
                )
                else -> ""
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

            val daysBetweenEncounterAndNow = Days.daysBetween(exposureDate, LocalDate.now()).days

            when (viewModel.category.value) {
                Category.ONE -> guidelines.guidelines.getCategory1(
                    exposureDatePlusEleven = exposureDatePlusEleven,
                    referenceNumberItem = referenceItem
                )
                Category.TWO_A, Category.TWO_B -> guidelines.guidelines.getCategory2(
                    withinRange = daysBetweenEncounterAndNow < 4,
                    exposureDatePlusFive = exposureDatePlusFive,
                    exposureDatePlusTen = exposureDatePlusTen,
                    referenceNumberItem = referenceItem
                )
                Category.THREE_A, Category.THREE_B -> guidelines.guidelines.getCategory3(
                    exposureDatePlusFive = exposureDatePlusFive,
                    referenceNumberItem = referenceItem
                )
                else -> ""
            }
        }

        val link = when (viewModel.category.value) {
            Category.ONE -> guidelines.outro.getCategory1()
            Category.TWO_A, Category.TWO_B -> guidelines.outro.getCategory2()
            Category.THREE_A, Category.THREE_B -> guidelines.outro.getCategory3()
            else -> ""
        }

        message += "<br/>$link"

        // To be shown above the copied message
        val introMessage = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            when (viewModel.category.value) {
                Category.ONE -> guidelines.genericIntro.getCategory1()
                Category.TWO_A, Category.TWO_B -> guidelines.genericIntro.getCategory2()
                Category.THREE_A, Category.THREE_B -> guidelines.genericIntro.getCategory3()
                else -> ""
            }
        } else {
            when (viewModel.category.value) {
                Category.ONE -> guidelines.intro.getCategory1()
                Category.TWO_A, Category.TWO_B -> guidelines.intro.getCategory2(
                    exposureDate = dateLastExposure
                )
                Category.THREE_A, Category.THREE_B -> guidelines.intro.getCategory3(
                    exposureDate = dateLastExposure
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
            add(SubHeaderItem(footer))

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
                        type = if (!featureFlags.enableContactCalling || (featureFlags.enableContactCalling && viewModel.task.linkedContact?.hasValidPhoneNumber() == false)) {
                            ButtonType.DARK
                        } else {
                            ButtonType.LIGHT
                        }
                    )
                )
            }

            // add "Call $name" button if phone is set and config has calling on
            if (featureFlags.enableContactCalling && viewModel.task.linkedContact?.hasValidPhoneNumber() == true) {
                add(
                    ButtonItem(
                        context.getString(
                            R.string.contact_section_inform_call,
                            viewModel.task.linkedContact?.firstName ?: "contact"
                        ),
                        {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${viewModel.task.linkedContact?.numbers?.first()}")
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

    companion object {
        const val ANSWER_EARLIER = "earlier"
    }
}