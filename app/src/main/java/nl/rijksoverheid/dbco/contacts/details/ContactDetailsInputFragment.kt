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
import nl.rijksoverheid.dbco.contacts.data.LocalContact
import nl.rijksoverheid.dbco.contacts.data.entity.AnswerOption
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.contacts.data.entity.QuestionType
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem
import nl.rijksoverheid.dbco.items.QuestionnaireSectionDecorator
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.items.ui.*
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
            Json.decodeFromString(MOCKED_OUTPUT) // TODO move to ViewModel


        args.selectedContact.also { contact ->
            binding.toolbar.title = contact.displayName
            setupContactTypeSection(response)
            setupContactDetailsSection(contact, response)
            setupContactInformSection()
        }
    }

    private fun setupContactTypeSection(response: ContactDetailsResponse) {
        adapter.add(
            QuestionnaireSection(
                this,
                QuestionnaireSectionHeader(
                    R.string.contact_section_typeofcontact_header,
                    R.string.contact_section_typeofcontact_subtext,
                    1
                ), false
            ).apply {
                response.questionnaires?.forEach {
                    it?.questions?.forEach { question ->
                        when (question?.questionType) {
                            QuestionType.Multiplechoice -> {
                                question.answerOptions?.size?.let { size ->
                                    if (size > 2) {
                                        add(
                                            QuestionMultipleOptionsItem(
                                                requireContext(),
                                                question,
                                                answerSelectedListener
                                            )
                                        )
                                    } else if (size == 2) {
                                        add(
                                            QuestionTwoOptionsItem(
                                                question,
                                                answerSelectedListener
                                            )
                                        )
                                    }
                                }
                            }
                            // TODO handle other types
                        }
                    }
                }

            }
        )
    }


    private fun setupContactDetailsSection(
        contact: LocalContact,
        response: ContactDetailsResponse
    ) {
        val nameParts = contact.displayName.split(" ", limit = 2)
        val firstName = nameParts[0] ?: ""
        val lastName = if (nameParts.size > 1) {
            nameParts[1]
        } else {
            ""
        }

        val primaryPhone = if (contact.numbers.isNotEmpty()) {
            contact.numbers[0]
        } else {
            ""
        }

        val primaryEmail = if (contact.emails.isNotEmpty()) {
            contact.emails[0]
        } else {
            ""
        }

        // Default items to always add
        adapter.add(
            QuestionnaireSection(
                this,
                QuestionnaireSectionHeader(
                    R.string.contact_section_contactdetails_header,
                    R.string.contact_section_contactdetails_subtext,
                    2
                ), false
            ).apply {
                addAll(
                    listOf(
                        ContactNameItem(firstName, lastName),
                        PhoneNumberItem(primaryPhone),
                        EmailAdressItem(primaryEmail)
                    )

                )
            }
        )
    }

    private fun setupContactInformSection() {
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


    private fun parseInput() {

        Toast.makeText(context, "Nog niet actief", Toast.LENGTH_SHORT).show()

        var i = 0
        while (i < adapter.itemCount) {
            val item = adapter.getItem(i)
            if (item is QuestionnaireItem) {
                when (item.getItemType()) {
                    ItemType.INPUT_NAME -> {
                        Timber.d("Found name field with content ${(item as ContactNameItem).getFirstNameAndLastName()}")
                    }

                    else -> {
                        // Todo: Handle rest of input fields
                    }
                }
                i++
            }
        }
    }

    companion object {
        const val MOCKED_OUTPUT = "{\n" +
                "  \"questionnaires\": [\n" +
                "    {\n" +
                "      \"id\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "      \"taskType\": \"contact\",\n" +
                "      \"questions\": [\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368387\",\n" +
                "              \"group\": \"context\",\n" +
                "              \"questionType\": \"classificationdetails\",\n" +
                "              \"label\": \"Vragen over jullie ontmoeting\",\n" +
                "              \"description\": null,\n" +
                "              \"relevantForCategories\": [ \"1\", \"2a\", \"2b\", \"3\" ]\n" +
                "\t\t  },\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368388\",\n" +
                "              \"group\": \"contactdetails\",\n" +
                "              \"questionType\": \"date\",\n" +
                "              \"label\": \"Geboortedatum\",\n" +
                "              \"description\": null,\n" +
                "              \"relevantForCategories\": [ \"1\" ]\n" +
                "          },\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368389\",\n" +
                "              \"group\": \"contactdetails\",\n" +
                "              \"questionType\": \"open\",\n" +
                "              \"label\": \"Beroep\",\n" +
                "              \"description\": null,\n" +
                "              \"relevantForCategories\": [ \"1\" ]\n" +
                "          },\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368390\",\n" +
                "              \"group\": \"contactdetails\",\n" +
                "              \"questionType\": \"open\",\n" +
                "              \"label\": \"Beroep\",\n" +
                "              \"description\": null,\n" +
                "              \"relevantForCategories\": [ \"1\" ]\n" +
                "          },\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368391\",\n" +
                "              \"group\": \"contactdetails\",\n" +
                "              \"questionType\": \"multiplechoice\",\n" +
                "              \"label\": \"Waar ken je deze persoon van?\",\n" +
                "              \"description\": null,\n" +
                "              \"relevantForCategories\": [ \"2a\", \"2b\" ],\n" +
                "              \"answerOptions\": [\n" +
                "                  {\n" +
                "                      \"label\": \"Vriend of kennis\",\n" +
                "                      \"value\": \"Vriend of kennis\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                      \"label\": \"Collega\",\n" +
                "                      \"value\": \"Collega\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                      \"label\": \"Overig\",\n" +
                "                      \"value\": \"Overig\"\n" +
                "                  }\n" +
                "              ]\n" +
                "              \n" +
                "          },\n" +
                "          {\n" +
                "              \"id\": \"37d818ed-9499-4b9a-9771-725467368392\",\n" +
                "              \"group\": \"contactdetails\",\n" +
                "              \"questionType\": \"multiplechoice\",\n" +
                "              \"label\": \"Is een of meerdere onderstaande zaken van toepassing voor deze persoon?\",\n" +
                "              \"description\": \"<ul><li>Is student<li>70 jaar of ouder<li>Heeft gezondheidsklachten of loopt extra gezondheidsrisico's<li>Woont in een asielzoekerscentrum<li>Spreekt slecht of geen Nederlands</ul>\",\n" +
                "              \"relevantForCategories\": [ \"1\", \"2a\", \"2b\" ],\n" +
                "              \"answerOptions\": [\n" +
                "                  {\n" +
                "                      \"label\": \"Ja, één of meerdere dingen\",\n" +
                "                      \"value\": \"Ja\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                      \"label\": \"Nee, ik denk het niet\",\n" +
                "                      \"value\": \"Nee\"\n" +
                "                  }\n" +
                "              ]\n" +
                "          }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"
    }

}