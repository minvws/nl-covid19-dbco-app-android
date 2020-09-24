/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.picker

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactItemDecoration
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.Contact
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.SearchFieldItem
import timber.log.Timber


class ContactPickerSelectionFragment : BaseFragment(R.layout.fragment_list),
    SearchView.OnQueryTextListener {
    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val contactsViewModel by viewModels<ContactsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListBinding.bind(view)
        binding.toolbar.setTitle("Contact list")
        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            ContactItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        contactsViewModel.contactsLiveData.observe(viewLifecycleOwner, Observer {
            Toast.makeText(
                requireContext(),
                "Retrieved ${it.size ?: 0} contacts in total",
                Toast.LENGTH_SHORT
            ).show()

            adapter.add(SearchFieldItem({ }))

            adapter.addAll(it)
            it.forEach {
                Timber.d("Found contact $it")

            }
        })

        contactsViewModel.fetchContacts()

        adapter.setOnItemClickListener { item, _ ->
            when(item) {
                is Contact -> {
                    Timber.d("Clicked contact $item")
                }
            }

        }


    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        Timber.d("Submitted : $query")
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        Timber.d("New input: $newText")
        return true
    }
}