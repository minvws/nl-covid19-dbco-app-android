/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.finalizing

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFinalizingCheckBinding
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.TaskItem
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import timber.log.Timber

class FinalizeCheckFragment : BaseFragment(R.layout.fragment_finalizing_check) {

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFinalizingCheckBinding.bind(view)
        binding.content.adapter = adapter

        tasksViewModel.getCachedCase().let { case ->
            contentSection.clear()
            val uninformedSection = Section().apply {
                setHeader(
                        DuoHeaderItem(
                                R.string.finalize_uninformed_header,
                                R.string.finalize_uninformed_subtext
                        )
                )
            }
            val noPhoneOrEmailSection = Section()
                    .apply {
                        setHeader(
                                DuoHeaderItem(
                                        R.string.finalize_no_phone_or_email_header,
                                        R.string.finalize_no_phone_or_email_subtext
                                )
                        )
                    }


            case?.tasks?.forEach { task ->
                Timber.d("Found task $task")
                when (task.taskType) {
                    "contact" -> {
                        val hasEmailOrPhone = task.linkedContact?.hasValidEmailOrPhone() == true

                        val informed = when (task.communication) {
                            CommunicationType.Index -> task.contactIsInformedAlready
                            CommunicationType.Staff -> hasEmailOrPhone
                            else -> false
                        }

                        if (!informed) {
                            if (hasEmailOrPhone) {
                                uninformedSection.add(TaskItem(task))
                            } else {
                                noPhoneOrEmailSection.add(TaskItem(task))
                            }
                        }
                    }
                }
            }

            if (uninformedSection.groupCount > 1) {
                contentSection.add(uninformedSection)
            }

            if (noPhoneOrEmailSection.groupCount > 1) {
                contentSection.add(noPhoneOrEmailSection)
            }
        }

        adapter.setOnItemClickListener { item, view ->
            if (item is TaskItem) {
                findNavController().navigate(
                    FinalizeCheckFragmentDirections.toContactDetailsInputFragment(
                        item.task,
                        item.task.linkedContact
                    )
                )
            }
        }

        binding.sendButton.setOnClickListener {

            tasksViewModel.sendCurrentCase()

            findNavController().navigate(FinalizeCheckFragmentDirections.toFinalizeSentFragment())
        }
    }


}