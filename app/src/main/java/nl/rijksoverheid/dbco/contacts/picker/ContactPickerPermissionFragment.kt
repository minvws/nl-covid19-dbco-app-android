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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem

class ContactPickerPermissionFragment : BaseFragment(R.layout.fragment_list) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                findNavController().navigate(ContactPickerPermissionFragmentDirections.toContactPicker())
            } else {
                // Todo: Handle permanently denied permission. Behavior TBD
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = Section(
            listOf(
                HeaderItem(R.string.contact_permission_header),
                ParagraphItem(R.string.contact_permission_summary),
                ButtonItem(
                    R.string.mycontacts_grant_permission,
                    { requestContactAccess() }),
                ButtonItem(
                    R.string.mycontacts_deny_permission,
                    {
                        // Todo: Handle denying permission Behavior TBD
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
    }


    private fun requestContactAccess() {
        Toast.makeText(context, "Clicked button", Toast.LENGTH_SHORT).show()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCallback.launch(Manifest.permission.READ_CONTACTS)
        } else {
            findNavController().navigate(ContactPickerPermissionFragmentDirections.toContactPicker())
        }
    }

}