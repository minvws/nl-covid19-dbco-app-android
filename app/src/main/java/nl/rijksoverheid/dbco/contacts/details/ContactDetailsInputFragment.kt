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
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.VerticalSpaceItemDecoration
import nl.rijksoverheid.dbco.items.input.*
import nl.rijksoverheid.dbco.util.toPx
import timber.log.Timber

class ContactDetailsInputFragment : BaseFragment(R.layout.fragment_list) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactDetailsInputFragmentArgs by navArgs()
    private val answerSelectedListener: (AnswerOption) -> Unit = {
        // TODO handle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentListBinding.bind(view)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            VerticalSpaceItemDecoration(verticalSpaceHeight = 32.toPx())
        )

        Timber.d("Found selected user ${args.selectedContact}");

        val response: ContactDetailsResponse =
            Json.decodeFromString(MOCKED_OUTPUT) // TODO move to ViewModel

        setupQuestionnary(response)

        args.selectedContact.also { contact ->
            binding.toolbar.title = contact.displayName
            setupBasicFields(contact, response)
        }
    }

    private fun setupQuestionnary(response: ContactDetailsResponse) {
        val section = Section()
        response.questionnaires?.forEach {
            it?.questions?.forEach { question ->
                when (question?.questionType) {
                    QuestionType.Multiplechoice -> {
                        question.answerOptions?.size?.let { size ->
                            if (size > 2) {
                                section.add(QuestionMultipleOptionsItem(requireContext(), question, answerSelectedListener))
                            } else if (size == 2) {
                                section.add(QuestionTwoOptionsItem(question, answerSelectedListener))
                            }
                        }
                    }
                    QuestionType.Date -> {
                        section.add(DateInputItem(requireContext(), question))
                    }
                    // TODO handle other types
                }
            }
        }
        adapter.add(
            section
        )
    }

    private fun setupBasicFields(contact: LocalContact, response: ContactDetailsResponse) {
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
            Section(
                listOf(
                    ContactNameItem(firstName, lastName),
                    PhoneNumberItem(primaryPhone),
                    EmailAdressItem(primaryEmail),
                    ButtonItem(R.string.save, {
                        parseInput()
                    })
                )
            )
        )
    }


    private fun parseInput() {
        var i = 0
        while (i < adapter.itemCount) {
            val item = adapter.getItem(i)
            if (item is BaseBindableItem<*>) {
                when (item.itemType) {
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