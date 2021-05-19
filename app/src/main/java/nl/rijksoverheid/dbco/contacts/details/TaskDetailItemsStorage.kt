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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.questionnaire.data.entity.AnswerOption
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.bcocase.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.config.GuidelinesContainer
import nl.rijksoverheid.dbco.items.ui.*
import nl.rijksoverheid.dbco.util.removeAllChildren
import nl.rijksoverheid.dbco.util.removeHtmlTags
import org.joda.time.Days
import org.joda.time.LocalDate
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType.Index
import nl.rijksoverheid.dbco.items.input.ButtonType.DARK
import nl.rijksoverheid.dbco.items.input.ButtonType.LIGHT

/**
 * Used to display all questions and data for a [Task]
 */
class TaskDetailItemsStorage(
    val enabled: Boolean,
    val newTask: Boolean,
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
        previousAnswerValue = viewModel.sameHouseholdRisk.value.toString(),
        isLocked = viewModel.task.source == Source.Portal,
        isEnabled = enabled,
        canShowEmptyWarning = canShowEmptyWarnings()
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
        previousAnswerValue = "${viewModel.distanceRisk.value?.first}, ${viewModel.distanceRisk.value?.second}",
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
        previousAnswerValue = viewModel.physicalContactRisk.value.toString(),
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
        previousAnswerValue = viewModel.sameRoomRisk.value.toString(),
        isEnabled = enabled
    )

    private val noRiskItem = NoRiskItem(horizontalMargin = R.dimen.activity_horizontal_margin)
    private val noExposureRiskItem = NoExposureRiskItem()

    fun addClassificationQuestions(
        question: Question,
        section: QuestionnaireSection?
    ) {
        classificationQuestion = question
        section?.add(sameHouseholdRiskItem) // always added

        viewModel.sameHouseholdRisk.observe(viewLifecycleOwner, { risk ->
            onSameHouseRiskChanged(risk = risk, section = section)
        })

        viewModel.distanceRisk.observe(viewLifecycleOwner, { risk ->
            onDistanceRiskChanged(risk = risk, section = section)
        })

        viewModel.physicalContactRisk.observe(viewLifecycleOwner, {
            section?.remove(noRiskItem)
        })

        viewModel.sameRoomRisk.observe(viewLifecycleOwner, { risk ->
            onSameRoomRiskChanged(risk = risk, section = section)
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

    private fun onSameRoomRiskChanged(risk: Boolean?, section: QuestionnaireSection?) {
        if (risk == false) {
            if (section?.getPosition(noRiskItem) == -1) {
                section.add(noRiskItem)
            }
        } else {
            section?.remove(noRiskItem)
        }
    }

    private fun onDistanceRiskChanged(
        risk: Pair<Boolean?, Boolean?>?,
        section: QuestionnaireSection?
    ) {
        if (risk != null && risk.first == false) {
            section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
            if (section?.getPosition(sameRoomRiskItem) == -1) {
                section.add(sameRoomRiskItem)
            }
        } else if (risk != null && risk.second == false) {
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
    }

    private fun onSameHouseRiskChanged(risk: Boolean?, section: QuestionnaireSection?) {
        if (risk == false) {
            if (section?.getPosition(distanceRiskItem) == -1) {
                section.add(distanceRiskItem)
            }
        } else {
            section?.remove(distanceRiskItem.apply { clearPreviousAnswer() })
            section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
            section?.remove(sameRoomRiskItem.apply { clearPreviousAnswer() })
            section?.remove(noRiskItem)
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
        previousAnswer = viewModel.dateOfLastExposure.value,
        isEnabled = enabled,
        canShowEmptyWarning = canShowEmptyWarnings()
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
                                answerSelectedListener = {
                                    viewModel.textAnswers[question.uuid!!] = it
                                },
                                previousAnswerValue = viewModel.textAnswers[question.uuid],
                                isEnabled = enabled
                            )
                        )
                    }
                    QuestionType.Date -> {
                        contactDetailsSection.add(
                            DateInputItem(
                                context = context,
                                question = question,
                                answerSelectedListener = {
                                    viewModel.textAnswers[question.uuid!!] = it
                                },
                                previousAnswerValue = viewModel.textAnswers[question.uuid],
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
        val previousValue = viewModel.textAnswers[question.uuid!!]
        section.add(
            when (size) {
                2 -> {
                    QuestionTwoOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = {
                            viewModel.textAnswers[question.uuid!!] = it.value!!
                        },
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                3 -> {
                    QuestionThreeOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = {
                            viewModel.textAnswers[question.uuid!!] = it.value!!
                        },
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                else -> {
                    QuestionMultipleOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = {
                            viewModel.textAnswers[question.uuid!!] = it.value!!
                        },
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
                VerticalSpaceItem(R.dimen.activity_vertical_margin),
                ContactNameItem(
                    firstName = viewModel.task.linkedContact?.firstName,
                    lastName = viewModel.task.linkedContact?.lastName,
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings(),
                    canShowFakeNameWarning = canShowWarnings(),
                ) { newFirstName, newLastName ->
                    viewModel.task.linkedContact?.firstName = newFirstName
                    viewModel.task.linkedContact?.lastName = newLastName
                    viewModel.name.value = newFirstName
                },
                PhoneNumberItem(
                    numbers = viewModel.task.linkedContact?.numbers ?: emptySet(),
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings() && viewModel.hasEmailOrPhone.value == false
                ) {
                    viewModel.task.linkedContact?.numbers = it
                    viewModel.hasEmailOrPhone.value =
                        viewModel.task.linkedContact?.hasValidEmailOrPhone()
                },
                EmailAddressItem(
                    emailAddresses = viewModel.task.linkedContact?.emails ?: emptySet(),
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings() && viewModel.hasEmailOrPhone.value == false
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

        val isEnabled = isInformSectionEnabled()
        val contactName = getContactName()
        val header = context.getString(R.string.inform_header, contactName)
        val footer = getInformFooter(contactName)

        val dateLastExposure = viewModel.dateOfLastExposure.value
        val referenceItem = getInformCaseReferenceItem()
        val message = getInformMessage(dateLastExposure, referenceItem)

        val introMessage = getInformIntroMessage(dateLastExposure)
        val fullMessage = "$introMessage<br/>$message"
        val plainMessage = fullMessage.removeHtmlTags()

        informSection.apply {
            if (!isExpanded) onToggleExpanded()
            removeAllChildren()
            setEnabled(isEnabled)
            if (!isEnabled) return

            val margin = R.dimen.activity_horizontal_margin
            add(SubHeaderItem(text = header, horizontalMargin = margin))
            add(ParagraphItem(text = message, clickable = true, horizontalMargin = margin))
            add(SubHeaderItem(text = footer, horizontalMargin = margin))

            val callButtonType = getCallButtonType()
            val copyButtonType = getCopyButtonType()

            if (viewModel.copyEnabled(featureFlags)) {
                add(
                    ButtonItem(
                        text = context.getString(R.string.contact_section_inform_copy),
                        buttonClickListener = { copyGuidelines(plainMessage, fullMessage) },
                        type = copyButtonType,
                        horizontalMargin = margin
                    )
                )
            }

            if (viewModel.callingEnabled(featureFlags)) {
                val name = viewModel.task.linkedContact!!.firstName ?: "contact"
                val number = viewModel.task.linkedContact!!.numbers.first()
                add(
                    ButtonItem(
                        text = context.getString(R.string.contact_section_inform_call, name),
                        buttonClickListener = { callTask(number) },
                        type = callButtonType,
                        horizontalMargin = margin
                    )
                )
            }
        }
    }

    private fun getCopyButtonType(): ButtonType {
        return if (
            viewModel.commByIndex() &&
            viewModel.copyEnabled(featureFlags) &&
            !viewModel.callingEnabled(featureFlags)
        ) {
            DARK
        } else {
            LIGHT
        }
    }

    private fun getCallButtonType(): ButtonType {
        return if (viewModel.commByIndex() && viewModel.callingEnabled(featureFlags)) {
            DARK
        } else {
            LIGHT
        }
    }

    private fun getInformFooter(contactName: String): String {
        return when (viewModel.communicationType.value) {
            CommunicationType.Staff -> context.getString(
                R.string.inform_contact_title_staff,
                contactName
            )
            Index -> context.getString(
                R.string.inform_contact_title_index,
                contactName
            )
            else -> context.getString(R.string.inform_contact_title_unknown, contactName)
        }
    }

    private fun isInformSectionEnabled(): Boolean {
        return viewModel.category.value != null && viewModel.category.value != Category.NO_RISK
    }

    private fun getContactName(): String {
        return if (!viewModel.name.value.isNullOrEmpty()) {
            viewModel.name.value!!
        } else {
            context.getString(R.string.inform_header_this_person)
        }
    }

    private fun getInformIntroMessage(dateLastExposure: String?): String {
        return if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            when (viewModel.category.value) {
                Category.ONE -> guidelines.introExposureDateUnknown.getCategory1()
                Category.TWO_A, Category.TWO_B -> guidelines.introExposureDateUnknown.getCategory2()
                Category.THREE_A, Category.THREE_B -> guidelines.introExposureDateUnknown.getCategory3()
                else -> ""
            }
        } else {
            when (viewModel.category.value) {
                Category.ONE -> guidelines.introExposureDateKnown.getCategory1()
                Category.TWO_A, Category.TWO_B -> guidelines.introExposureDateKnown.getCategory2(
                    exposureDate = dateLastExposure
                )
                Category.THREE_A, Category.THREE_B -> guidelines.introExposureDateKnown.getCategory3(
                    exposureDate = dateLastExposure
                )
                else -> ""
            }
        }
    }

    private fun getInformLink(): String {
        return when (viewModel.category.value) {
            Category.ONE -> guidelines.outro.getCategory1()
            Category.TWO_A, Category.TWO_B -> guidelines.outro.getCategory2()
            Category.THREE_A, Category.THREE_B -> guidelines.outro.getCategory3()
            else -> ""
        }
    }

    private fun getInformCaseReferenceItem(): String? {
        return if (viewModel.hasCaseReference()) {
            guidelines.getReferenceNumberItem(viewModel.getCaseReference()!!)
        } else null
    }

    private fun getInformMessage(dateLastExposure: String?, referenceItem: String?): String {
        val message = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            // Handle generic texts
            when (viewModel.category.value) {
                Category.ONE -> guidelines.guidelinesExposureDateUnknown.getCategory1(
                    referenceNumberItem = referenceItem
                )
                Category.TWO_A, Category.TWO_B -> guidelines.guidelinesExposureDateUnknown.getCategory2(
                    referenceNumberItem = referenceItem
                )
                Category.THREE_A, Category.THREE_B -> guidelines.guidelinesExposureDateUnknown.getCategory3(
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
                Category.ONE -> guidelines.guidelinesExposureDateKnown.getCategory1(
                    exposureDatePlusEleven = exposureDatePlusEleven,
                    referenceNumberItem = referenceItem
                )
                Category.TWO_A, Category.TWO_B -> guidelines.guidelinesExposureDateKnown.getCategory2(
                    withinRange = daysBetweenEncounterAndNow < 4,
                    exposureDatePlusFive = exposureDatePlusFive,
                    exposureDatePlusTen = exposureDatePlusTen,
                    referenceNumberItem = referenceItem
                )
                Category.THREE_A, Category.THREE_B -> guidelines.guidelinesExposureDateKnown.getCategory3(
                    exposureDatePlusFive = exposureDatePlusFive,
                    referenceNumberItem = referenceItem
                )
                else -> ""
            }
        }
        return "$message<br/>${getInformLink()}"
    }

    private fun callTask(number: String) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:${number}")
        )
        context.startActivity(intent)
    }

    private fun copyGuidelines(plainMessage: String, fullMessage: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newHtmlText("Copied Text", plainMessage, fullMessage)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            context.getString(R.string.contact_section_inform_copied),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun canShowWarnings(): Boolean = enabled

    private fun canShowEmptyWarnings(): Boolean = canShowWarnings() && !newTask

    companion object {
        const val ANSWER_EARLIER = "earlier"
    }
}