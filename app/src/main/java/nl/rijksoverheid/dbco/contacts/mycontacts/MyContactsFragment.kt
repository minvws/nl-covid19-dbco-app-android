/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.mycontacts

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.entity.CommunicationType
import nl.rijksoverheid.dbco.contacts.data.entity.Task
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsBinding
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.TaskItem
import timber.log.Timber

/**
 * Overview fragment showing selected or suggested contacts of the user
 */

class MyContactsFragment : BaseFragment(R.layout.fragment_my_contacts) {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private val contentSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentSection.setHideWhenEmpty(true)
        adapter.add(contentSection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyContactsBinding.bind(view)

        binding.buildVersion.text = getString(
            R.string.status_app_version,
            BuildConfig.VERSION_NAME,
            "${BuildConfig.VERSION_CODE}-${BuildConfig.GIT_VERSION}"
        )

        binding.content.adapter = adapter

        binding.toolbar.visibility = View.GONE

        binding.manualEntryButton.setOnClickListener {
            checkPermissionAndNavigate()
        }

        contactsViewModel.indexTasksLivedata.observe(viewLifecycleOwner, Observer {
            contentSection.clear()
            val informPersonallySection = Section().apply {
                setHeader(
                    DuoHeaderItem(
                        R.string.mycontacts_inform_personally_header,
                        R.string.mycontacts_inform_subtext
                    )
                )
            }
            val informGgdSection = Section()
                .apply {
                    setHeader(
                        DuoHeaderItem(
                            R.string.mycontacts_inform_ggd_header,
                            R.string.mycontacts_inform_subtext
                        )
                    )
                }


            it.tasks?.forEach { task ->
                Timber.d("Found task $task")
                when (task.taskType) {
                    "contact" -> {
                        when (task.communication) {
                            CommunicationType.Index -> {
                                informPersonallySection.add(TaskItem(task))
                            }
                            CommunicationType.Staff -> {
                                informGgdSection.add(TaskItem(task))
                            }
                            else -> {
                                informPersonallySection.add(TaskItem(task))
                            }
                        }

                    }
                }
            }

            if (informPersonallySection.groupCount > 1) {
                contentSection.add(informPersonallySection)
            }

            if (informGgdSection.groupCount > 1) {
                contentSection.add(informGgdSection)
            }


        })

        adapter.setOnItemClickListener { item, view ->
            if (item is TaskItem) {
                checkPermissionAndNavigate(item.task)
            }
        }

        // Fake loading from backend
        lifecycleScope.launch {
            delay(250)
            contactsViewModel.fetchTasksForUUID()
        }


    }

    private fun checkPermissionAndNavigate(task: Task? = null) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If not granted send users to permission grant screen
            findNavController().navigate(MyContactsFragmentDirections.toContactPickerAbout(task))
        } else {
            findNavController().navigate(MyContactsFragmentDirections.toContactPickerSelection(task))
        }
    }

}