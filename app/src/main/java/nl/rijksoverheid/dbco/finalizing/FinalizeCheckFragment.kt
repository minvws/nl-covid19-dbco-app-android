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
import androidx.lifecycle.Observer
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
        binding.toolbar.visibility = View.GONE

        tasksViewModel.indexTasks.observe(viewLifecycleOwner, Observer {
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


            it.case?.tasks?.forEach { task ->
                Timber.d("Found task $task")
                if (task.questionnaireResult == null) {
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
            }

            if (informPersonallySection.groupCount > 1) {
                contentSection.add(informPersonallySection)
            }

            if (informGgdSection.groupCount > 1) {
                contentSection.add(informGgdSection)
            }


        })

        binding.sendButton.setOnClickListener {
            findNavController().navigate(FinalizeCheckFragmentDirections.toFinalizeSentFragment())
        }
    }


}