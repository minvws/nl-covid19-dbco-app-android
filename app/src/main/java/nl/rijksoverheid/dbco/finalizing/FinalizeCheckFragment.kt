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
import nl.rijksoverheid.dbco.items.ui.TaskItem
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel

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

        val case = tasksViewModel.getCachedCase()

        if (case.hasEssentialTaskData()) {
            findNavController().navigate(FinalizeCheckFragmentDirections.toFinalizeLoadingFragment())
        } else {
            contentSection.clear()
            val incompleteTasks = Section().apply {
                case.tasks.filter { task ->
                    !task.hasEssentialData()
                }.forEach { task ->
                    add(TaskItem(task))
                }
            }
            contentSection.add(incompleteTasks)
        }

        adapter.setOnItemClickListener { item, _ ->
            if (item is TaskItem) {
                findNavController().navigate(
                    FinalizeCheckFragmentDirections.toContactDetailsInputFragment(
                        indexTaskUuid = item.task.uuid!!,
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