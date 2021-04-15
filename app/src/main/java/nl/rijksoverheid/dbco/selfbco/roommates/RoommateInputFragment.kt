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
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import java.io.Serializable

class RoommateInputFragment : BaseFragment(R.layout.fragment_selfbco_roommates_input) {

    private val contactsViewModel: ContactsViewModel by viewModels()

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var binding: FragmentSelfbcoRoommatesInputBinding

    private var contactNames = ArrayList<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoRoommatesInputBinding.bind(view)

        val content = Section()

        val state: State = State.fromBundle(savedInstanceState) ?: State(
            selfBcoViewModel.getRoommates().map { State.Roommate(it.label!!, it.uuid) }
        )

        initToolbar()
        initContent(content)

        for (roommate in state.roommates) {
            addContactToSection(
                section = content,
                contactName = roommate.name,
                contactUuid = roommate.uuid
            )
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

        updateNextButton(content)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = getState()
        if (state.roommates.isNotEmpty()) {
            state.addToBundle(outState)
        }
        super.onSaveInstanceState(outState)
    }

    private fun initToolbar() {
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initContent(content: Section) {
        content.setHeader(
            Section(
                listOf(
                    HeaderItem(getString(R.string.selfbco_roommates_header)),
                    ParagraphItem(getString(R.string.selfbco_roommates_summary))
                )
            )
        )
        content.setFooter(ContactAddItem())
        adapter.clear()
        adapter.add(content)
        binding.content.adapter = adapter

        adapter.setOnItemClickListener { item, _ ->
            if (item is ContactAddItem) {
                addContactToSection(
                    section = content,
                    withFocus = true
                )
            }
            updateNextButton(content)
        }

        binding.btnNext.setOnClickListener {
            saveInput()
            findNavController().navigate(
                RoommateInputFragmentDirections.toTimelineExplanationFragment()
            )
        }
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

    private fun saveInput() {
        getState().roommates.forEach { roommate ->
            selfBcoViewModel.addContact(roommate.name, category = Category.ONE)
        }
    }

    private fun getState(): State {
        val list = ArrayList<State.Roommate>()
        for (groupIndex: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(groupIndex)
            if (item is ContactInputItem) {
                list.add(State.Roommate(item.contactName, item.contactUuid))
            }
        }
        return State(list)
    }


    private data class State(
        val roommates: List<Roommate>
    ) : Serializable {

        fun addToBundle(bundle: Bundle) {
            bundle.putSerializable(STATE_KEY, this)
        }

        data class Roommate(
            val name: String,
            val uuid: String?
        ) : Serializable

        companion object {
            private const val STATE_KEY = "RoommateInputFragment_State"

            fun fromBundle(bundle: Bundle?): State? {
                return bundle?.getSerializable(STATE_KEY) as? State
            }
        }
    }
}