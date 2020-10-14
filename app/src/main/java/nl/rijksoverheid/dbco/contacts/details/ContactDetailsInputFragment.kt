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
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentContactInputBinding
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem
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

        args.selectedContact.also { contact ->
            binding.toolbar.title = contact.displayName
            setupContactTypeSection()
            setupContactDetailsSection(contact)
            setupContactInformSection()
        }
    }

    private fun setupContactTypeSection() {
        adapter.add(ExpandableGroup(
            QuestionnaireSectionHeader(
                R.string.contact_section_typeofcontact_header,
                R.string.contact_section_typeofcontact_subtext,
                1
            ), false
        ).apply {
            add(
                Section(
                    listOf(
                    )
                )
            )
        }
        )
    }


    private fun setupContactDetailsSection(contact: LocalContact) {
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
        adapter.add(ExpandableGroup(
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

}