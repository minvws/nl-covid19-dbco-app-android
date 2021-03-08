/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingBinding
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingResult
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus

class ReversePairingFragment : BaseFragment(R.layout.fragment_selfbco_pairing) {

    lateinit var binding: FragmentSelfbcoPairingBinding

    private val reversePairingViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            ReversePairingViewModel::class.java
        )
    }
    private val pairingViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            PairingViewModel::class.java
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoPairingBinding.bind(view)

        setupBackButton()
        setUpListeners()

        binding.btnNext.setOnClickListener {
            findNavController().navigate(ReversePairingFragmentDirections.toFinalizeCheckFragment())
        }

        reversePairingViewModel.retrievePairingCode()
    }

    private fun setUpListeners() {
        reversePairingViewModel.pairingCode.observe(viewLifecycleOwner, { code ->
            binding.pairingCode.text = StringBuilder(code)
                .insert(code.length / 2, "-")
                .toString()
        })

        reversePairingViewModel.pairingStatus.observe(viewLifecycleOwner, { status ->
            when (status) {
                is ReversePairingStatus.Success -> pairingViewModel.pair(status.code)
                is ReversePairingStatus.Error -> {
                    // TODO
                }
                is ReversePairingStatus.Expired -> {
                    // TODO
                }
                is ReversePairingStatus.Pairing -> {
                    // TODO
                }
            }
        })

        pairingViewModel.pairingResult.observe(viewLifecycleOwner, { result ->
            when (result) {
                is PairingResult.Success -> {
                    binding.loadingIndicator.visibility = View.INVISIBLE
                    binding.pairedIndicator.visibility = View.VISIBLE
                    binding.stateText.text = getString(R.string.selfbco_reverse_pairing_paired)
                    binding.btnNext.isEnabled = true
                    reversePairingViewModel.cancelPollingForChanges()
                }
                is PairingResult.Invalid -> {
                    // TODO what?
                }
                is PairingResult.Error -> {
                    showErrorDialog(getString(R.string.error_while_pairing), {}, result.exception)
                }
            }
        })
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            if (hasUserSharedCode()) {
                findNavController().popBackStack()
            } else {
                showShareCodeDialog()
            }
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    // Handle the back button event
                    if (hasUserSharedCode()) {
                        findNavController().popBackStack()
                    } else {
                        showShareCodeDialog()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun hasUserSharedCode(): Boolean {
        return reversePairingViewModel.userHasSharedCode.value == true
    }

    private fun showShareCodeDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.selfbco_reverse_pairing_share_message_title)
        builder.setPositiveButton(R.string.answer_yes) { dialog, _ ->
            reversePairingViewModel.setUserHasSharedCode(true)
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}