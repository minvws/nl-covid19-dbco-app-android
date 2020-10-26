/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.user.UserRepository

class FillCodeViewModel(val context: Context) : ViewModel() {

    fun pair(pin: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                UserRepository(context).pair(pin)
                // TODO notify fragment
            }
        }
    }
}