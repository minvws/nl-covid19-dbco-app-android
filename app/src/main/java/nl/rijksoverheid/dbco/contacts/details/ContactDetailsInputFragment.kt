/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jay.widget.StickyHeadersLinearLayoutManager
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
import nl.rijksoverheid.dbco.databinding.FourOptionsDialogContentBinding
import nl.rijksoverheid.dbco.items.ui.VerticalSpaceItem
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import java.util.*

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val taskDetailViewModel: TasksDetailViewModel by viewModels()

    private val selfBcoCaseViewModel: SelfBcoCaseViewModel by activityViewModels()

    private val appViewModel: AppViewModel by activityViewModels()

    private val args: ContactDetailsInputFragmentArgs by navArgs()

    private val adapter = ContactDetailsAdapter()

    private lateinit var itemsStorage: TaskDetailItemsStorage
    private lateinit var binding: FragmentContactInputBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentContactInputBinding.bind(view)

        if (savedInstanceState == null) {
            taskDetailViewModel.init(args.indexTaskUuid)
        }
        initToolbar()
        initContent()
        initItemStorage(
            enabled = args.enabled,
            newTask = args.newTask
        )

        taskDetailViewModel.category.observe(viewLifecycleOwner, { cat -> onCategoryChanged(cat) })
        taskDetailViewModel.communicationType.observe(viewLifecycleOwner, { onTypeChanged() })
        taskDetailViewModel.hasEmailOrPhone.observe(viewLifecycleOwner, { onHasEmailOrPhoneChanged() })
        taskDetailViewModel.dateOfLastExposure.observe(viewLifecycleOwner, { onLastExposureChanged() })
        taskDetailViewModel.name.observe(viewLifecycleOwner, { onNameChanged() })

        if (!args.enabled) {
            showDisabledDialog()
        }
    }

    private fun initToolbar() {
        binding.toolbar.backButton.setOnClickListener { checkUnsavedChanges() }
        binding.delete.isVisible = taskDetailViewModel.isDeletionPossible(args.enabled)
        binding.delete.setOnClickListener { showDeleteItemDialog(noRisk = false) }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            checkUnsavedChanges()
        }
    }

    private fun initContent() {
        binding.content.adapter = adapter
        binding.content.layoutManager = StickyHeadersLinearLayoutManager<ContactDetailsAdapter>(
            requireContext()
        )
        var contactName = taskDetailViewModel.task.linkedContact?.getDisplayName()
        if (contactName.isNullOrEmpty()) {
            contactName = getString(R.string.mycontacts_add_contact)
        }
        adapter.add(
            HeaderItem(
                text = contactName,
                horizontalMargin = R.dimen.activity_horizontal_margin
            )
        )
        updateButton()
    }

    private fun initItemStorage(
        enabled: Boolean,
        newTask: Boolean
    ) {
        itemsStorage = TaskDetailItemsStorage(
            enabled = enabled,
            newTask = newTask,
            taskDetailViewModel = taskDetailViewModel,
            selfBcoCaseViewModel = selfBcoCaseViewModel,
            context = requireContext(),
            viewLifecycleOwner = viewLifecycleOwner,
            featureFlags = appViewModel.getFeatureFlags(),
            guidelines = appViewModel.getGuidelines()
        ).apply {
            if (taskDetailViewModel.task.source != Source.Portal) {
                adapter.add(classificationSection)
            } else {
                contactDetailsSection.setSectionNumber(1)
                informSection.setSectionNumber(2)
            }

            adapter.add(contactDetailsSection)
            adapter.add(informSection)

            classificationSection.removeAllChildren()
            classificationSection.add(VerticalSpaceItem(R.dimen.activity_vertical_margin))
            classificationSection.add(dateOfLastExposureItem)
            val questions = taskDetailViewModel.questionnaire?.questions?.filterNotNull() ?: emptyList()
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
        val task = taskDetailViewModel.task

        val dateOfLastExposure = taskDetailViewModel.dateOfLastExposure.value
        val category = taskDetailViewModel.category.value

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
                    shouldCancel -> checkUnsavedChanges()
                    shouldClose -> findNavController().popBackStack()
                    taskDetailViewModel.task.shouldInform -> showDidYouInformDialog()
                    else -> saveContact()
                }
            }
        }
        setButtonType(binding.saveButton)
    }

    private fun setButtonType(saveButton: MaterialButton) {
        val featureFlags = appViewModel.getFeatureFlags()
        val byGGD = !taskDetailViewModel.commByIndex()
        val callAndCopyDisabled = !taskDetailViewModel.callingEnabled(featureFlags) &&
                !taskDetailViewModel.copyEnabled(featureFlags)
        if (byGGD || callAndCopyDisabled) {
            saveButton.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_primary
            )
            saveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
        } else {
            saveButton.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_secondary
            )
            saveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
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
            updateButton()
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
            taskDetailViewModel.deleteCurrentTask()
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
                cancel()
            }
            builder.create().show()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun cancel() {
        taskDetailViewModel.onCancelled(args.enabled)
        findNavController().popBackStack()
    }

    private fun hasMadeChanges(): Boolean {
        val updatedQuestionnaire = collectAnswers() != taskDetailViewModel.getQuestionnaireAnswers()
        return updatedQuestionnaire || taskDetailViewModel.hasUpdatedExposureDate()
    }

    private fun showDidYouInformDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val name = taskDetailViewModel.task.linkedContact?.firstName
        val string = getString(
            R.string.contact_inform_prompt_title,
            if (!name.isNullOrBlank()) name else getString(R.string.inform_header_this_person)
        )
        builder.setTitle(string)
        val content = FourOptionsDialogContentBinding.inflate(LayoutInflater.from(context))
        content.message.text = getString(R.string.contact_inform_prompt_message)
        content.button1.text = getString(R.string.contact_inform_option_done)
        content.button2.text = getString(R.string.contact_inform_action_inform_later)
        content.button3.text = getString(R.string.contact_inform_action_inform_now)
        content.button4.text = getString(R.string.contact_inform_action_inform_not)
        builder.setView(content.root)
        val dialog = builder.create()
        content.button1.setOnClickListener {
            val date = LocalDateTime.now().toString(DateFormats.informDate)
            taskDetailViewModel.task.informedByIndexAt = date
            dialog.dismiss()
            checkIfInformSectionComplete()
            saveContact()
        }
        content.button2.setOnClickListener {
            dialog.dismiss()
            saveContact()

        }
        content.button3.setOnClickListener {
            dialog.dismiss()
            itemsStorage.informSection.isExpanded = true
            itemsStorage.classificationSection.isExpanded = false
            itemsStorage.contactDetailsSection.isExpanded = false
            binding.content.smoothScrollToPosition(adapter.itemCount - 1)
        }
        content.button4.setOnClickListener {
            taskDetailViewModel.task.notGoingToBeInformedByIndex = true
            dialog.dismiss()
            saveContact()
        }
        dialog.show()
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
            taskDetailViewModel.hasEmailOrPhone.value == true &&
                    taskDetailViewModel.communicationType.value != null
        )
    }

    private fun checkIfInformSectionComplete() {
        itemsStorage.informSection.setCompleted(
            when (taskDetailViewModel.communicationType.value) {
                Index -> taskDetailViewModel.task.didInform
                Staff -> taskDetailViewModel.hasEmailOrPhone.value == true
                else -> false
            }
        )
    }

    private fun saveContact() {
        val answers = collectAnswers()
        with(taskDetailViewModel.task) {
            questionnaireResult = QuestionnaireResult(
                taskDetailViewModel.questionnaire?.uuid ?: "",
                answers
            )
            if (isLocal()) {
                label = if (!linkedContact?.getDisplayName().isNullOrBlank()) {
                    linkedContact?.getDisplayName()
                } else {
                    if (label.isNullOrBlank()) {
                        getString(R.string.mycontacts_name_unknown)
                    } else {
                        label
                    }
                }
            }
            communication = taskDetailViewModel.communicationType.value
            taskDetailViewModel.dateOfLastExposure.value?.let { dateOfLastExposure = it }
            taskDetailViewModel.category.value?.let { newCategory -> category = newCategory }
            taskDetailViewModel.saveTask()
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
                            taskDetailViewModel.category.value?.let { category ->
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
        val oldAnswers = taskDetailViewModel.getQuestionnaireAnswers()
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

    private fun Task.isLocalAndSaved() = isLocal() && isSaved()

    private fun Task.isLocalAndNotSaved() = isLocal() && !isSaved()
}