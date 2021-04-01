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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactItemDecoration
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.LocalContactItem
import nl.rijksoverheid.dbco.databinding.FragmentContactSelectionBinding
import nl.rijksoverheid.dbco.items.input.SearchFieldItem
import nl.rijksoverheid.dbco.items.ui.TinyHeaderItem
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import timber.log.Timber

class ContactPickerSelectionFragment : BaseFragment(R.layout.fragment_contact_selection) {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactPickerSelectionFragmentArgs by navArgs()
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private var selectedTask: Task? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentContactSelectionBinding.bind(view)
        binding.toolbar.title = getString(R.string.contacts_picker_title)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            ContactItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        args.indexTask?.let {
            selectedTask = it
        }

        binding.manualEntryButton.setOnClickListener {
            // User chooses not to select an existing contact from their device, start fresh
            findNavController().navigate(
                ContactPickerSelectionFragmentDirections.toContactDetails(
                    indexTask = selectedTask ?: Task.createAppContact(),
                    enabled = true
                )
            )
        }


        contactsViewModel.localContactsLiveDataItem.observe(
            viewLifecycleOwner,
            Observer { allContacts ->
                adapter.clear()
                adapter.add(SearchFieldItem({ content ->
                    Timber.d("Searching for ${content.toString()}")
                    contactsViewModel.filterLocalContactsOnName(content.toString())
                }, R.string.contact_picker_search_hint))

                // If the user has selected a task, see if we can suggest contacts that match the label of this task
                if (selectedTask != null) {
                    val suggestedContacts =
                        contactsViewModel.filterSuggestedContacts(selectedTask?.label ?: "")
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


                val contactsSection = Section(
                    TinyHeaderItem(R.string.header_all_contacts)
                )

                allContacts.forEach {
                    contactsSection.add(LocalContactItem(it))
                }

                adapter.add(contactsSection)
                adapter.setOnItemClickListener { item, _ ->
                    when (item) {
                        is LocalContactItem -> {
                            Timber.d("Clicked contact $item")
                            val task = selectedTask ?: Task.createAppContact()
                            task.linkedContact = item.contact
                            findNavController().navigate(
                                ContactPickerSelectionFragmentDirections.toContactDetails(
                                    indexTask = task,
                                    enabled = true
                                )
                            )
                        }
                    }
                }

            })

        contactsViewModel.fetchLocalContacts()
    }

    override fun onPause() {
        super.onPause()
        view?.findViewById<View>(R.id.searchView)?.clearFocus() // make search input inactive
    }

}