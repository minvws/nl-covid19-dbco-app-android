/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private val appLifecycleViewModel by viewModels<AppLifecycleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appLifecycleViewModel.appConfig.observe(viewLifecycleOwner, {
            findNavController().navigate(SplashFragmentDirections.toOnboarding())
        })
        appLifecycleViewModel.checkForForcedAppUpdate()
    }
}