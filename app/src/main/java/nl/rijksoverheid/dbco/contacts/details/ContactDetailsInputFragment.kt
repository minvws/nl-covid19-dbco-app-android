/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.details

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.questionnaire.data.entity.Group
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionType
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.removeHtmlTags
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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

        val task = args.indexTask ?: Task(taskType = "contact", source = "app")
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

        viewModel.category.observe(viewLifecycleOwner, {
            val hasCategory = it != null
            itemsStorage?.classificationSection?.setCompleted(hasCategory)
            if (hasCategory) {
                if (itemsStorage?.classificationSection?.isExpanded == true) {
                    itemsStorage?.classificationSection?.onToggleExpanded()
                }
                if (itemsStorage?.contactDetailsSection?.isExpanded == false) {
                    itemsStorage?.contactDetailsSection?.onToggleExpanded()
                }
            }

            val categoryHasRisk = hasCategory && it != Category.NO_RISK
            itemsStorage?.contactDetailsSection?.setEnabled(categoryHasRisk)
            itemsStorage?.informSection?.setEnabled(categoryHasRisk)
        })

        viewModel.communicationType.observe(viewLifecycleOwner, {
            checkIfInformSectionComplete()
            checkIfContactDetailsSectionComplete()
        })

        viewModel.hasEmailOrPhone.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
        })
        viewModel.dateOfLastExposure.observe(viewLifecycleOwner, {
            checkIfContactDetailsSectionComplete()
        })

        addQuestionnaireSections()
        addInformSection()

        binding.saveButton.setOnClickListener {
            if (viewModel.communicationType.value == CommunicationType.Index && viewModel.task.value?.contactIsInformedAlready == false) {
                showDidYouInformDialog(view)
                return@setOnClickListener
            }
            collectAnswers()
        }
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

    private fun addQuestionnaireSections() {
        var communicationTypeQuestionFound = false

        // add questions to sections, based on their "group"
        val questions = viewModel.questionnaire?.questions?.filterNotNull()
        questions?.forEach { question ->
            val section =
                when (question.group) {
                    Group.ContactDetails -> itemsStorage?.contactDetailsSection
                    Group.Classification -> itemsStorage?.classificationSection
                    else -> null
                }

            // add hardcoded "date of last exposure" question before communication type question
            if (isCommunicationTypeQuestion(question)) {
                itemsStorage?.let {
                    it.contactDetailsSection.add(it.dateOfLastExposureItem)
                    communicationTypeQuestionFound = true
                }
            }

            when (question.questionType) {
                QuestionType.Multiplechoice -> {
                    addMultiChoiceItem(question, section)
                }
                QuestionType.Open -> {
                    section?.add(SingleInputItem(requireContext(), question))
                }
                QuestionType.Date -> {
                    section?.add(DateInputItem(requireContext(), question))
                }
                QuestionType.ContactDetails -> {
                    addContactDetailsItems(section, question)
                }
                QuestionType.ClassificationDetails -> {
                    addClassificationQuestions(section)
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
                            viewModel.questionnaireResult?.getAnswerByUuid(question.uuid!!)
                        )
                    )
                }
                size == 2 -> {
                    sectionToAddTo?.add(
                        QuestionTwoOptionsItem(
                            question,
                            {
                                when (it.trigger) {
                                    COMMUNICATION_STUFF -> viewModel.communicationType.value =
                                        CommunicationType.Staff
                                    COMMUNICATION_INDEX -> viewModel.communicationType.value =
                                        CommunicationType.Index
                                }
                            },
                            null,
                            viewModel.questionnaireResult?.getAnswerByUuid(question.uuid!!)
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
                },
                EmailAddressItem(viewModel.selectedContact?.email, question) {
                    viewModel.selectedContact?.email = it
                }
            )
        )
    }

    private fun addInformSection() {

        // TODO message should be dynamic
        val message = getString(R.string.contact_section_inform_content_details, "9 november", "10")
        val plainMessage = message.removeHtmlTags()

        itemsStorage?.informSection?.apply {
            add(SubHeaderItem(R.string.contact_section_inform_content_header))
            add(ParagraphItem(message))
            add(ButtonItem(
                getString(R.string.contact_section_inform_copy),
                {
                    val clipboard =
                        context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newHtmlText("Copied Text", plainMessage, message)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        context,
                        getString(R.string.contact_section_inform_copied),
                        Toast.LENGTH_LONG
                    ).show()
                }
            ))

            // add "Call $name" button if phone is set
            viewModel.selectedContact?.number?.let {
                add(
                    ButtonItem(
                        getString(
                            R.string.contact_section_inform_call,
                            viewModel.selectedContact?.firstName
                        ),
                        {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it}"))
                            startActivity(intent)
                        },
                    )
                )
            }
        }
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
                }
            })

            viewModel.durationRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.distanceRiskItem)
                } else {
                    section?.remove(itemsStorage.distanceRiskItem)
                    section?.remove(itemsStorage.otherRiskItem)
                }
            })

            viewModel.distanceRisk.observe(viewLifecycleOwner, {
                if (it == false) {
                    section?.add(itemsStorage.otherRiskItem)
                } else {
                    section?.remove(itemsStorage.otherRiskItem)
                }
            })

            viewModel.otherRisk.observe(viewLifecycleOwner, {
                // TODO add "no risk" label
            })
        }
    }

    private fun collectAnswers() {

        val answerCollector = HashMap<String, Map<String, Any>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())

        for (groupIndex: Int in 0 until adapter.groupCount) {
            val item = adapter.getTopLevelGroup(groupIndex)
            (item as? QuestionnaireSection)?.let {
                for (childIndex in 0 until (it.childCount + 1)) {
                    val child = it.getGroup(childIndex)
                    if (child is BaseQuestionItem<*>) {
                        val answer = HashMap<String, Any>()
                        if (child.question != null) {
                            answer.put("questionUuid", child.question.uuid!!)
                            answer.put("lastModified", sdf.format(Date()))
                            val data = child.getUserAnswers()
                            answer.putAll(data)

                            // Combine with previous entry if found
                            if (answerCollector.containsKey(child.question.uuid)) {
                                val prev =
                                    answerCollector.get(child.question.uuid) as Map<String, Any>
                                answer.putAll(prev)
                            }
                            answerCollector.put(child.question.uuid, answer)
                        } else {
                            Timber.d("Got child without question")
                        }
                    }
                }
            }
        }
        // Extract all actual answers, discard the keys since they're already in the answer
        val finalAnswers = ArrayList<JsonObject>()
        answerCollector.entries.forEach { answerField ->
            val answerValue = answerField.value
            val newMap = HashMap<String, JsonElement>()
            for ((key, value) in answerValue) {
                if (value is Boolean) {
                    newMap.put(key, Json.encodeToJsonElement(Boolean.serializer(), value))
                } else if (value is String) {
                    newMap.put(key, Json.encodeToJsonElement(String.serializer(), value))
                }
            }

            finalAnswers.add(JsonObject(newMap))
        }

        viewModel.task.value?.let { task ->
            task.linkedContact = viewModel.selectedContact
            task.questionnaireResult =
                QuestionnaireResult(viewModel.questionnaire?.uuid!!, JsonArray(finalAnswers))
            if (task.uuid.isNullOrEmpty()) {
                task.uuid = UUID.randomUUID().toString()
            }
            val contactType = answerCollector[CONTACT_TYPE_UUID]?.get("value")
            contactType?.let { type ->
                task.taskContext = type.toString()
            }
            task.status = calculateStatus()
            task.communication = viewModel.communicationType.value
            viewModel.category.value?.let { newCategory ->
                task.category = newCategory
            }
            viewModel.saveChangesToTask(task)
        }

        //Timber.d("Answers are $answerCollector")
        findNavController().popBackStack()
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