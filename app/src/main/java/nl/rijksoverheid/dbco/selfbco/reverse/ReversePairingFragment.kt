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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.mycontacts.MyContactsFragmentDirections
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingBinding
import nl.rijksoverheid.dbco.onboarding.FillCodeFragment
import nl.rijksoverheid.dbco.onboarding.FillCodeFragmentDirections
import nl.rijksoverheid.dbco.onboarding.FillCodeViewModel
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState
import nl.rijksoverheid.dbco.util.accessibilityAnnouncement
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.resolve
import nl.rijksoverheid.dbco.util.scrollTo
import retrofit2.HttpException


class ReversePairingFragment : BaseFragment(R.layout.fragment_selfbco_pairing) {

    lateinit var binding: FragmentSelfbcoPairingBinding
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            ReversePairingViewmodel::class.java
        )
    }
    private val pairingViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            FillCodeViewModel::class.java
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoPairingBinding.bind(view)
        setupBackButton()

        setUpListeners()

        requestPairingCode()

        binding.btnNext.setOnClickListener {
            findNavController().navigate(ReversePairingFragmentDirections.toFinalizeCheckFragment())
        }

    }

    private fun setUpListeners() {
        viewModel.reversePairingCode.observe(viewLifecycleOwner, { retrievedCode ->
            retrievedCode?.let {
                val showCode =
                    StringBuilder(retrievedCode).insert(retrievedCode.length / 2, "-").toString()
                binding.pairingCode.text = showCode
                // Make sure to kill any pre-existing pollers before starting one with the new code
                viewModel.cancelPollingForChanges()

                checkPairingCodeStatus()
            }
        })

        viewModel.reversePairingResult.observe(viewLifecycleOwner, { response ->
            if (response.status == ReversePairingState.COMPLETED) {
                response.pairingCode?.let {
                    pairingViewModel.pair(response.pairingCode)
                }
            }
        })

        setUpRegularPairingListener()
    }

    private fun setUpRegularPairingListener() {
        // Setup pairing logic
        pairingViewModel.pairingResult.observe(viewLifecycleOwner, { resource ->
            resource?.resolve(onError = { exception ->


                if (exception is HttpException && exception.code() == 400) {
                    Toast.makeText(requireContext(), "Error 400 met koppelen", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    showErrorDialog(getString(R.string.error_while_pairing), {
                    }, exception)
                }

            }, onSuccess = {
                // Handle success flow
                Toast.makeText(requireContext(), "Succesvol gekoppeld", Toast.LENGTH_SHORT).show()
                binding.loadingIndicator.visibility = View.INVISIBLE
                binding.pairedIndicator.visibility = View.VISIBLE
                binding.stateText.text = "Gekoppeld met GGD"
                binding.btnNext.isEnabled = true
                // Cancel again, just in case
                viewModel.cancelPollingForChanges()
            })
        })
    }

    private fun requestPairingCode() {
        viewModel.retrievePairingCode()
    }

    private fun checkPairingCodeStatus() {
        // Start polling on receiving code
        viewModel.pollForChanges()
    }

    private fun setupBackButton() {
        // Handle button on screen and back button
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
        // Check viewmodel value if user shared code
        return viewModel.userHasSharedCode.value == true
    }

    private fun showShareCodeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Heb je de code gedeeld met de GGD-medewerker?")
        builder.setPositiveButton(R.string.answer_yes) { dialog, _ ->
            // Start background polling
            viewModel.setUserHasSharedCode(true)
            dialog.dismiss()
            findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


}