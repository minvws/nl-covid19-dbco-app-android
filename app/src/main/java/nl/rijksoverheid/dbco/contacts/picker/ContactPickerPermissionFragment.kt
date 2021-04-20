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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentPermissionBinding
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Task

class ContactPickerPermissionFragment : BaseFragment(R.layout.fragment_permission) {

    private val args: ContactPickerPermissionFragmentArgs by navArgs()
    private val userPrefs by lazy {
        LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
    }
    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(
                    ContactPickerPermissionFragmentDirections.toContactPicker(indexTask = args.indexTask)
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
                ContactPickerPermissionFragmentDirections.toContactPicker(indexTask = args.indexTask)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPermissionBinding.bind(view)

        val nameToShow = args.indexTask?.label ?: getString(R.string.this_contact)

        binding.onboardingHeader.text =
            String.format(getString(R.string.permission_name_header), nameToShow)
        binding.btnNext.setOnClickListener {
            requestContactAccess()
        }

        binding.btnManual.setOnClickListener {
            userPrefs.edit()?.putBoolean(
                Constants.USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY,
                true
            )?.apply()
            findNavController().navigate(
                ContactPickerPermissionFragmentDirections.toContactDetails(
                    indexTask = args.indexTask ?: Task.createAppContact()
                )
            )
        }
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun requestContactAccess() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                requestCallback.launch(Manifest.permission.READ_CONTACTS)
            } else {
                activity?.let {
                    val builder = MaterialAlertDialogBuilder(it)
                    builder.setTitle(R.string.permissions_title)
                    builder.setCancelable(false)
                    builder.setMessage(R.string.permissions_some_permissions_denied)
                    builder.setPositiveButton(
                        R.string.permissions_go_to_settings
                    ) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        // Go to app settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", it.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.startActivity(intent)
                        it.finish()
                    }
                    builder.setNegativeButton(R.string.permissions_no) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    val alert: AlertDialog = builder.create()
                    alert.show()
                }
            }
        } else {
            ContactPickerPermissionFragmentDirections.toContactPicker(indexTask = args.indexTask)
        }
    }
}