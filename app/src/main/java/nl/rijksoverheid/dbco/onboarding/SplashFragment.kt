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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.AppViewModel
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.Start
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.MyTasks
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.Consent
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation

class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by viewModels()

    private val appViewModel: AppViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            handleNavigation(navigation)
        })
        appViewModel.appConfig.observe(viewLifecycleOwner, { config ->
            viewModel.onConfigLoaded(config)
        })
    }

    private fun handleNavigation(navigation: Navigation) {
        val direction = when (navigation) {
            is MyTasks -> SplashFragmentDirections.toMyContacts()
            is Consent -> SplashFragmentDirections.toOnboardingPrivacyConsentFragment(
                canGoBack = false
            )
            is Start -> SplashFragmentDirections.toOnboardingStartFragment()
        }
        findNavController().navigate(direction)
    }
}