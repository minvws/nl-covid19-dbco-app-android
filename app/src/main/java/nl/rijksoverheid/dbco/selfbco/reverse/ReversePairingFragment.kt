/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingBinding
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingResult.Error
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingResult.Success
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingResult.Invalid

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

        reversePairingViewModel.pairingResult.observe(viewLifecycleOwner, { completed ->
            pairingViewModel.pair(completed.code)
        })

        pairingViewModel.pairingResult.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Success -> {
                    binding.loadingIndicator.visibility = View.INVISIBLE
                    binding.pairedIndicator.visibility = View.VISIBLE
                    binding.stateText.text = "Gekoppeld met GGD"
                    binding.btnNext.isEnabled = true
                    reversePairingViewModel.cancelPollingForChanges()
                }
                is Invalid -> {
                    // TODO what?
                }
                is Error -> {
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Heb je de code gedeeld met de GGD-medewerker?")
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