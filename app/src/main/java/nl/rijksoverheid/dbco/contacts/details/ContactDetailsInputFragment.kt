/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.input.BaseQuestionItem
import nl.rijksoverheid.dbco.items.input.ContactNameItem
import nl.rijksoverheid.dbco.items.input.DateInputItem
import nl.rijksoverheid.dbco.items.input.EmailAddressItem
import nl.rijksoverheid.dbco.items.input.PhoneNumberItem
import nl.rijksoverheid.dbco.items.input.QuestionMultipleOptionsItem
import nl.rijksoverheid.dbco.items.input.QuestionTwoOptionsItem
import nl.rijksoverheid.dbco.items.input.SingleInputItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.removeAllChildren
import org.joda.time.LocalDate
import java.util.*
import kotlin.collections.HashMap


class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()
    private val viewModel by viewModels<TasksDetailViewModel>()
    private var itemsStorage: TaskDetailItemsStorage? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactInputBinding.bind(view)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            QuestionnaireSectionDecorator(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        val task = args.indexTask ?: Task(taskType = "contact", source = Source.App)
        viewModel.setTask(task)

        itemsStorage = TaskDetailItemsStorage(viewModel, view.context).apply {
            adapter.add(classificationSection)
            adapter.add(contactDetailsSection)
            adapter.add(informSection)
        }

        if (viewModel.selectedContact == null) {
            viewModel.selectedContact = args.selectedContact ?: LocalContact.fromLabel(task.label)
        }

        binding.toolbar.title = args.selectedContact?.getDisplayName()
            ?: resources.getString(R.string.mycontacts_add_contact)

        if (task.source == Source.App) {
            binding.toolbar.inflateMenu(R.menu.contact_detail_menu)
            binding.toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete_contact_item -> showDeleteItemDialog(view)
                }
                true
            }
        }

        viewModel.category.observe(viewLifecycleOwner, {
            val hasCategory = it != null
            val categoryHasRisk = it != null && it != Category.NO_RISK

            itemsStorage?.classificationSection?.setEnabled(true)
            itemsStorage?.classificationSection?.setCompleted(hasCategory)
            if (categoryHasRisk) {
                if (itemsStorage?.classificationSection?.isExpanded == true) {
                    itemsStorage?.classificationSection?.onToggleExpanded()
                }
                if (task.source == Source.Portal) {
                    itemsStorage?.classificationSection?.setBlocked(true)
                }
            }

            if (hasCategory) {
                if (itemsStorage?.contactDetailsSection?.isExpanded == false) {
                    itemsStorage?.contactDetailsSection?.onToggleExpanded()
                }
            }

            itemsStorage?.contactDetailsSection?.setEnabled(categoryHasRisk)
            refreshContactDetailsSection()

            itemsStorage?.informSection?.setEnabled(categoryHasRisk)
            itemsStorage?.refreshInformSection()

            binding.saveButton.text =
                if (it == Category.NO_RISK) getString(R.string.cancel) else getString(R.string.save)
        })

        viewModel.communicationType.observe(viewLifecycleOwner, {
            checkIfInformSectionComplete()
            checkIfContactDetailsSectionComplete()
            itemsStorage?.refreshInformSection()
        })

        viewModel.hasEmailOrPhone.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
        })
        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
        })

        refreshClassificationSection()

        binding.saveButton.setOnClickListener {
            if (viewModel.category.value == Category.NO_RISK) {
                findNavController().popBackStack()
                return@setOnClickListener
            }

            if (viewModel.communicationType.value == CommunicationType.Index && viewModel.task.value?.contactIsInformedAlready == false) {
                showDidYouInformDialog(view)
                return@setOnClickListener
            }
            collectAnswers()
        }
    }

    private fun showDeleteItemDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        builder.setMessage(R.string.delete_contact_message)
        builder.setPositiveButton(R.string.answer_yes) { dialog, _ ->
            viewModel.deleteCurrentTask()
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun showDidYouInformDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)
        val string = getString(
            R.string.contact_inform_prompt_title, viewModel.selectedContact?.getDisplayName()
                ?: ""
        )
        builder.setTitle(string)
        builder.setMessage(R.string.contact_inform_prompt_message)
        builder.setPositiveButton(R.string.contact_inform_option_done) { dialog, _ ->
            viewModel.task.value?.contactIsInformedAlready = true
            dialog.dismiss()
            checkIfInformSectionComplete()
        }
        builder.setNeutralButton(R.string.contact_inform_action_inform_now) { dialog, _ ->
            dialog.dismiss()
            // TODO do same as cancel - don't save data???
        }
        builder.setNegativeButton(R.string.contact_inform_action_inform_later) { dialog, _ ->
            dialog.dismiss()
            itemsStorage?.contactDetailsSection?.onToggleExpanded()
            itemsStorage?.informSection?.onToggleExpanded()
            // TODO scroll to inform section
        }
        builder.create().show()
    }

    private fun refreshClassificationSection() {
        itemsStorage?.classificationSection?.removeAllChildren()
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            if (question.group == Group.Classification) {
                addClassificationQuestions(question, itemsStorage?.classificationSection)
            }
        }
    }

    private fun refreshContactDetailsSection() {
        itemsStorage?.contactDetailsSection?.removeAllChildren()
        var communicationTypeQuestionFound = false
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            if (question.group == Group.ContactDetails && question.isRelevantForCategory(viewModel.category.value)) {
                // add hardcoded "date of last exposure" question before communication type question
                if (isCommunicationTypeQuestion(question)) {
                    itemsStorage?.let {
                        it.contactDetailsSection.add(it.dateOfLastExposureItem)
                        communicationTypeQuestionFound = true
                    }
                }
                when (question.questionType) {
                    QuestionType.Multiplechoice -> {
                        addMultiChoiceItem(question, itemsStorage?.contactDetailsSection)
                    }
                    QuestionType.Open -> {
                        itemsStorage?.contactDetailsSection?.add(
                            SingleInputItem(
                                requireContext(),
                                question,
                                viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                            )
                        )
                    }
                    QuestionType.Date -> {
                        itemsStorage?.contactDetailsSection?.add(
                            DateInputItem(
                                requireContext(),
                                question,
                                viewModel.questionnaireResult?.getAnswerByQuestionUuid(question.uuid)?.value
                            )
                        )
                    }
                    QuestionType.ContactDetails -> {
                        addContactDetailsItems(itemsStorage?.contactDetailsSection, question)
                    }
                }
            }
        }
        if (!communicationTypeQuestionFound) { // fallback, shouldn't happen
            itemsStorage?.let {
                itemsStorage?.contactDetailsSection?.add(it.dateOfLastExposureItem)
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
                            requireContext(),
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
                                    CommunicationType.Index -> COMMUNICATION_INDEX
                                    CommunicationType.Staff -> COMMUNICATION_STUFF
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
                            requireContext(),
                            question,
                            {
                                when (it.trigger) {
                                    COMMUNICATION_STUFF -> viewModel.communicationType.value =
                                        CommunicationType.Staff
                                    COMMUNICATION_INDEX -> viewModel.communicationType.value =
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

    private fun checkIfInformSectionComplete() {
        itemsStorage?.informSection?.setCompleted(
            when (viewModel.communicationType.value) {
                CommunicationType.Index -> viewModel.task.value?.contactIsInformedAlready == true
                CommunicationType.Staff -> viewModel.hasEmailOrPhone.value == true
                else -> false
            }
        )
    }

    private fun checkIfContactDetailsSectionComplete() {
        itemsStorage?.contactDetailsSection?.setCompleted(
            viewModel.hasEmailOrPhone.value == true &&
                    viewModel.communicationType.value != null &&
                    viewModel.dateOfLastExposure.value != null
        )
    }

    private fun addClassificationQuestions(
        question: Question,
        section: QuestionnaireSection?
    ) {

        itemsStorage?.let { itemsStorage ->

            section?.add(itemsStorage.livedTogetherRiskItem) // always added

            viewModel.livedTogetherRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.durationRiskItem)
                } else {
                    section?.remove(itemsStorage.distanceRiskItem)
                    section?.remove(itemsStorage.durationRiskItem)
                    section?.remove(itemsStorage.otherRiskItem)
                    section?.remove(itemsStorage.noRiskItem)
                }
            })

            viewModel.durationRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.distanceRiskItem)
                } else {
                    section?.remove(itemsStorage.distanceRiskItem)
                    section?.remove(itemsStorage.otherRiskItem)
                    section?.remove(itemsStorage.noRiskItem)
                }
            })

            viewModel.distanceRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.otherRiskItem)
                } else {
                    section?.remove(itemsStorage.otherRiskItem)
                    section?.remove(itemsStorage.noRiskItem)
                }
            })

            viewModel.otherRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.noRiskItem)
                } else {
                    section?.remove(itemsStorage.noRiskItem)
                }
            })

            listOf(
                itemsStorage.livedTogetherRiskItem,
                itemsStorage.distanceRiskItem,
                itemsStorage.durationRiskItem,
                itemsStorage.otherRiskItem
            ).forEach {
                it.question?.uuid = question.uuid
            }
        }
    }

    private fun collectAnswers() {
        val answers = mutableListOf<Answer>()
        for (groupIndex: Int in 0 until adapter.groupCount) {
            val item = adapter.getTopLevelGroup(groupIndex)
            (item as? QuestionnaireSection)?.let {
                for (childIndex in 0 until (it.childCount + 1)) {
                    val child = it.getGroup(childIndex)
                    if (child is BaseQuestionItem<*> && child.question != null) {
                        val value = JsonObject(child.getUserAnswers())
                        val answer = Answer(
                            UUID.randomUUID().toString(),
                            LocalDate.now().toString(DateFormats.questionData),
                            child.question.uuid,
                            value
                        )
                        val answersOnSameQuestion = answers.filter { predicate -> predicate.questionUuid == child.question.uuid }
                        if (answersOnSameQuestion.isEmpty()) {
                            answers.add(answer)
                        } else {
                            val newValue = answersOnSameQuestion.firstOrNull()?.value?.plus(value)
                            newValue?.let {
                                answersOnSameQuestion.firstOrNull()?.value = JsonObject(it)
                            }
                        }
                    }
                }
            }
        }

        viewModel.task.value?.let { task ->
            task.linkedContact = viewModel.selectedContact
            task.questionnaireResult =
                QuestionnaireResult(viewModel.questionnaire?.uuid ?: "", answers)
            if (task.uuid.isNullOrEmpty()) {
                task.uuid = UUID.randomUUID().toString()
            }
            answers.firstOrNull { it.questionUuid == CONTACT_TYPE_UUID }?.value?.get("value")?.jsonPrimitive?.content.let {
                task.taskContext = it
            }
            task.status = calculateStatus()
            task.communication = viewModel.communicationType.value
            viewModel.dateOfLastExposure.value?.let {
                task.dateOfLastExposure = it
            }
            viewModel.category.value?.let { newCategory ->
                task.category = newCategory
            }
            viewModel.saveChangesToTask(task)
        }

        view?.hideKeyboard()
        view?.postDelayed({
            findNavController().popBackStack()
        }, 400)
    }

    private fun isCommunicationTypeQuestion(question: Question): Boolean {
        var foundTrigger = false
        question.answerOptions?.forEach {
            if (it?.trigger == COMMUNICATION_STUFF || it?.trigger == COMMUNICATION_INDEX) {
                foundTrigger = true
            }
        }
        return foundTrigger
    }

    private fun calculateStatus(): Int {
        var status = 0
        if (itemsStorage?.classificationSection?.isCompleted() == true) {
            status++
        }
        if (itemsStorage?.contactDetailsSection?.isCompleted() == true) {
            status++
        }
        if (itemsStorage?.informSection?.isCompleted() == true) {
            status++
        }
        return status
    }

    companion object { // TODO not use UUID's in future, they might change!
        const val CONTACT_TYPE_UUID = "37d818ed-9499-4b9a-9771-725467368390"
        const val COMMUNICATION_STUFF = "communication_staff"
        const val COMMUNICATION_INDEX = "communication_index"
    }

}