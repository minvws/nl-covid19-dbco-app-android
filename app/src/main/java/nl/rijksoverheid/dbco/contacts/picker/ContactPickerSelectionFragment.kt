/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.picker

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactItemDecoration
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.LocalContactItem
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentContactSelectionBinding
import nl.rijksoverheid.dbco.items.input.SearchFieldItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.TinyHeaderItem
import java.util.ArrayList

class ContactPickerSelectionFragment : BaseFragment(R.layout.fragment_contact_selection) {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val args: ContactPickerSelectionFragmentArgs by navArgs()

    private val contactsViewModel: ContactsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactSelectionBinding.bind(view)
        initToolbar(binding)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(ContactItemDecoration(requireContext()))
        binding.manualEntryButton.setOnClickListener { onManualEntryClicked() }

        contactsViewModel.localContactsLiveDataItem.observe(viewLifecycleOwner, { allContacts ->
            onContacts(allContacts)
        })
        contactsViewModel.fetchLocalContacts()
    }

    private fun onContacts(allContacts: ArrayList<LocalContact>) {
        adapter.clear()
        adapter.add(
            HeaderItem(
                getString(R.string.contacts_picker_title),
                R.dimen.activity_horizontal_margin
            )
        )
        adapter.add(
            SearchFieldItem({ content ->
                contactsViewModel.filterLocalContactsOnName(content.toString())
            }, R.string.contact_picker_search_hint)
        )

        // If the user has selected a task, see if we can suggest contacts that match the label of this task
        val label = contactsViewModel.getTaskLabel(args.indexTaskUuid)
        if (!label.isNullOrEmpty()) {
            val suggestedContacts = contactsViewModel.filterSuggestedContacts(label)
            if (suggestedContacts.isNotEmpty()) {
                val suggestedContactsSection = Section(
                    TinyHeaderItem(R.string.header_suggested_contacts)
                )
                suggestedContacts.forEach {
                    suggestedContactsSection.add(LocalContactItem(it))
                }
                adapter.add(suggestedContactsSection)
            }
        }

        val contactsSection = Section(TinyHeaderItem(R.string.header_all_contacts))

        allContacts.forEach { contactsSection.add(LocalContactItem(it)) }

        adapter.add(contactsSection)
        adapter.setOnItemClickListener { item, _ -> onContactClicked(item) }
    }

    private fun onContactClicked(item: Item<*>) {
        when (item) {
            is LocalContactItem -> {
                contactsViewModel.onContactPicked(args.indexTaskUuid, item.contact)
                findNavController().navigate(
                    ContactPickerSelectionFragmentDirections.toContactDetails(
                        indexTaskUuid = args.indexTaskUuid,
                        enabled = true,
                        newTask = true
                    )
                )
            }
        }
    }

    private fun onManualEntryClicked() {
        findNavController().navigate(
            ContactPickerSelectionFragmentDirections.toContactDetails(
                indexTaskUuid = args.indexTaskUuid,
                enabled = true,
                newTask = true
            )
        )
    }

    private fun initToolbar(binding: FragmentContactSelectionBinding) {
        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<View>(R.id.searchView)?.clearFocus() // make search input inactive
    }
}