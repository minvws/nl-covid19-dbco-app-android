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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentFinalizingLoadingBinding
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadSuccess

class FinalizeLoadingFragment : BaseFragment(R.layout.fragment_finalizing_loading) {

    private val tasksViewModel: TasksOverviewViewModel by activityViewModels()

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
            showError()
        }
    }

    private fun showError() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setCancelable(false)
            setTitle(R.string.error)
            setMessage(R.string.generic_error_prompt_message)
            setPositiveButton(R.string.error_try_again) { dialogInterface, _ ->
                tasksViewModel.uploadCurrentCase()
                dialogInterface.dismiss()
            }
            setNegativeButton(R.string.close) { dialogInterface, _ ->
                dialogInterface.dismiss()
                findNavController().navigate(FinalizeLoadingFragmentDirections.toMyContactsFragment())
            }
        }.create().show()
    }
}