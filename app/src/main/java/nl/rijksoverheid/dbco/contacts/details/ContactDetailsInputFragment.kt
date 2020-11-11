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
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.questionnaire.data.entity.*
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
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
    private var itemsStorage:ItemsStorage? = null

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

        viewModel.category.observe(viewLifecycleOwner, {
            itemsStorage?.classificationSection?.setCompleted(it != null)
            val categoryHasRisk = it != null && it != Category.NO_RISK
            itemsStorage?.contactDetailsSection?.setEnabled(categoryHasRisk)
            itemsStorage?.informSection?.setEnabled(categoryHasRisk)
        })

        viewModel.selectedContact = args.selectedContact
        viewModel.setTask(args.indexTask ?: Task(taskType = "contact", source = "app"))

        itemsStorage = ItemsStorage(viewModel, view.context).apply {
            adapter.add(classificationSection)
            adapter.add(contactDetailsSection)
            adapter.add(informSection)
        }

        binding.toolbar.title = args.selectedContact?.displayName ?: resources.getString(R.string.mycontacts_add_contact)

        addQuestionnaireSections()
        addContactInformSection()

        binding.saveButton.setOnClickListener {
            collectAnswers()
        }
    }

    private fun addQuestionnaireSections() {

        // add questions to sections, based on their "group"
        viewModel.questionnaire?.questions?.filterNotNull()?.forEach { question ->
            val section =
                    when (question.group) {
                        Group.ContactDetails -> itemsStorage?.contactDetailsSection
                        Group.Classification -> itemsStorage?.classificationSection
                        else -> null
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
                                    {},
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

        val nameParts: List<String> =
                viewModel.selectedContact?.displayName?.split(" ", limit = 2) ?: listOf("", "")
        val firstName = nameParts[0] ?: ""
        val lastName = if (nameParts.size > 1) {
            nameParts[1]
        } else {
            ""
        }

        val primaryPhone = if (!viewModel.selectedContact?.number.isNullOrEmpty()) {
            viewModel.selectedContact?.number
        } else {
            ""
        }

        val primaryEmail = if (!viewModel.selectedContact?.email.isNullOrEmpty()) {
            viewModel.selectedContact?.email
        } else {
            ""
        }

        val previousAnswer = viewModel.questionnaireResult?.getAnswerByUuid(question.uuid!!)

        sectionToAddTo?.addAll(
                listOf(
                        ContactNameItem(firstName, lastName, question, previousAnswer),
                        PhoneNumberItem(primaryPhone, question, previousAnswer),
                        EmailAddressItem(primaryEmail, question, previousAnswer)
                )
        )
    }

    private fun addContactInformSection() {

        // TODO message should be dynamic
        val message = getString(R.string.contact_section_inform_content_details, "9 november", "10")
        val plainMessage = message.removeHtmlTags()

        itemsStorage?.informSection?.apply {
            add(SubHeaderItem(R.string.contact_section_inform_content_header))
            add(ParagraphItem(message))
            add(ButtonItem(
                    getString(R.string.contact_section_inform_copy),
                    {
                        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newHtmlText("Copied Text", plainMessage, message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, getString(R.string.contact_section_inform_copied), Toast.LENGTH_LONG).show()
                    }
            ))

            // add "Call $name" button if phone is set
            viewModel.selectedContact?.number?.let {
                add(ButtonItem(
                        getString(R.string.contact_section_inform_call, viewModel.selectedContact?.displayName),
                        {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it}"))
                            startActivity(intent)
                        },
                ))
            }
        }
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
            task.questionnaireResult = QuestionnaireResult(viewModel.questionnaire?.uuid!!, JsonArray(finalAnswers))
            task.label = viewModel.selectedContact?.displayName
            if (task.uuid.isNullOrEmpty()) {
                task.uuid = UUID.randomUUID().toString()
            }
            val contactType = answerCollector[CONTACT_TYPE_UUID]?.get("value")
            contactType?.let { type ->
                task.taskContext = type.toString()
            }
            viewModel.category.value?.let { newCategory ->
                task.category = newCategory
            }
            viewModel.saveChangesToTask(task)
        }


        //Timber.d("Answers are $answerCollector")
        findNavController().popBackStack()
    }

    companion object {
        const val CONTACT_TYPE_UUID = "37d818ed-9499-4b9a-9771-725467368390"
    }

}