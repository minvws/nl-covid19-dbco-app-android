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
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoPairingExplanationBinding

class ReversePairingExplanationFragment :
    BaseFragment(R.layout.fragment_selfbco_pairing_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoPairingExplanationBinding.bind(view)

        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.btnYes.setOnClickListener {
            findNavController().navigate(
                ReversePairingExplanationFragmentDirections.toReversePairingFragment(
                    credentials = null,
                    initWithInvalidCodeState = false
                )
            )
        }
        binding.btnNo.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}