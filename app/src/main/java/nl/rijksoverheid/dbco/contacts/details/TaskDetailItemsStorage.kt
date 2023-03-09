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
import nl.rijksoverheid.dbco.questionnaire.data.entity.*
import nl.rijksoverheid.dbco.questionnaire.data.entity.Trigger.ShareIndexNameAllowed
import nl.rijksoverheid.dbco.questionnaire.data.entity.Trigger.ShareIndexNameDisallowed
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel

/**
 * Used to display all questions and data for a [Task]
 */
class TaskDetailItemsStorage(
    val enabled: Boolean,
    val newTask: Boolean,
    val taskDetailViewModel: TasksDetailViewModel,
    val selfBcoCaseViewModel: SelfBcoCaseViewModel,
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
            R.string.contact_section_typeofcontact_subtext,
            1
        ), true
    )

    private val sameHouseholdRiskItem = QuestionTwoOptionsItem(
        context = context,
        question = Question(
            description = null,
            label = context.getString(R.string.lived_together_risk_label),
            questionType = QuestionType.ClassificationDetails,
            group = Group.Classification,
            answerOptions = listOf(
                AnswerOption(context.getString(R.string.answer_no), false.toString()),
                AnswerOption(context.getString(R.string.answer_yes), true.toString())
            )
        ),
        answerSelectedListener = {
            taskDetailViewModel.sameHouseholdRisk.value = it.value.toBoolean()
            taskDetailViewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = taskDetailViewModel.sameHouseholdRisk.value.toString(),
        isLocked = taskDetailViewModel.task.source == Source.Portal,
        isEnabled = enabled,
        canShowEmptyWarning = canShowEmptyWarnings()
    )

    private val distanceRiskItem = QuestionThreeOptionsItem(
        context = context,
        question = Question(
            description = context.getString(R.string.distance_risk_description),
            label = context.getString(R.string.distance_risk_label),
            questionType = QuestionType.ClassificationDetails,
            group = Group.Classification,
            answerOptions = listOf(
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
                "true, true" -> taskDetailViewModel.distanceRisk.value =
                    Pair(first = true, second = true)
                "true, false" -> taskDetailViewModel.distanceRisk.value =
                    Pair(first = true, second = false)
                else -> taskDetailViewModel.distanceRisk.value = Pair(first = false, second = false)
            }
            taskDetailViewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = "${taskDetailViewModel.distanceRisk.value?.first}, ${taskDetailViewModel.distanceRisk.value?.second}",
        isLocked = taskDetailViewModel.task.source == Source.Portal,
        isEnabled = enabled
    )

    private val physicalContactRiskItem = QuestionTwoOptionsItem(
        context = context,
        question = Question(
            description = context.getString(R.string.physical_risk_description),
            label = context.getString(R.string.physical_risk_label),
            questionType = QuestionType.ClassificationDetails,
            group = Group.Classification,
            answerOptions = listOf(
                AnswerOption(context.getString(R.string.answer_think_yes), true.toString()),
                AnswerOption(context.getString(R.string.answer_know_no), false.toString())
            )
        ),
        answerSelectedListener = {
            taskDetailViewModel.physicalContactRisk.value = it.value.toBoolean()
            taskDetailViewModel.updateCategoryFromRiskFlags()
        },
        previousAnswerValue = taskDetailViewModel.physicalContactRisk.value.toString(),
        isLocked = taskDetailViewModel.task.source == Source.Portal,
        isEnabled = enabled,
    )

    private val noRiskItem = NoRiskItem(horizontalMargin = R.dimen.activity_horizontal_margin)
    private val noExposureRiskItem = NoExposureRiskItem()

    fun addClassificationQuestions(
        question: Question,
        section: QuestionnaireSection?
    ) {
        classificationQuestion = question
        section?.add(sameHouseholdRiskItem) // always added

        taskDetailViewModel.sameHouseholdRisk.observe(viewLifecycleOwner) { risk ->
            onSameHouseRiskChanged(risk = risk, section = section)
        }

        taskDetailViewModel.distanceRisk.observe(viewLifecycleOwner) { risk ->
            onDistanceRiskChanged(risk = risk, section = section)
        }

        taskDetailViewModel.physicalContactRisk.observe(viewLifecycleOwner) { risk ->
            onPhysicalContactRiskChanged(risk = risk, section = section)
        }

        listOf(
            sameHouseholdRiskItem,
            distanceRiskItem,
            physicalContactRiskItem
        ).forEach {
            it.question?.uuid = question.uuid
        }
    }

    private fun onDistanceRiskChanged(
        risk: Pair<Boolean?, Boolean?>?,
        section: QuestionnaireSection?
    ) {
        if (risk != null && risk.first == false) {
            section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
            section.addItem(noRiskItem)
        } else if (risk != null && risk.second == false) {
            section?.remove(noRiskItem)
            section.addItem(physicalContactRiskItem)
        } else {
            section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
            section?.remove(noRiskItem)
        }
    }

    private fun onPhysicalContactRiskChanged(
        risk: Boolean?,
        section: QuestionnaireSection?
    ) {
        if (risk != null && risk == false) {
            section.addItem(noRiskItem)
        } else {
            section?.remove(noRiskItem)
        }
    }

    private fun onSameHouseRiskChanged(risk: Boolean?, section: QuestionnaireSection?) {
        if (risk == false) {
            section.addItem(distanceRiskItem)
        } else {
            section?.remove(distanceRiskItem.apply { clearPreviousAnswer() })
            section?.remove(physicalContactRiskItem.apply { clearPreviousAnswer() })
            section?.remove(noRiskItem)
        }
    }

    // Contact details

    val contactDetailsSection = QuestionnaireSection(
        QuestionnaireSectionHeader(
            R.string.contact_section_contactdetails_header,
            R.string.contact_section_contactdetails_subtext,
            R.string.contact_section_contactdetails_subtext_disabled,
            2
        ), false
    )

    private val lastExposureExplanation: String?
        get() = if (selfBcoCaseViewModel.isStartOfContagiousPeriodTooFarInPast()) {
            context.getString(R.string.contact_information_last_exposure_explanation)
        } else null

    val dateOfLastExposureItem = QuestionMultipleOptionsItem(
        context = context,
        question = Question(
            description = null,
            label = context.getString(R.string.contact_information_last_exposure_label),
            explanation = lastExposureExplanation,
            questionType = QuestionType.Multiplechoice,
            group = Group.ContactDetails,
            answerOptions = mutableListOf<AnswerOption>().apply {
                taskDetailViewModel.getEarliestExposureDateOption()?.let {

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
            taskDetailViewModel.dateOfLastExposure.postValue(it.value)
        },
        previousAnswer = taskDetailViewModel.dateOfLastExposure.value,
        isEnabled = enabled,
        canShowEmptyWarning = canShowEmptyWarnings()
    )

    fun refreshContactDetailsSection() {
        val questions = taskDetailViewModel.questionnaire?.questions?.filterNotNull()?.toMutableList()
        val task = taskDetailViewModel.task
        questions ?: return

        contactDetailsSection.removeAllChildren()
        contactDetailsSection.add(SimpleTextItem(R.string.contact_section_contactdetails_extra_info))

        val shareIndexNameQuestion = questions.find {
            it.hasTrigger(ShareIndexNameAllowed, ShareIndexNameDisallowed)
        }

        if (task.shareIndexNameAlreadyAnswered) questions.remove(shareIndexNameQuestion)

        questions.forEach { question ->
            if (question.group == Group.ContactDetails &&
                question.isRelevantForCategory(taskDetailViewModel.category.value)
            ) {
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
                                    taskDetailViewModel.textAnswers[question.uuid!!] = it
                                },
                                previousAnswerValue = taskDetailViewModel.textAnswers[question.uuid],
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
                                    taskDetailViewModel.textAnswers[question.uuid!!] = it
                                },
                                previousAnswerValue = taskDetailViewModel.textAnswers[question.uuid],
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
        val previousValue = taskDetailViewModel.textAnswers[question.uuid!!]
        val answerSelectedClosure = { answer: AnswerOption ->
            taskDetailViewModel.textAnswers[question.uuid!!] = answer.value!!
            answer.trigger?.let { taskDetailViewModel.onAnswerTrigger(it) }
            Unit
        }
        section.add(
            when (size) {
                2 -> {
                    QuestionTwoOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = answerSelectedClosure,
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                3 -> {
                    QuestionThreeOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = answerSelectedClosure,
                        previousAnswerValue = previousValue,
                        isEnabled = enabled
                    )
                }
                else -> {
                    QuestionMultipleOptionsItem(
                        context = context,
                        question = question,
                        answerSelectedListener = answerSelectedClosure,
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
                    firstName = taskDetailViewModel.task.linkedContact?.firstName,
                    lastName = taskDetailViewModel.task.linkedContact?.lastName,
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings(),
                    canShowFakeNameWarning = canShowWarnings(),
                ) { newFirstName, newLastName ->
                    taskDetailViewModel.task.linkedContact?.firstName = newFirstName
                    taskDetailViewModel.task.linkedContact?.lastName = newLastName
                    taskDetailViewModel.name.value = newFirstName
                },
                PhoneNumberItem(
                    numbers = taskDetailViewModel.task.linkedContact?.numbers ?: emptySet(),
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings() && taskDetailViewModel.hasEmailOrPhone.value == false
                ) {
                    taskDetailViewModel.task.linkedContact?.numbers = it
                    taskDetailViewModel.hasEmailOrPhone.value =
                        taskDetailViewModel.task.linkedContact?.hasValidEmailOrPhone()
                },
                EmailAddressItem(
                    emailAddresses = taskDetailViewModel.task.linkedContact?.emails ?: emptySet(),
                    question = question,
                    isEnabled = enabled,
                    canShowEmptyWarning = canShowEmptyWarnings() && taskDetailViewModel.hasEmailOrPhone.value == false
                ) {
                    taskDetailViewModel.task.linkedContact?.emails = it
                    taskDetailViewModel.hasEmailOrPhone.value =
                        taskDetailViewModel.task.linkedContact?.hasValidEmailOrPhone()
                }
            )
        )

        taskDetailViewModel.dateOfLastExposure.observe(viewLifecycleOwner) {
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
        }
    }

    // Inform

    val informSection = QuestionnaireSection(
        QuestionnaireSectionHeader(
            R.string.contact_section_inform_header,
            R.string.contact_section_inform_subtext,
            R.string.contact_section_inform_subtext_disabled,
            3
        ), false
    )

    fun refreshInformSection() {

        val isEnabled = isInformSectionEnabled()
        val contactName = getContactName()
        val header = context.getString(R.string.inform_header, contactName)
        val footer = getInformFooter(contactName)

        val dateLastExposure = taskDetailViewModel.dateOfLastExposure.value
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

            if (taskDetailViewModel.copyEnabled(featureFlags)) {
                add(
                    ButtonItem(
                        text = context.getString(R.string.contact_section_inform_copy),
                        buttonClickListener = { copyGuidelines(plainMessage, fullMessage) },
                        type = copyButtonType,
                        horizontalMargin = margin
                    )
                )
            }

            if (taskDetailViewModel.callingEnabled(featureFlags)) {
                val name = taskDetailViewModel.task.linkedContact!!.firstName ?: "contact"
                val number = taskDetailViewModel.task.linkedContact!!.numbers.first()
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
            taskDetailViewModel.commByIndex() &&
            taskDetailViewModel.copyEnabled(featureFlags) &&
            !taskDetailViewModel.callingEnabled(featureFlags)
        ) {
            DARK
        } else {
            LIGHT
        }
    }

    private fun getCallButtonType(): ButtonType {
        return if (taskDetailViewModel.commByIndex() && taskDetailViewModel.callingEnabled(
                featureFlags
            )
        ) {
            DARK
        } else {
            LIGHT
        }
    }

    private fun getInformFooter(contactName: String): String {
        return when (taskDetailViewModel.communicationType.value) {
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
        return taskDetailViewModel.category.value != null && taskDetailViewModel.category.value != Category.NO_RISK
    }

    private fun getContactName(): String {
        return if (!taskDetailViewModel.name.value.isNullOrEmpty()) {
            taskDetailViewModel.name.value!!
        } else {
            context.getString(R.string.inform_header_this_person)
        }
    }

    private fun getInformIntroMessage(dateLastExposure: String?): String {
        return if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            when (taskDetailViewModel.category.value) {
                Category.ONE -> guidelines.introExposureDateUnknown.getCategory1()
                Category.TWO_A, Category.TWO_B -> guidelines.introExposureDateUnknown.getCategory2()
                Category.THREE_A, Category.THREE_B -> guidelines.introExposureDateUnknown.getCategory3()
                else -> ""
            }
        } else {
            val exposureDate = LocalDate.parse(dateLastExposure, DateFormats.dateInputData)
            when (taskDetailViewModel.category.value) {
                Category.ONE -> guidelines.introExposureDateKnown.getCategory1(
                    exposureDate = exposureDate
                )
                Category.TWO_A, Category.TWO_B -> guidelines.introExposureDateKnown.getCategory2(
                    exposureDate = exposureDate
                )
                Category.THREE_A, Category.THREE_B -> guidelines.introExposureDateKnown.getCategory3(
                    exposureDate = exposureDate
                )
                else -> ""
            }
        }
    }

    private fun getInformLink(): String {
        return when (taskDetailViewModel.category.value) {
            Category.ONE -> guidelines.outro.getCategory1()
            Category.TWO_A, Category.TWO_B -> guidelines.outro.getCategory2()
            Category.THREE_A, Category.THREE_B -> guidelines.outro.getCategory3()
            else -> ""
        }
    }

    private fun getInformCaseReferenceItem(): String? {
        return if (taskDetailViewModel.hasCaseReference()) {
            guidelines.getReferenceNumberItem(taskDetailViewModel.getCaseReference()!!)
        } else null
    }

    private fun getInformMessage(dateLastExposure: String?, referenceItem: String?): String {
        val message = if (dateLastExposure == null || dateLastExposure == ANSWER_EARLIER) {
            // Handle generic texts
            when (taskDetailViewModel.category.value) {
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
            val daysBetweenEncounterAndNow = Days.daysBetween(exposureDate, LocalDate.now()).days

            when (taskDetailViewModel.category.value) {
                Category.ONE -> guidelines.guidelinesExposureDateKnown.getCategory1(
                    exposureDate = exposureDate,
                    referenceNumberItem = referenceItem
                )
                Category.TWO_A, Category.TWO_B -> guidelines.guidelinesExposureDateKnown.getCategory2(
                    withinRange = daysBetweenEncounterAndNow < 4,
                    exposureDate = exposureDate,
                    referenceNumberItem = referenceItem
                )
                Category.THREE_A, Category.THREE_B -> guidelines.guidelinesExposureDateKnown.getCategory3(
                    exposureDate = exposureDate,
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

    private fun QuestionnaireSection?.addItem(group: com.xwray.groupie.Group) {
        if (this != null && getPosition(noRiskItem) == -1) {
            add(group)
        }
    }

    companion object {

        const val ANSWER_EARLIER = "earlier"
    }
}