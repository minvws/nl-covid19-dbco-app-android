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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.questionnary.data.entity.*
import nl.rijksoverheid.dbco.tasks.data.TasksViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.toDp
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()
    private val tasksViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            TasksViewModel::class.java
        )
    }
    private val answerSelectedListener: (AnswerOption) -> Unit = {
        // TODO handle
    }

    private val shownQuestions: ArrayList<Question> = ArrayList()

    private lateinit var selectedTask: Task
    private lateinit var selectedContact: LocalContact
    private var questionnairy: Questionnairy? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactInputBinding.bind(view)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            VerticalSpaceItemDecoration(verticalSpaceHeight = 32.toDp())
        )
        binding.content.addItemDecoration(
            QuestionnaireSectionDecorator(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        selectedContact = args.selectedContact ?: LocalContact("-1", "Nieuw Contact")
        selectedTask = args.indexTask ?: Task(taskType = "contact", source = "app")


        tasksViewModel.questionnairy.observe(viewLifecycleOwner) { response ->
            questionnairy = response.questionnaires?.firstOrNull()
            args.selectedContact?.also { contact ->
                binding.toolbar.title = contact.displayName
                addQuestionnarySections(contact, response)
                addContactInformSection()
            }
            if (args.selectedContact == null) {
                binding.toolbar.title = resources.getString(R.string.mycontacts_add_contact)
                addQuestionnarySections(null, response)
                addContactInformSection()
            }
        }

        binding.saveButton.setOnClickListener {
            collectAnswers()
        }
    }

    private fun addQuestionnarySections(
        contactItem: LocalContact?,
        response: ContactDetailsResponse
    ) {
        val classificationSection = QuestionnaireSection(
            this,
            QuestionnaireSectionHeader(
                R.string.contact_section_typeofcontact_header,
                R.string.contact_section_typeofcontact_subtext,
                1
            ), false
        )
        adapter.add(classificationSection)

        val contactDetailsSection = QuestionnaireSection(
            this,
            QuestionnaireSectionHeader(
                R.string.contact_section_contactdetails_header,
                R.string.contact_section_contactdetails_subtext,
                2
            ), false
        )
        adapter.add(contactDetailsSection)

        // add questions to sections, based on their "group"
        response.questionnaires?.firstOrNull().let {
            it?.questions?.forEach { question ->
                val questionCategory = Category(args.indexTask?.category)
                if (!question?.relevantForCategories!!.contains(questionCategory)) {
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
                }

                shownQuestions.add(question)

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
                            answerSelectedListener
                        )
                    )
                }
                size == 2 -> {
                    sectionToAddTo?.add(
                        QuestionTwoOptionsItem(
                            question,
                            answerSelectedListener
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

        sectionToAddTo?.addAll(
            listOf(
                ContactNameItem(firstName, lastName, question),
                PhoneNumberItem(primaryPhone, question),
                EmailAdressItem(primaryEmail, question)
            )
        )
    }

    private fun addContactInformSection() {
        adapter.add(
            QuestionnaireSection(
                this,
                QuestionnaireSectionHeader(
                    R.string.contact_section_inform_header,
                    R.string.contact_section_inform_subtext,
                    3
                ), false
            ).apply {
                add(
                    Section(
                        listOf(
                            SubHeaderItem(R.string.contact_section_inform_content_header),
                            ParagraphItem(R.string.contact_section_inform_content_details),
                            ButtonItem(
                                R.string.contact_section_inform_share,
                                {},
                                type = ButtonType.LIGHT
                            )
                        )
                    )
                )
            }
        )
    }


    private fun collectAnswers() {


        val answerCollector = HashMap<String, Map<String, Any>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())

        for (i: Int in 0 until adapter.groupCount) {
            val item = adapter.getGroupAtAdapterPosition(i)
            (item as? QuestionnaireSection)?.let {
                for (i in 0 until (it.childCount + 1)) {
                    val child = it.getGroup(i)
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
            // Extract all actual answers, discard the keys since they're already in the answer
            val finalAnswers = ArrayList<JsonObject>()
            answerCollector.entries.forEach {
                val answerAsJson = JsonObject(it.value as Map<String, JsonElement>)
                finalAnswers.add(answerAsJson)
            }


            selectedTask.let {
                it.linkedContact = selectedContact
                it.questionnaireResult = QuestionnaireResult(questionnairy?.uuid!!, finalAnswers)
                tasksViewModel.saveChangesToTask(it)
            }

        }


//        shownQuestions.forEach { question ->
//            val answer = HashMap<String, Any>()
//            answer.put("questionUuid", question.uuid!!)
//            answer.put("lastModified", sdf.format(Date()))
//
////            for (i: Int in 0 until adapter.groupCount) {
////                val item = adapter.getGroupAtAdapterPosition(i)
////                (item as? QuestionnaireSection)?.let {
////                    for (i in 0 until (it.childCount + 1)) {
////                        val child = it.getGroup(i)
////                        if (child is BaseQuestionItem<*>) {
////                                answer.putAll(child.getUserAnswers())
////                            }
////                        }
////                    }
////
////            }
//
//
////            when (question.questionType) {
////                QuestionType.ContactDetails -> {
////                    for (i: Int in 0 until adapter.groupCount) {
////                        val item = adapter.getGroupAtAdapterPosition(i)
////                        (item as? QuestionnaireSection)?.let {
////                            for (i in 0 until (it.childCount + 1)) {
////                                val child = it.getGroup(i)
////                                if (child is BaseQuestionItem<*>) {
////                                    if (child is ContactNameItem || child is PhoneNumberItem || child is EmailAdressItem) {
////                                        answer.putAll(child.getUserAnswers())
////                                    }
////                                }
////                            }
////
////                        }
////                    }
////                }
////                else -> {
////                    for (i: Int in 0 until adapter.groupCount) {
////                        val item = adapter.getGroupAtAdapterPosition(i)
////                        (item as? QuestionnaireSection)?.let {
////                            for (i in 0 until (it.childCount + 1)) {
////                                val child = it.getGroup(i)
////                                if (child is BaseQuestionItem<*>) {
////                                    answer.putAll(child.getUserAnswers())
////                                }
////                            }
////
////                        }
////                    }
////                }
////            }
//
//
//        }
//
//

        Timber.d("Answers are $answerCollector")
        findNavController().navigate(ContactDetailsInputFragmentDirections.toMyContactsFragment())
    }


}