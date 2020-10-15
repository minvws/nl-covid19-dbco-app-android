/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.mycontacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsBinding

/**
 * Overview fragment showing selected or suggested contacts of the user
 */

class MyContactsFragment : BaseFragment(R.layout.fragment_my_contacts) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private val contentSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentSection.setHideWhenEmpty(true)
        adapter.add(contentSection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyContactsBinding.bind(view)
        binding.content.adapter = adapter

        binding.toolbar.visibility = View.GONE

        binding.manualEntryButton.setOnClickListener {
            checkPermissionAndNavigate()
        }

        // After retrieving data, clear list before showing new
        contactsViewModel.indexContactsLiveData.observe(viewLifecycleOwner, Observer {
            contentSection.clear()
            contentSection.addAll(it)
        })

        // Fake loading from backend
        lifecycleScope.launch {
            delay(250)
            contactsViewModel.fetchBackendIndexContacts()
        }


    }

    private fun checkPermissionAndNavigate() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Will navigate to picker
            findNavController().navigate(MyContactsFragmentDirections.toContactPickerAbout())
        } else {
            findNavController().navigate(MyContactsFragmentDirections.toContactPickerSelection())
        }
    }

}