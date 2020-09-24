/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.AboutFragment
import nl.rijksoverheid.dbco.databinding.FragmentListBinding
import nl.rijksoverheid.dbco.items.BulletedListItem
import nl.rijksoverheid.dbco.items.ButtonItem
import nl.rijksoverheid.dbco.items.HeaderItem
import nl.rijksoverheid.dbco.items.ParagraphItem

class ContactPickerAboutFragment : BaseFragment(R.layout.fragment_list) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val requestCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(
                    context,
                    "Access to Contacts given after request",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Access to Contacts denied after request",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = Section(
            listOf(
                HeaderItem(R.string.placeholder),
                ParagraphItem(R.string.placeholder_long),
                BulletedListItem(R.string.placeholder_list),
                ButtonItem(R.string.app_name, { requestContactAccess() })
            )
        )

        adapter.add(content)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListBinding.bind(view)
        //binding.toolbar.setTitle(getString(R.string.about_app_title))
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
            findNavController().navigate(ContactPickerAboutFragmentDirections.toContactPicker())
        }
    }

}