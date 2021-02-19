/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.roommates

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoRoommatesInputBinding
import nl.rijksoverheid.dbco.items.input.ContactInputItem
import nl.rijksoverheid.dbco.items.ui.ContactAddItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import timber.log.Timber

class RoommateInputFragment() :
    BaseFragment(R.layout.fragment_selfbco_roommates_input) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private var contactNames = ArrayList<String>()
    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoRoommatesInputBinding.bind(view)

        val section = Section()
        section.setHeader(
            Section(
                listOf(
                    HeaderItem(R.string.selfbco_roommates_header),
                    ParagraphItem(getString(R.string.selfbco_roommates_summary))
                )
            )
        )
        section.setFooter(ContactAddItem())
        adapter.clear()
        adapter.add(section)
        binding.content.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            if (item is ContactAddItem) {
                section.add(ContactInputItem(contactNames.toTypedArray(), trashListener = object :
                    ContactInputItem.OnTrashClickedListener {
                    // Remove item from section if trashcan is clicked
                    override fun onTrashClicked(item: ContactInputItem) {
                        section.remove(item)
                    }

                }))
            }
            if (item is ContactInputItem) {
                when (view.id) {
                    R.id.icon_trash -> {
                        section.remove(item)
                    }
                }
            }
        }
        // Only check for contacts if we have the permission, otherwise we'll use the empty list instead
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactsViewModel.fetchLocalContacts()
        }

        contactsViewModel.localContactsLiveDataItem.observe(
            viewLifecycleOwner,
            {
                contactNames = contactsViewModel.getLocalContactNames()
                Timber.d("Found names ${contactNames}")
            })


        binding.btnNext.setOnClickListener {
            grabInput()
            findNavController().navigate(RoommateInputFragmentDirections.toTimelineExplanationFragment())
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(RoommateInputFragmentDirections.toSelfBcoDoubleCheckFragment(selfBcoViewModel.getTypeOfFlow(), selfBcoViewModel.getDateOfSymptomOnset().millis))
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    findNavController().navigate(RoommateInputFragmentDirections.toSelfBcoDoubleCheckFragment(selfBcoViewModel.getTypeOfFlow(), selfBcoViewModel.getDateOfSymptomOnset().millis))
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun grabInput() {
        for (groupIndex: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(groupIndex)
            Timber.d("Found at $groupIndex an item of $item with input")
            if (item is ContactInputItem) {
                Timber.d("Content is ${item.contactName}")
                if (item.contactName.isNotEmpty()) {
                    selfBcoViewModel.addSelfBcoContact(
                        item.contactName,
                        category = Category.LIVED_TOGETHER
                    )
                }
            }
        }
    }


}