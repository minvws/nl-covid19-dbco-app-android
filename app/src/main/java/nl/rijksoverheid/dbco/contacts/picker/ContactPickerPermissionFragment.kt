/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.picker

import android.Manifest
import android.content.Context
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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

class ContactPickerPermissionFragment : BaseFragment(R.layout.fragment_list) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val args: ContactPickerPermissionFragmentArgs by navArgs()
    private val userPrefs by lazy { activity?.getSharedPreferences(Constants.USER_PREFS, Context.MODE_PRIVATE) }
    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(
                    ContactPickerPermissionFragmentDirections.toContactPicker(
                        args.indexTask
                    )
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val content = Section(
            listOf(
                HeaderItem(R.string.contact_permission_header),
                ParagraphItem(getString(R.string.contact_permission_summary)),
                ButtonItem(
                    getString(R.string.mycontacts_grant_permission),
                    { requestContactAccess() }, type = ButtonType.DARK),
                ButtonItem(
                    getString(R.string.mycontacts_deny_permission),
                    {
                        findNavController().navigate(
                            ContactPickerPermissionFragmentDirections.toContactDetails(indexTask = args.indexTask)
                        )
                        userPrefs?.edit()?.putBoolean(Constants.USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY, true)?.apply()
                    }, type = ButtonType.LIGHT
                )
            )
        )
        adapter.add(content)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListBinding.bind(view)
        binding.content.adapter = adapter
        binding.toolbar.visibility = View.GONE
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
                    val builder: AlertDialog.Builder = AlertDialog.Builder(it)
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
                ContactPickerPermissionFragmentDirections.toContactPicker(
                    args.indexTask
                )
            )
        }
    }

}