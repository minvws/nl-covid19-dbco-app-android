/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.roommates

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoRoommatesInputBinding
import nl.rijksoverheid.dbco.items.input.ContactInputItem
import nl.rijksoverheid.dbco.items.ui.ContactAddItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import timber.log.Timber

class RoommateInputFragment(val contactName : String = "") : BaseFragment(R.layout.fragment_selfbco_roommates_input) {

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
        section.setFooter(ContactAddItem())
        adapter.add(section)
        binding.content.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            if(item is ContactAddItem){
                section.add(ContactInputItem(contactNames.toTypedArray(), trashListener = object :
                    ContactInputItem.OnTrashClickedListener {
                    override fun onTrashClicked(item: ContactInputItem) {
                        Toast.makeText(context, "Clicked trash for item with text ${item.contactName}", Toast.LENGTH_SHORT).show()
                        section.remove(item)
                    }

                }))
            }
            if(item is ContactInputItem){
                when(view.id){
                    R.id.icon_trash -> {
                        section.remove(item)
                    }
                }
            }
        }

        contactsViewModel.fetchLocalContacts()

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
            findNavController().popBackStack()
        }
        
    }

    private fun grabInput(){
        for (groupIndex: Int in 0 until adapter.itemCount) {
            val item = adapter.getItem(groupIndex)
            Timber.d("Found at $groupIndex an item of $item with input")
            if(item is ContactInputItem){
                Timber.d("Content is ${item.contactName}")
                if(item.contactName.isNotEmpty()) {
                    selfBcoViewModel.addRoommate(item.contactName)
                }
            }
        }
    }


}