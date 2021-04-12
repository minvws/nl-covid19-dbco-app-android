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
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingBinding
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.*

@ExperimentalSerializationApi
class ReversePairingFragment : BaseFragment(R.layout.fragment_selfbco_pairing) {

    private val args: ReversePairingFragmentArgs by navArgs()

    lateinit var binding: FragmentSelfbcoPairingBinding

    private val pairingViewModel: PairingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoPairingBinding.bind(view)

        setupBackButton()
        setUpListeners()

        binding.btnNext.setOnClickListener {
            findNavController().navigate(ReversePairingFragmentDirections.toFinalizeCheckFragment())
        }

        binding.retryWithNewCode.setOnClickListener { pairingViewModel.startReversePairing() }

        if (savedInstanceState != null) return
        if (args.initWithInvalidCodeState) {
            showInvalidCode()
        } else {
            pairingViewModel.startReversePairing(args.credentials)
        }
    }

    private fun showInvalidCode() {
        binding.pairingCode.isVisible = false
        binding.pairingExpiredCodeContainer.isVisible = true
    }

    private fun showPairingError(credentials: ReversePairingCredentials) {
        binding.retryPairing.setOnClickListener { pairingViewModel.startReversePairing(credentials) }
        binding.pairingLoadingIndicator.isVisible = false
        binding.stateText.isVisible = false
        binding.pairingErrorContainer.isVisible = true
    }

    private fun showPairing() {
        binding.pairingErrorContainer.isVisible = false
        binding.pairingLoadingIndicator.isVisible = true
        binding.stateText.isVisible = true
    }

    private fun setUpListeners() {
        pairingViewModel.pairingCode.observe(viewLifecycleOwner, { code ->
            binding.pairingExpiredCodeContainer.isVisible = false
            binding.pairingCode.isVisible = true
            binding.pairingCode.text = StringBuilder(code)
                .insert(code.length / 2, "-")
                .toString()
        })

        pairingViewModel.reversePairingStatus.observe(viewLifecycleOwner, { status ->
            when (status) {
                is ReversePairingSuccess -> {
                    binding.pairingLoadingIndicator.isVisible = false
                    binding.pairedIndicator.isVisible = true
                    binding.stateText.text = getString(R.string.selfbco_reverse_pairing_paired)
                    binding.btnNext.isEnabled = true
                }
                is ReversePairingError -> showPairingError(status.credentials)
                is ReversePairingExpired -> showInvalidCode()
                is ReversePairing -> showPairing()
                else -> { /* NO-OP */
                }
            }
        })
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            handleBackPress()
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleBackPress() {
        if (hasUserSharedCode()) {
            findNavController().popBackStack()
        } else {
            showShareCodeDialog()
        }
    }

    private fun hasUserSharedCode(): Boolean {
        return pairingViewModel.userHasSharedCode.value == true
    }

    private fun showShareCodeDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.selfbco_reverse_pairing_share_message_title)
        builder.setPositiveButton(R.string.answer_yes) { dialog, _ ->
            pairingViewModel.setUserHasSharedCode(true)
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
            pairingViewModel.cancelPairing()
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.create().show()
    }
}