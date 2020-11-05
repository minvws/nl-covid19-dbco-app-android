/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.dbco.applifecycle

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentEndOfLifeBinding


class EndOfLifeFragment : BaseFragment(R.layout.fragment_end_of_life) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentEndOfLifeBinding.bind(view)
        binding.next.setOnClickListener {
            // To do: Determine what to do when end of life.
            // On CoronaMelder this would take a user to a website with information
        }
    }
}
