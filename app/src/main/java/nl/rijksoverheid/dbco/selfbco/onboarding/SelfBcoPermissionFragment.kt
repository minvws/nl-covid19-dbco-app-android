/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.onboarding

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPermissionBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

class SelfBcoPermissionFragment : BaseFragment(R.layout.fragment_selfbco_permission) {

    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(
                    SelfBcoPermissionFragmentDirections.toRoommateInputFragment()
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if we somehow already gave contacts permission
        // Continue to roommate input if we do, or stay here if we don't
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            findNavController().navigate(
                SelfBcoPermissionFragmentDirections.toRoommateInputFragment()
            )
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoPermissionBinding.bind(view)

        val content = Section(
            listOf(
                HeaderItem(getString(R.string.selfbco_permission_header)),
                ParagraphItem(getString(R.string.selfbco_permission_summary), clickable = true),
                ParagraphIconItem(getString(R.string.selfbco_permission_item1)),
                ParagraphIconItem(getString(R.string.selfbco_permission_item2)),
                ParagraphIconItem(getString(R.string.selfbco_permission_item3))
            )
        )
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(content)

        binding.content.adapter = adapter

        binding.btnNext.setOnClickListener {
            requestContactAccess()
        }

        binding.btnManual.setOnClickListener {
            findNavController().navigate(
                SelfBcoPermissionFragmentDirections.toRoommateInputFragment()
            )
        }
        binding.backButton.setOnClickListener {
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
            findNavController().navigate(
                SelfBcoPermissionFragmentDirections.toRoommateInputFragment()
            )
        }
    }

}