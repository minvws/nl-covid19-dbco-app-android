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
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingBinding


class ReversePairingFragment : BaseFragment(R.layout.fragment_selfbco_pairing) {

    lateinit var binding: FragmentSelfbcoPairingBinding
    private val viewModel by viewModels<ReversePairingViewmodel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoPairingBinding.bind(view)
        setupBackButton()

        setUpListeners()

        requestPairingCode()

    }

    private fun setUpListeners() {
        viewModel.reversePairingCode.observe(viewLifecycleOwner, { retrievedCode ->
            retrievedCode?.let {
                val showCode =
                    StringBuilder(retrievedCode).insert(retrievedCode.length / 2, "-").toString()
                binding.pairingCode.text = showCode

                checkPairingCodeStatus()
            }
        })


        viewModel.userHasPaired.observe(viewLifecycleOwner, { hasPaired: Boolean ->
            if (hasPaired) {
                Toast.makeText(requireContext(), "User has paired", Toast.LENGTH_SHORT).show()
                binding.btnNext.isEnabled = true
            }
        })

    }

    private fun requestPairingCode() {
        viewModel.retrievePairingCode()
    }

    private fun checkPairingCodeStatus(){
        // Start polling on receiving code
       // viewModel.checkPairingStatus()
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
            checkPairingCodeStatus()
            viewModel.setUserHasSharedCode(true)
            dialog.dismiss()
            //findNavController().popBackStack()
        }
        builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


}