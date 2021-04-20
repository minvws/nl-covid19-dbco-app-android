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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFinalizingCheckBinding
import nl.rijksoverheid.dbco.items.ui.DuoHeaderItem
import nl.rijksoverheid.dbco.items.ui.TaskItem
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.TaskType
import timber.log.Timber

class FinalizeCheckFragment : BaseFragment(R.layout.fragment_finalizing_check) {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val tasksViewModel: TasksOverviewViewModel by activityViewModels()

    private val contentSection = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentSection.setHideWhenEmpty(true)
        adapter.add(contentSection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFinalizingCheckBinding.bind(view)
        initToolbar(binding)
        binding.content.adapter = adapter

        tasksViewModel.getCachedCase().let { case ->
            contentSection.clear()
            val noPhoneOrEmailSection = Section()
                .apply {
                    setHeader(
                        DuoHeaderItem(
                            getString(R.string.finalize_no_phone_or_email_header),
                            getString(R.string.finalize_no_phone_or_email_subtext)
                        )
                    )
                }


            case.tasks.forEach { task ->
                Timber.d("Found task $task")
                when (task.taskType) {
                    TaskType.Contact -> {
                        // Check if all (required) data has been filled in
                        val hasEmailOrPhone = task.linkedContact?.hasValidEmailOrPhone() == true

                        if (!hasEmailOrPhone) {
                            noPhoneOrEmailSection.add(TaskItem(task))
                        }
                    }
                }
            }


            if (noPhoneOrEmailSection.groupCount > 1) {
                contentSection.add(noPhoneOrEmailSection)
            }

            // Auto upload and continue if no contacts require extra checking. Check for groupcount <= 1 as preset headers can count against this
            if (noPhoneOrEmailSection.groupCount <= 1) {
                findNavController().navigate(FinalizeCheckFragmentDirections.toFinalizeLoadingFragment())
            }
        }

        adapter.setOnItemClickListener { item, view ->
            if (item is TaskItem) {
                findNavController().navigate(
                    FinalizeCheckFragmentDirections.toContactDetailsInputFragment(
                        indexTask = item.task,
                        enabled = true
                    )
                )
            }
        }

        binding.sendButton.setOnClickListener {
            findNavController().navigate(FinalizeCheckFragmentDirections.toFinalizeLoadingFragment())
        }
    }

    private fun initToolbar(binding: FragmentFinalizingCheckBinding) {
        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }
}