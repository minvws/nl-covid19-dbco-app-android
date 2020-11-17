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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsBinding
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.TaskItem
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.resolve
import timber.log.Timber

/**
 * Overview fragment showing selected or suggested contacts of the user
 */

class MyContactsFragment : BaseFragment(R.layout.fragment_my_contacts) {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val tasksViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            TasksOverviewViewModel::class.java
        )
    }

    private val contentSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentSection.setHideWhenEmpty(true)
        adapter.add(contentSection)

        // Load data from backend
        tasksViewModel.syncTasks()
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

        binding.sendButton.setOnClickListener {
            findNavController().navigate(MyContactsFragmentDirections.toFinalizeCheck())
        }

        tasksViewModel.fetchCase.observe(viewLifecycleOwner, { resource ->
            resource.resolve(onError = {
                showErrorDialog(getString(R.string.error_while_fetching_case), {
                    tasksViewModel.syncTasks()
                }, it)
            }, onSuccess = {case ->
                contentSection.clear()
                val uninformedSection = Section().apply {
                    setHeader(
                        DuoHeaderItem(
                            R.string.mycontacts_uninformed_header,
                            R.string.mycontacts_uninformed_subtext
                        )
                    )
                }
                val informedSection = Section()
                    .apply {
                        setHeader(
                            DuoHeaderItem(
                                R.string.mycontacts_informed_header,
                                R.string.mycontacts_informed_subtext
                            )
                        )
                    }


                case?.tasks?.forEach { task ->
                    Timber.d("Found task $task")
                    when (task.taskType) {
                        "contact" -> {
                            val informed = when (task.communication) {
                                CommunicationType.Index -> task.contactIsInformedAlready
                                CommunicationType.Staff -> task.linkedContact?.hasEmailOrPhone() == true
                                else -> false
                            }
                            if (informed) {
                                informedSection.add(TaskItem(task))
                            } else {
                                uninformedSection.add(TaskItem(task))
                            }
                        }
                    }
                }

                if (uninformedSection.groupCount > 1) {
                    contentSection.add(uninformedSection)
                }

                if (informedSection.groupCount > 1) {
                    contentSection.add(informedSection)
                }

                binding.sendButton.isEnabled = tasksViewModel.ifCaseWasChanged()
            })

        })

        adapter.setOnItemClickListener { item, view ->
            if (item is TaskItem) {
                checkPermissionAndNavigate(item.task)
            }
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

            if (task?.linkedContact != null) {
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactDetails(
                        task,
                        task.linkedContact
                    )
                )
            } else {
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactPickerSelection(
                        task
                    )
                )
            }
        }
    }

}