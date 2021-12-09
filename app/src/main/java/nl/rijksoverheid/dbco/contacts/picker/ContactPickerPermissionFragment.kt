/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.picker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.databinding.FragmentPermissionBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.storage.LocalStorageRepository

class ContactPickerPermissionFragment : BaseFragment(R.layout.fragment_permission) {

    private val contactsViewModel: ContactsViewModel by viewModels()

    private val args: ContactPickerPermissionFragmentArgs by navArgs()

    private val userPrefs by lazy {
        LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
    }

    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(
                    ContactPickerPermissionFragmentDirections.toContactPicker(
                        indexTaskUuid = args.indexTaskUuid
                    )
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if we somehow already gave contacts permission
        // Continue to contact input if we do, or stay here if we don't
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            findNavController().navigate(
                ContactPickerPermissionFragmentDirections.toContactPicker(
                    indexTaskUuid = args.indexTaskUuid
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPermissionBinding.bind(view)

        val label = contactsViewModel.getTaskLabel(args.indexTaskUuid)
        val nameToShow = label ?: getString(R.string.this_contact)
        val content = Section(
            listOf(
                HeaderItem(String.format(getString(R.string.permission_name_header), nameToShow)),
                ParagraphItem(getString(R.string.permission_summary), clickable = false),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item4)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item1)),
                ParagraphIconItem(getString(R.string.selfbco_permission_extra_item2)),
            )
        )
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(content)

        binding.content.adapter = adapter

        binding.btnNext.setOnClickListener {
            requestPermission(requestCallback, Manifest.permission.READ_CONTACTS) {
                findNavController().navigate(
                    ContactPickerPermissionFragmentDirections.toContactPicker(
                        indexTaskUuid = args.indexTaskUuid
                    )
                )
            }
        }

        binding.btnManual.setOnClickListener {
            userPrefs.edit()?.putBoolean(
                Constants.USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY,
                true
            )?.apply()
            findNavController().navigate(
                ContactPickerPermissionFragmentDirections.toContactDetails(
                    indexTaskUuid = args.indexTaskUuid,
                    newTask = true
                )
            )
        }
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}