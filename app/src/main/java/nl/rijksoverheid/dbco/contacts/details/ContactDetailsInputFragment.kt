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
import androidx.lifecycle.ViewModelProvider
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
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
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
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
                TasksDetailViewModel::class.java
        )
    }
    private val answerSelectedListener: (AnswerOption) -> Unit = {

    }

    private val shownQuestions: ArrayList<Question> = ArrayList()

    private val classificationSection = QuestionnaireSection(
            this,
            QuestionnaireSectionHeader(
                    R.string.contact_section_typeofcontact_header,
                    R.string.contact_section_typeofcontact_subtext,
                    1
            ), true
    ).apply {
        adapter.add(this)
    }

    private val contactDetailsSection = QuestionnaireSection(
            this,
            QuestionnaireSectionHeader(
                    R.string.contact_section_contactdetails_header,
                    R.string.contact_section_contactdetails_subtext,
                    2
            ), false
    ).apply {
        adapter.add(this)
    }

    private val informSection = QuestionnaireSection(
            this,
            QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
            ), false
    ).apply {
        adapter.add(this)
    }

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

        viewModel.selectedContact = args.selectedContact
        viewModel.updateTask(args.indexTask ?: Task(taskType = "contact", source = "app"))

        binding.toolbar.title = args.selectedContact?.displayName
                ?: resources.getString(R.string.mycontacts_add_contact)

        addQuestionnaireSections()
        addContactInformSection()

        binding.saveButton.setOnClickListener {
            collectAnswers()
        }
    }

    private fun addQuestionnaireSections() {

        // add questions to sections, based on their "group"
        viewModel.questionnaire?.questions?.forEach { question ->
            val questionCategory = Category(args.indexTask?.category)
            if ((!questionCategory.category.isNullOrEmpty() && !question?.relevantForCategories!!.contains(
                            questionCategory
                    )) || question == null
            ) {
                Timber.d("Skipping $question")
                return@forEach
            }

            val section =
                    when (question.group) {
                        Group.ContactDetails -> contactDetailsSection
                        Group.Classification -> classificationSection
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

            shownQuestions.add(question)

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
                                    answerSelectedListener,
                                    viewModel.questionnaireResult?.getAnswerByUuid(question.uuid!!)
                            )
                    )
                }
                size == 2 -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    question,
                                    answerSelectedListener,
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

        informSection.apply {
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

    private val livedTogetherRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    "Woon je in hetzelfde huis of ben je langer dan 12 uur op dezelfde plek geweest?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(AnswerOption("Nee", null, "false"), AnswerOption("Ja", null, "true"))
            ),
            {
                when (it.value) {
                    "false" -> viewModel.livedTogetherRisk.value = false
                    "true" -> viewModel.livedTogetherRisk.value = true
                }
            },
            "livedTogetherRisk",
    )

    private val durationRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    "Was je langer dan 15 minuten op minder dan 1,5 meter afstand van elkaar?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.durationRisk.value = false
                    "true" -> viewModel.durationRisk.value = true
                }
            },
            "durationRisk",
    )

    private val distanceRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    "<ul><li>Binnen anderhalve meter van de ander gehoest of geniesd</li><li>Geknuffeld of gezoend</li><li>Ander lichamelijk contact</li><ul>",
                    "",
                    "Heb je een of meerdere van deze dingen tijdens jullie ontmoeting gedaan?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                when (it.value) {
                    "false" -> viewModel.distanceRisk.value = false
                    "true" -> viewModel.distanceRisk.value = true
                }
            },
            "distanceRisk",
    )

    private val otherRiskItem = QuestionTwoOptionsItem(
            Question(
                    null,
                    null,
                    "",
                    "Was je langer dan 15 minuten in dezelfde ruimte?",
                    QuestionType.ClassificationDetails,
                    Group.Classification,
                    listOf(
                            AnswerOption("Ja, denk het wel", null, "true"),
                            AnswerOption("Nee, denk het niet", null, "false")
                    )
            ),
            {
                // TODO add "no risk" label
            },
            "otherRisk",
    )

    private fun addClassificationQuestions(
            section: QuestionnaireSection?
    ) {

        section?.add(livedTogetherRiskItem) // always added

        viewModel.livedTogetherRisk.observe(viewLifecycleOwner, {
            if (it == false) {
                section?.add(durationRiskItem)
            } else {
                section?.remove(distanceRiskItem)
                section?.remove(durationRiskItem)
                section?.remove(otherRiskItem)
            }
        })

        viewModel.durationRisk.observe(viewLifecycleOwner, {
            if (it == false) {
                section?.add(distanceRiskItem)
            } else {
                section?.remove(distanceRiskItem)
                section?.remove(otherRiskItem)
            }
        })

        viewModel.distanceRisk.observe(viewLifecycleOwner, {
            if (it == false) {
                section?.add(otherRiskItem)
            } else {
                section?.remove(otherRiskItem)
            }
        })
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

        viewModel.task.value?.let {
            it.linkedContact = viewModel.selectedContact
            it.questionnaireResult = QuestionnaireResult(viewModel.questionnaire?.uuid!!, JsonArray(finalAnswers))
            it.label = viewModel.selectedContact?.displayName
            if (it.uuid.isNullOrEmpty()) {
                it.uuid = UUID.randomUUID().toString()
            }
            val contactType = answerCollector[CONTACT_TYPE_UUID]?.get("value")
            contactType?.let { type ->
                it.taskContext = type.toString()
            }
            viewModel.saveChangesToTask(it)
        }


        //Timber.d("Answers are $answerCollector")
        findNavController().popBackStack()
    }

    companion object {
        const val CONTACT_TYPE_UUID = "37d818ed-9499-4b9a-9771-725467368390"
    }

}