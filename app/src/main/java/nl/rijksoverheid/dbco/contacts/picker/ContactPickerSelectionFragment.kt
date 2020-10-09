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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactItemDecoration
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.LocalContact
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.input.SearchFieldItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import timber.log.Timber


class ContactPickerSelectionFragment : BaseFragment(R.layout.fragment_list) {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val contactsViewModel by viewModels<ContactsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListBinding.bind(view)
        binding.toolbar.title = getString(R.string.contacts_picker_title)
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            ContactItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        contactsViewModel.localContactsLiveData.observe(viewLifecycleOwner, Observer {
            adapter.clear()
            adapter.add(SearchFieldItem({ content ->
                Timber.d("Searching for ${content.toString()}")
                contactsViewModel.filterLocalContactsOnName(content.toString())
            }, R.string.contact_picker_search_hint))

            val contactsSection = Section(
                HeaderItem(R.string.header_all_contacts),
                it
            )

            adapter.add(contactsSection)
            adapter.setOnItemClickListener { item, _ ->
                when (item) {
                    is LocalContact -> {
                        Timber.d("Clicked contact $item")
                        findNavController().navigate(
                            ContactPickerSelectionFragmentDirections.toContactDetails(
                                item
                            )
                        )
                    }
                }
            }

        })

        contactsViewModel.fetchLocalContacts()


    }

}