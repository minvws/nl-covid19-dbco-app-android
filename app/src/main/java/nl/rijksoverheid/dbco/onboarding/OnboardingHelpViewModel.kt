/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.user.IUserRepository

class OnboardingHelpViewModel(userRepository: IUserRepository) : ViewModel() {

    val skipOnboarding = userRepository.getToken() != null

}