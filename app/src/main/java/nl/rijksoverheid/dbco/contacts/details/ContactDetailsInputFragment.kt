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
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.entity.*
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSection
import nl.rijksoverheid.dbco.items.ui.QuestionnaireSectionHeader
import nl.rijksoverheid.dbco.items.ui.SubHeaderItem
import nl.rijksoverheid.dbco.util.toDp
import timber.log.Timber

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_contact_input) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()
    private val answerSelectedListener: (AnswerOption) -> Unit = {
        // TODO handle
    }

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

        Timber.d("Found selected user ${args.selectedContact}");


        val response: ContactDetailsResponse =
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString(MOCKED_OUTPUT) // TODO move to ViewModel


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
        response.questionnaires?.forEach {
            it?.questions?.forEach { question ->
                val sectionToAddTo =
                    when (question?.group) {
                        Group.ContactDetails -> contactDetailsSection
                        Group.Classification -> classificationSection
                        else -> null
                    }

                when (question?.questionType) {
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
                        addContactDetailsItems(contactItem, sectionToAddTo)
                    }
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
        sectionToAddTo: QuestionnaireSection?
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
                ContactNameItem(firstName, lastName),
                PhoneNumberItem(primaryPhone),
                EmailAdressItem(primaryEmail)
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

        Toast.makeText(context, "Nog niet actief", Toast.LENGTH_SHORT).show()

        val answers = HashMap<String, Any>()

        for (i: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(i)
            (item as? BaseQuestionItem<*>)?.let {
                answers.putAll(it.getUserAnswers())
            }
        }
    }

    companion object {
        const val MOCKED_OUTPUT = "{\n" +
                "    \"questionnaires\": [\n" +
                "        {\n" +
                "            \"uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "            \"taskType\": \"contact\",\n" +
                "            \"questions\": [\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368387\",\n" +
                "                    \"group\": \"classification\",\n" +
                "                    \"questionType\": \"classificationdetails\",\n" +
                "                    \"label\": \"Vragen over jullie ontmoeting\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    },{\n" +
                "                        \"category\": \"2a\"\n" +
                "                    },{\n" +
                "                        \"category\": \"2b\"\n" +
                "                    },{\n" +
                "                        \"category\": \"3\"\n" +
                "                    }]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9772-725467368387\",\n" +
                "                    \"group\": \"classification\",\n" +
                "                    \"questionType\": \"date\",\n" +
                "                    \"label\": \"Wanneer was de laatste ontmoeting?\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    },{\n" +
                "                        \"category\": \"2a\"\n" +
                "                    },{\n" +
                "                        \"category\": \"2b\"\n" +
                "                    },{\n" +
                "                        \"category\": \"3\"\n" +
                "                    }]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9770-725467368388\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"contactdetails\",\n" +
                "                    \"label\": \"Contactgegevens\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    },{\n" +
                "                        \"category\": \"2a\"\n" +
                "                    },{\n" +
                "                        \"category\": \"2b\"\n" +
                "                    },{\n" +
                "                        \"category\": \"3\"\n" +
                "                    }]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368388\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"date\",\n" +
                "                    \"label\": \"Geboortedatum\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    }]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368389\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"open\",\n" +
                "                    \"label\": \"Beroep\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    }]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368391\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"multiplechoice\",\n" +
                "                    \"label\": \"Waar ken je deze persoon van?\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"2a\"\n" +
                "                    },{\n" +
                "                        \"category\": \"2b\"\n" +
                "                    }],\n" +
                "                    \"answerOptions\": [\n" +
                "                        {\n" +
                "                            \"label\": \"Ouder\",\n" +
                "                            \"value\": \"Ouder\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Kind\",\n" +
                "                            \"value\": \"Kind\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Broer of zus\",\n" +
                "                            \"value\": \"Broer of zus\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Partner\",\n" +
                "                            \"value\": \"Partner\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Familielid (overig)\",\n" +
                "                            \"value\": \"Familielid (overig)\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Huisgenoot\",\n" +
                "                            \"value\": \"Huisgenoot\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Vriend of kennis\",\n" +
                "                            \"value\": \"Vriend of kennis\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Medestudent of leerling\",\n" +
                "                            \"value\": \"Medestudent of leerling\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Collega\",\n" +
                "                            \"value\": \"Collega\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Gezondheidszorg medewerker\",\n" +
                "                            \"value\": \"Gezondheidszorg medewerker\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Ex-partner\",\n" +
                "                            \"value\": \"Ex-partner\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Overig\",\n" +
                "                            \"value\": \"Overig\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368392\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"multiplechoice\",\n" +
                "                    \"label\": \"Is een of meerdere onderstaande zaken van toepassing voor deze persoon?\",\n" +
                "                    \"description\": \"* Is student\\n* 70 jaar of ouder\\n* Heeft gezondheidsklachten of loopt extra gezondheidsrisico's\\n* Woont in een asielzoekerscentrum\\n* Spreekt slecht of geen Nederlands\",\n" +
                "                    \"relevantForCategories\": [{\n" +
                "                        \"category\": \"1\" \n" +
                "                    },{\n" +
                "                        \"category\": \"2a\"\n" +
                "                    },{\n" +
                "                        \"category\": \"2b\"\n" +
                "                    }],\n" +
                "                    \"answerOptions\": [\n" +
                "                        {\n" +
                "                            \"label\": \"Ja, één of meerdere dingen\",\n" +
                "                            \"value\": \"Ja\",\n" +
                "                            \"trigger\": \"communication_staff\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Nee, ik denk het niet\",\n" +
                "                            \"value\": \"Nee\",\n" +
                "                            \"trigger\": \"communication_index\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    }

}