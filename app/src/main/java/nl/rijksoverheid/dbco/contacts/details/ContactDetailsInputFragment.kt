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
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.contacts.details.TaskDetailItemsStorage.Companion.ANSWER_EARLIER
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.input.BaseQuestionItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.removeAllChildren
import org.joda.time.LocalDateTime
import java.util.*


class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()
    private val viewModel by viewModels<TasksDetailViewModel>()
    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()
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

        itemsStorage = TaskDetailItemsStorage(
            viewModel,
            view.context,
            viewLifecycleOwner,
            appLifecycleViewModel.getFeatureFlags()
        ).apply {
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

            if (hasCategory) {
                if (itemsStorage?.contactDetailsSection?.isExpanded == false) {
                    itemsStorage?.contactDetailsSection?.onToggleExpanded()
                }
            }

            itemsStorage?.contactDetailsSection?.setEnabled(categoryHasRisk)
            itemsStorage?.refreshContactDetailsSection()

            itemsStorage?.informSection?.setEnabled(categoryHasRisk)
            itemsStorage?.refreshInformSection()

            binding.saveButton.text =
                if (it == Category.NO_RISK) getString(R.string.cancel) else getString(R.string.save)
        })

        viewModel.communicationType.observe(viewLifecycleOwner, {
            checkIfInformSectionComplete()
            checkIfContactDetailsSectionComplete()
            itemsStorage?.refreshInformSection()
            if (it == CommunicationType.Index) {
                binding.saveButton.apply {
                    backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.gray_lighter)
                    setTextColor(context.getColor(R.color.purple))
                }
            } else {
                binding.saveButton.apply {
                    backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.color_primary)
                    setTextColor(context.getColor(R.color.white))
                }
            }
        })

        viewModel.hasEmailOrPhone.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
        })
        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
            itemsStorage?.refreshInformSection()
            binding.saveButton.text =
                // If the user selects Earlier, and the task has been saved before, delete it
                // Or, if it hasn't been saved yet, cancel. If the task is valid, save instead
                if (it == ANSWER_EARLIER && task.source == Source.App && task.uuid != null) {
                    getString(R.string.delete)
                }else if (it == ANSWER_EARLIER && task.source == Source.App && task.uuid == null) {
                    getString(R.string.cancel)
                } else { getString(R.string.save) }
        })

        refreshClassificationSection()

        binding.saveButton.setOnClickListener {
            if (viewModel.dateOfLastExposure.value == ANSWER_EARLIER && task.source == Source.App && task.uuid != null) {
                showDeleteItemDialog(it)
                return@setOnClickListener
            }

            if (viewModel.category.value == Category.NO_RISK || (viewModel.dateOfLastExposure.value == ANSWER_EARLIER && task.uuid == null)) {
                findNavController().popBackStack()
                return@setOnClickListener
            }

            if (viewModel.communicationType.value == CommunicationType.Index && viewModel.task.value?.didInform == false) {
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
            // Set informed to true, close the dialog and check if all answers were added
            viewModel.task.value?.didInform = true
            dialog.dismiss()
            checkIfInformSectionComplete()
            collectAnswers()
        }
        builder.setNeutralButton(R.string.contact_inform_action_inform_now) { dialog, _ ->
            // Close dialog and focus on inform section
            dialog.dismiss()
            itemsStorage?.informSection?.isExpanded = true
            itemsStorage?.classificationSection?.isExpanded = false
            itemsStorage?.contactDetailsSection?.isExpanded = false
        }
        builder.setNegativeButton(R.string.contact_inform_action_inform_later) { dialog, _ ->
            // Index will inform later. Close dialog and save answers already given
            dialog.dismiss()
            collectAnswers()
        }
        builder.create().show()
    }

    private fun refreshClassificationSection() {
        itemsStorage?.classificationSection?.removeAllChildren()
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            if (question.group == Group.Classification) {
                itemsStorage?.addClassificationQuestions(
                    question,
                    itemsStorage?.classificationSection
                )
            }
        }
    }

    private fun checkIfContactDetailsSectionComplete() {
        itemsStorage?.contactDetailsSection?.setCompleted(
            viewModel.hasEmailOrPhone.value == true &&
                    viewModel.communicationType.value != null &&
                    viewModel.dateOfLastExposure.value != null
        )
    }

    private fun checkIfInformSectionComplete() {
        itemsStorage?.informSection?.setCompleted(
            when (viewModel.communicationType.value) {
                CommunicationType.Index -> viewModel.task.value?.didInform == true
                CommunicationType.Staff -> viewModel.hasEmailOrPhone.value == true
                else -> false
            }
        )
    }

    private fun collectAnswers() {
        val answers = mutableListOf<Answer>()
        for (groupIndex: Int in 0 until adapter.groupCount) {
            val item = adapter.getTopLevelGroup(groupIndex)
            (item as? QuestionnaireSection)?.let { section ->
                for (childIndex in 0 until (section.childCount + 1)) {
                    val child = section.getGroup(childIndex)
                    if (child is BaseQuestionItem<*> && child.question != null) {
                        if (child == itemsStorage?.dateOfLastExposureItem) {
                            continue // skip date of last exposure item, it shouldn't be sent to server
                        }
                        val question: Question = child.question
                        var value: JsonObject? = JsonObject(child.getUserAnswers())
                        // override logic for classification questions
                        if (question.uuid == itemsStorage?.classificationQuestion?.uuid) {
                            value = itemsStorage?.getClassificationAnswerValue()
                        }
                        val answer = Answer(
                            UUID.randomUUID().toString(),
                            LocalDateTime.now().toString(DateFormats.questionData),
                            question.uuid,
                            value
                        )
                        val answersOnSameQuestion =
                            answers.filter { predicate -> predicate.questionUuid == question.uuid }
                        if (answersOnSameQuestion.isEmpty()) {
                            answers.add(answer)
                        } else if (value != null) {
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

    companion object { // TODO not use UUID's in future, they might change!
        const val CONTACT_TYPE_UUID = "37d818ed-9499-4b9a-9771-725467368390"
        const val COMMUNICATION_STAFF = "communication_staff"
        const val COMMUNICATION_INDEX = "communication_index"
    }

}