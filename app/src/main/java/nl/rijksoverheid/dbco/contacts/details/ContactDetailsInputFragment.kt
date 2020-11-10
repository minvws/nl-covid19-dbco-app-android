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
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
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
        // TODO handle
    }

    private val shownQuestions: ArrayList<Question> = ArrayList()

    private lateinit var selectedTask: Task
    private lateinit var selectedContact: LocalContact
    private var questionnaireResult: QuestionnaireResult? = null
    private val natureOfContactQuestions = HashMap<String, Question>()

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

        selectedContact = args.selectedContact ?: LocalContact("-1", "Nieuw Contact")
        selectedTask = args.indexTask ?: Task(taskType = "contact", source = "app")

        if (selectedTask.questionnaireResult != null) {
            questionnaireResult = selectedTask.questionnaireResult
        }

        binding.toolbar.title = args.selectedContact?.displayName ?: resources.getString(R.string.mycontacts_add_contact)

        addQuestionnaireSections(args.selectedContact)
        addContactInformSection(args.selectedContact)

        binding.saveButton.setOnClickListener {
            collectAnswers()
        }
    }

    private fun addQuestionnaireSections(
            contactItem: LocalContact?
    ) {

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

                val sectionToAddTo =
                        when (question.group) {
                            Group.ContactDetails -> contactDetailsSection
                            Group.Classification -> classificationSection
                            else -> null
                        }


                when (question.questionType) {
                    QuestionType.Multiplechoice -> {
                        addMultiChoiceItem(question, sectionToAddTo)
                    }
                    QuestionType.Open -> {
                        sectionToAddTo?.add(SingleInputItem(requireContext(), question))
                    }
                    QuestionType.Date -> {
                        sectionToAddTo?.add(DateInputItem(requireContext(), question))
                    }
                    QuestionType.ContactDetails -> {
                        addContactDetailsItems(contactItem, sectionToAddTo, question)
                    }
                    QuestionType.ClassificationDetails -> {
                        addClassificationQuestions(question, sectionToAddTo, selectedTask)
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
                                    questionnaireResult?.getAnswerByUuid(question.uuid!!)
                            )
                    )
                }
                size == 2 -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    question,
                                    answerSelectedListener,
                                    null,
                                    questionnaireResult?.getAnswerByUuid(question.uuid!!)
                            )
                    )
                }
                else -> {
                }
            }
        }
    }

    private fun addContactDetailsItems(
            contactItem: LocalContact?,
            sectionToAddTo: QuestionnaireSection?,
            question: Question
    ) {
        val nameParts: List<String> =
                contactItem?.displayName?.split(" ", limit = 2) ?: listOf("", "")
        val firstName = nameParts[0] ?: ""
        val lastName = if (nameParts.size > 1) {
            nameParts[1]
        } else {
            ""
        }

        val primaryPhone = if (!contactItem?.number.isNullOrEmpty()) {
            contactItem?.number
        } else {
            ""
        }

        val primaryEmail = if (!contactItem?.email.isNullOrEmpty()) {
            contactItem?.email
        } else {
            ""
        }

        val previousAnswer = questionnaireResult?.getAnswerByUuid(question.uuid!!)

        sectionToAddTo?.addAll(
                listOf(
                        ContactNameItem(firstName, lastName, question, previousAnswer),
                        PhoneNumberItem(primaryPhone, question, previousAnswer),
                        EmailAddressItem(primaryEmail, question, previousAnswer)
                )
        )
    }

    private fun addContactInformSection(contact: LocalContact?) {

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
            contact?.number?.let {
                add(ButtonItem(
                        getString(R.string.contact_section_inform_call, contact.displayName),
                        {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it}"))
                            startActivity(intent)
                        },
                ))
            }
        }
    }


    private fun addClassificationQuestions(
            question: Question,
            sectionToAddTo: QuestionnaireSection?,
            task: Task?
    ) {
        val livedTogetherRisk = Question(
                null,
                null,
                question.uuid,
                "Woon je in hetzelfde huis of ben je langer dan 12 uur op dezelfde plek geweest?",
                QuestionType.ClassificationDetails,
                Group.Classification,
                listOf(AnswerOption("Nee", null, "false"), AnswerOption("Ja", null, "true"))
        )

        val durationRisk = Question(
                null,
                null,
                question.uuid,
                "Was je langer dan 15 minuten op minder dan 1,5 meter afstand van elkaar?",
                QuestionType.ClassificationDetails,
                Group.Classification,
                listOf(
                        AnswerOption("Ja, denk het wel", null, "true"),
                        AnswerOption("Nee, denk het niet", null, "false")
                )
        )

        val distanceRisk = Question(
                null,
                "Heb je één of meerdere van deze dingen tijdens jullie ontmoeting gedaan?<b><ul><li>Binnen anderhalve meter van de ander gehoest of geniesd</li><li>Geknuffeld of gezoend</li><li>Ander lichamelijk contact</li><ul>",
                question.uuid,
                "Heb je een of meerdere van deze dingen tijdens jullie ontmoeting gedaan?",
                QuestionType.ClassificationDetails,
                Group.Classification,
                listOf(
                        AnswerOption("Ja, denk het wel", null, "true"),
                        AnswerOption("Nee, denk het niet", null, "false")
                )
        )

        val otherRisk = Question(
                null,
                null,
                question.uuid,
                "Was je langer dan 15 minuten in dezelfde ruimte?",
                QuestionType.ClassificationDetails,
                Group.Classification,
                listOf(
                        AnswerOption("Ja, denk het wel", null, "true"),
                        AnswerOption("Nee, denk het niet", null, "false")
                )
        )

        val previousAnswer = questionnaireResult?.getAnswerByUuid(question.uuid!!)


        if (task?.category != null) {
            when (task.category) {
                "1" -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    livedTogetherRisk,
                                    answerSelectedListener,
                                    "livedTogetherRisk",
                                    previousAnswer
                            )
                    )
                    natureOfContactQuestions.put("livedTogetherRisk", livedTogetherRisk)
                }
                "2a" -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    livedTogetherRisk,
                                    answerSelectedListener,
                                    "livedTogetherRisk",
                                    previousAnswer
                            )
                    )
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    durationRisk,
                                    answerSelectedListener,
                                    "durationRisk",
                                    previousAnswer
                            )
                    )
                    natureOfContactQuestions["livedTogetherRisk"] = livedTogetherRisk
                    natureOfContactQuestions["durationRisk"] = durationRisk
                }
                "2b" -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    livedTogetherRisk,
                                    answerSelectedListener,
                                    "livedTogetherRisk",
                                    previousAnswer
                            )
                    )
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    durationRisk,
                                    answerSelectedListener,
                                    "durationRisk",
                                    previousAnswer
                            )
                    )

                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    distanceRisk,
                                    answerSelectedListener,
                                    "distanceRisk",
                                    previousAnswer
                            )
                    )

                    natureOfContactQuestions["livedTogetherRisk"] = livedTogetherRisk
                    natureOfContactQuestions["durationRisk"] = durationRisk
                    natureOfContactQuestions["distanceRisk"] = distanceRisk

                }
                else -> {
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    livedTogetherRisk,
                                    answerSelectedListener,
                                    "livedTogetherRisk",
                                    previousAnswer
                            )
                    )
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    durationRisk,
                                    answerSelectedListener,
                                    "durationRisk",
                                    previousAnswer
                            )
                    )

                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    distanceRisk,
                                    answerSelectedListener,
                                    "distanceRisk",
                                    previousAnswer
                            )
                    )
                    sectionToAddTo?.add(
                            QuestionTwoOptionsItem(
                                    otherRisk,
                                    answerSelectedListener,
                                    "otherRisk",
                                    previousAnswer
                            )
                    )

                    natureOfContactQuestions["livedTogetherRisk"] = livedTogetherRisk
                    natureOfContactQuestions["durationRisk"] = durationRisk
                    natureOfContactQuestions["distanceRisk"] = distanceRisk
                    natureOfContactQuestions["otherRisk"] = otherRisk
                }
            }

        } else {
            sectionToAddTo?.add(
                    QuestionTwoOptionsItem(
                            livedTogetherRisk,
                            answerSelectedListener,
                            "livedTogetherRisk",
                            previousAnswer
                    )
            )
            sectionToAddTo?.add(
                    QuestionTwoOptionsItem(
                            durationRisk,
                            answerSelectedListener,
                            "durationRisk",
                            previousAnswer
                    )
            )

            sectionToAddTo?.add(
                    QuestionTwoOptionsItem(
                            distanceRisk,
                            answerSelectedListener,
                            "distanceRisk",
                            previousAnswer
                    )
            )
            sectionToAddTo?.add(
                    QuestionTwoOptionsItem(
                            otherRisk,
                            answerSelectedListener,
                            "otherRisk",
                            previousAnswer
                    )
            )

            natureOfContactQuestions["livedTogetherRisk"] = livedTogetherRisk
            natureOfContactQuestions["durationRisk"] = durationRisk
            natureOfContactQuestions["distanceRisk"] = distanceRisk
            natureOfContactQuestions["otherRisk"] = otherRisk
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


        selectedTask.let {
            it.linkedContact = selectedContact
            it.questionnaireResult = QuestionnaireResult(viewModel.questionnaire?.uuid!!, JsonArray(finalAnswers))
            it.label = selectedContact.displayName
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