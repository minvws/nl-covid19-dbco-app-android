/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.widget.CheckBox
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel

class PrivacyConsentItem(private val viewModel : OnboardingConsentViewModel)  : BaseBindableItem<ItemPrivacyConsentBinding>() {
    override fun bind(viewBinding: ItemPrivacyConsentBinding, position: Int) {
        viewBinding.termsAgree.setOnClickListener {
            viewModel.termsAgreed.postValue((it as CheckBox).isChecked)
        }
    }

    override fun getLayout(): Int = R.layout.item_privacy_consent
}