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
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFinalizingLoadingBinding
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.UploadStatus
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.UploadStatus.UploadError
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.UploadStatus.UploadSuccess

class FinalizeLoadingFragment : BaseFragment(R.layout.fragment_finalizing_loading) {

    private val tasksViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            TasksOverviewViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FragmentFinalizingLoadingBinding.bind(view)

        tasksViewModel.uploadStatus.observe(viewLifecycleOwner, { result -> handleUpload(result) })
        tasksViewModel.uploadCurrentCase()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(
                        FinalizeLoadingFragmentDirections.toMyContactsFragment()
                    )
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleUpload(result: UploadStatus) {

        if (result is UploadSuccess) {
            findNavController().navigate(FinalizeLoadingFragmentDirections.toFinalizeSentFragment())
        } else if (result is UploadError) {
            showErrorDialog(getString(R.string.generic_error_prompt_message), {
                tasksViewModel.uploadCurrentCase()
            })
        }
    }
}