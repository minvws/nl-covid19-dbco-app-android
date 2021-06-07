/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.picker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.databinding.FragmentPermissionBinding
import nl.rijksoverheid.dbco.selfbco.onboarding.SelfBcoPermissionFragmentDirections
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

        binding.onboardingHeader.text =
            String.format(getString(R.string.permission_name_header), nameToShow)
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