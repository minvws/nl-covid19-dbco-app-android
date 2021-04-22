/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.AppViewModel
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.details.TaskDetailItemsStorage.Companion.ANSWER_EARLIER
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.input.BaseQuestionItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.bcocase.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.removeAllChildren
import org.joda.time.LocalDateTime
import nl.rijksoverheid.dbco.contacts.data.entity.Category.NO_RISK
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType.Index
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType.Staff
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import java.util.*

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val viewModel: TasksDetailViewModel by viewModels()

    private val appViewModel: AppViewModel by activityViewModels()

    private val args: ContactDetailsInputFragmentArgs by navArgs()

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var itemsStorage: TaskDetailItemsStorage
    private lateinit var binding: FragmentContactInputBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactInputBinding.bind(view)

        if (savedInstanceState == null) {
            viewModel.init(args.indexTaskUuid)
        }
        initToolbar()
        initContent()
        initItemStorage(args.enabled)

        viewModel.category.observe(viewLifecycleOwner, { cat -> onCategoryChanged(cat) })
        viewModel.communicationType.observe(viewLifecycleOwner, { onTypeChanged() })
        viewModel.hasEmailOrPhone.observe(viewLifecycleOwner, { onHasEmailOrPhoneChanged() })
        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, { onLastExposureChanged() })
        viewModel.name.observe(viewLifecycleOwner, { onNameChanged() })

        if (!args.enabled) {
            showDisabledDialog()
        }
    }

    private fun initToolbar() {
        binding.toolbar.backButton.setOnClickListener { checkUnsavedChanges() }
        binding.delete.isVisible = viewModel.task.isLocalAndSaved() && args.enabled
        binding.delete.setOnClickListener { showDeleteItemDialog(noRisk = false) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            checkUnsavedChanges()
        }
    }

    private fun initContent() {
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            QuestionnaireSectionDecorator(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )
        var contactName = viewModel.task.linkedContact?.getDisplayName()
        if (contactName.isNullOrEmpty()) {
            contactName = getString(R.string.mycontacts_add_contact)
        }
        adapter.add(HeaderItem(contactName))
        updateButton()
    }

    private fun initItemStorage(enabled: Boolean) {
        itemsStorage = TaskDetailItemsStorage(
            enabled,
            viewModel,
            requireContext(),
            viewLifecycleOwner,
            appViewModel.getFeatureFlags()
        ).apply {
            if (viewModel.task.source != Source.Portal) {
                adapter.add(classificationSection)
            } else {
                contactDetailsSection.setSectionNumber(1)
                informSection.setSectionNumber(2)
            }

            adapter.add(contactDetailsSection)
            adapter.add(informSection)

            classificationSection.removeAllChildren()
            classificationSection.add(dateOfLastExposureItem)
            val questions = viewModel.questionnaire?.questions?.filterNotNull() ?: emptyList()
            questions.forEach { question ->
                if (question.group == Group.Classification) {
                    addClassificationQuestions(question, classificationSection)
                }
            }
        }
    }

    private fun onLastExposureChanged() {
        itemsStorage.refreshInformSection()
        updateButton()
    }

    private fun onNameChanged() {
        // This value can be broadcast again while the app is scrolling due to views re-binding.
        // If this happens, the app can crash due to the RV not accepting changes during layout computes
        // Adding this check makes sure the section is only refreshed when the user is actively filling
        // in their data rather than during a scroll.
        if (!binding.content.isComputingLayout) {
            itemsStorage.refreshInformSection()
        }
    }

    /**
     * Updates the text, click listener and colors of the save button for
     * different scenario's
     */
    private fun updateButton() {
        val task = viewModel.task

        val dateOfLastExposure = viewModel.dateOfLastExposure.value
        val category = viewModel.category.value

        val noRisk = category == NO_RISK
        val noExposure = dateOfLastExposure == ANSWER_EARLIER

        val shouldClose = !args.enabled
        val shouldCloseWithWarning = (noExposure || noRisk) && task.isLocalAndSaved()
        val shouldCancel = category == null
        val shouldCancelWithWarning = ((noExposure || noRisk) && task.isLocalAndNotSaved())

        with(binding.saveButton) {
            text = when {
                shouldClose || shouldCloseWithWarning -> getString(R.string.close)
                shouldCancel || shouldCancelWithWarning -> getString(R.string.cancel)
                else -> getString(R.string.save)
            }
            setOnClickListener {
                when {
                    shouldCloseWithWarning || shouldCancelWithWarning -> showDeleteItemDialog(noRisk = true)
                    shouldCancel || shouldClose -> findNavController().popBackStack()
                    indexShouldInform() -> showDidYouInformDialog()
                    else -> saveContact()
                }
            }
        }
    }

    private fun onHasEmailOrPhoneChanged() {
        checkIfContactDetailsSectionComplete()
        // This value can be broadcast again while the app is scrolling due to views re-binding.
        // If this happens, the app can crash due to the RV not accepting changes during layout computes
        // Adding this check makes sure the section is only refreshed when the user is actively filling
        // in their data rather than during a scroll.
        if (!binding.content.isComputingLayout) {
            itemsStorage.refreshInformSection()
        }
    }

    private fun onTypeChanged() {
        checkIfInformSectionComplete()
        checkIfContactDetailsSectionComplete()
        itemsStorage.refreshInformSection()
        updateButton()
    }

    private fun onCategoryChanged(category: Category?) {
        val hasCategory = category != null
        val categoryHasRisk = category != null && category != NO_RISK

        itemsStorage.classificationSection.setEnabled(true)
        itemsStorage.classificationSection.setCompleted(hasCategory)

        if (hasCategory && !itemsStorage.contactDetailsSection.isExpanded) {
            itemsStorage.contactDetailsSection.onToggleExpanded()
        }

        itemsStorage.contactDetailsSection.setEnabled(categoryHasRisk)
        itemsStorage.refreshContactDetailsSection()

        itemsStorage.informSection.setEnabled(categoryHasRisk)
        itemsStorage.refreshInformSection()

        updateButton()
    }

    private fun showDeleteItemDialog(noRisk: Boolean) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        if (noRisk) {
            builder.setTitle(R.string.delete_contact_no_risk_title)
            builder.setMessage(R.string.delete_contact_no_risk_message)
        } else {
            builder.setMessage(R.string.delete_contact_message)
        }
        builder.setPositiveButton(R.string.delete_short) { dialog, _ ->
            viewModel.deleteCurrentTask()
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.back) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun checkUnsavedChanges() {
        if (args.enabled && hasMadeChanges()) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(getString(R.string.unsaved_changes_title))
            builder.setMessage(getString(R.string.unsaved_changes_message))
            builder.setPositiveButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack()
            }
            builder.create().show()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun hasMadeChanges(): Boolean {
        val updatedQuestionnaire = collectAnswers() != viewModel.getQuestionnaireAnswers()
        return updatedQuestionnaire || viewModel.hasUpdatedExposureDate()
    }

    private fun showDidYouInformDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val string = getString(
            R.string.contact_inform_prompt_title,
            viewModel.task.linkedContact?.getDisplayName() ?: ""
        )
        builder.setTitle(string)
        builder.setMessage(R.string.contact_inform_prompt_message)
        builder.setPositiveButton(R.string.contact_inform_option_done) { dialog, _ ->
            val date = LocalDateTime.now().toString(DateFormats.informDate)
            viewModel.task.informedByIndexAt = date
            dialog.dismiss()
            checkIfInformSectionComplete()
            saveContact()
        }
        builder.setNeutralButton(R.string.contact_inform_action_inform_now) { dialog, _ ->
            dialog.dismiss()
            itemsStorage.informSection.isExpanded = true
            itemsStorage.classificationSection.isExpanded = false
            itemsStorage.contactDetailsSection.isExpanded = false
        }
        builder.setNegativeButton(R.string.contact_inform_action_inform_later) { dialog, _ ->
            dialog.dismiss()
            saveContact()
        }
        builder.create().show()
    }

    private fun showDisabledDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.contact_disabled_prompt_title)
        builder.setMessage(R.string.contact_disabled_prompt_message)
        builder.setPositiveButton(
            R.string.contact_disabled_prompt_button
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun checkIfContactDetailsSectionComplete() {
        itemsStorage.contactDetailsSection.setCompleted(
            viewModel.hasEmailOrPhone.value == true &&
                    viewModel.communicationType.value != null
        )
    }

    private fun checkIfInformSectionComplete() {
        itemsStorage.informSection.setCompleted(
            when (viewModel.communicationType.value) {
                Index -> viewModel.task.didInform
                Staff -> viewModel.hasEmailOrPhone.value == true
                else -> false
            }
        )
    }

    private fun saveContact() {
        val answers = collectAnswers()
        with(viewModel.task) {
            questionnaireResult = QuestionnaireResult(
                viewModel.questionnaire?.uuid ?: "",
                answers
            )
            if (label.isNullOrEmpty()) {
                label = if (!linkedContact?.getDisplayName().isNullOrEmpty()) {
                    linkedContact?.getDisplayName()
                } else {
                    getString(R.string.mycontacts_name_unknown)
                }
            }
            communication = viewModel.communicationType.value
            viewModel.dateOfLastExposure.value?.let { dateOfLastExposure = it }
            viewModel.category.value?.let { newCategory -> category = newCategory }
            viewModel.saveTask()
        }

        view?.hideKeyboard()
        view?.postDelayed({
            findNavController().popBackStack()
        }, 400)
    }

    private fun collectAnswers(): List<Answer> {
        val currentAnswers = mutableListOf<Answer>()
        for (groupIndex: Int in 0 until adapter.groupCount) {
            val item = adapter.getTopLevelGroup(groupIndex)
            (item as? QuestionnaireSection)?.let { section ->
                for (childIndex in 0 until (section.childCount + 1)) {
                    val child = section.getGroup(childIndex)
                    if (child is BaseQuestionItem<*> && child.question != null) {
                        if (child == itemsStorage.dateOfLastExposureItem) {
                            continue // skip date of last exposure item, it shouldn't be sent to server
                        }
                        val question: Question = child.question
                        var value: JsonObject? = JsonObject(child.getUserAnswers())
                        // override logic for classification questions
                        if (question.uuid == itemsStorage.classificationQuestion?.uuid) {
                            val map = HashMap<String, JsonElement>()
                            viewModel.category.value?.let { category ->
                                map["value"] = JsonPrimitive(category.label)
                            }
                            value = JsonObject(map)
                        }
                        val answer = Answer(
                            UUID.randomUUID().toString(),
                            LocalDateTime.now().toString(DateFormats.questionData),
                            question.uuid,
                            value
                        )
                        val answersOnSameQuestion = currentAnswers.filter { predicate ->
                            predicate.questionUuid == question.uuid
                        }
                        if (answersOnSameQuestion.isEmpty()) {
                            currentAnswers.add(answer)
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
        return compareAnswers(currentAnswers)
    }

    /**
     * Compare values which are entered by user
     * If values have not changed since the last time we can use the old answers
     * to preserve the lastModified information
     */
    private fun compareAnswers(currentAnswers: List<Answer>): List<Answer> {
        val finalAnswers = mutableListOf<Answer>()
        val oldAnswers = viewModel.getQuestionnaireAnswers()
        for (answer in currentAnswers) {
            val old = oldAnswers.find { it.questionUuid == answer.questionUuid }
            if (old != null && old.value == answer.value) {
                // entered info did not change, can use old answer to keep lastModified
                finalAnswers.add(old)
            } else {
                // index has entered new information
                finalAnswers.add(answer)
            }
        }
        return finalAnswers
    }

    /**
     * @return whether the index should be asked whether the contact was informed by the index
     */
    private fun indexShouldInform(): Boolean {
        return viewModel.communicationType.value != Staff && !viewModel.task.didInform
    }

    private fun Task.isLocalAndSaved() = source == Source.App && isSaved()

    private fun Task.isLocalAndNotSaved() = source == Source.App && !isSaved()

    private fun Task.isSaved() = uuid != null
}