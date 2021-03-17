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

class RoommateInputFragment : BaseFragment(R.layout.fragment_selfbco_roommates_input) {

    private val contactsViewModel by viewModels<ContactsViewModel>()

    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var binding: FragmentSelfbcoRoommatesInputBinding

    private var contactNames = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoRoommatesInputBinding.bind(view)

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

        // add existing roommates to list
        for (roommate in selfBcoViewModel.getRoommates()) {
            addContactToSection(
                section = section,
                contactUuid = roommate.uuid!!,
                contactName = roommate.label!!
            )
        }

        adapter.setOnItemClickListener { item, _ ->
            if (item is ContactAddItem) {
                addContactToSection(
                    section = section,
                    withFocus = true
                )
            }
            updateNextButton(section)
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
            viewLifecycleOwner, {
                contactNames = contactsViewModel.getLocalContactNames()
            }
        )

        updateNextButton(section)

        binding.btnNext.setOnClickListener {
            grabInput()
            findNavController().navigate(RoommateInputFragmentDirections.toTimelineExplanationFragment())
        }

        binding.backButton.setOnClickListener {
            findNavController().navigate(RoommateInputFragmentDirections.toSelfBcoDoubleCheckFragment())
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(RoommateInputFragmentDirections.toSelfBcoDoubleCheckFragment())
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    private fun updateNextButton(section: Section) {
        binding.btnNext.text = if (section.groupCount > 2) {
            getString(R.string.next)
        } else {
            getString(R.string.selfbco_roommates_alone_button_text)
        }
    }

    private fun addContactToSection(
        section: Section,
        contactName: String = "",
        contactUuid: String? = null,
        withFocus: Boolean = false
    ) {
        val thrashListener = object : ContactInputItem.OnTrashClickedListener {
            override fun onTrashClicked(item: ContactInputItem) {
                section.remove(item)
                updateNextButton(section)
                item.contactUuid?.let { uuid ->
                    // when a contact already has an uuid it means that it was already added
                    // to the case before so it needs to be removed
                    selfBcoViewModel.removeContact(uuid)
                }
            }
        }
        section.add(
            ContactInputItem(
                focusOnBind = withFocus,
                contactNames = contactNames.toTypedArray(),
                contactName = contactName,
                contactUuid = contactUuid,
                trashListener = thrashListener
            )
        )
    }

    private fun grabInput() {
        for (groupIndex: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(groupIndex)
            if (item is ContactInputItem) {
                if (item.contactName.isNotEmpty()) {
                    selfBcoViewModel.addContact(item.contactName, category = Category.ONE)
                }
            }
        }
    }
}