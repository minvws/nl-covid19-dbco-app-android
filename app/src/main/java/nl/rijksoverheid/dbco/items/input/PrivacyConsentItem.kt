/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemPrivacyConsentBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class PrivacyConsentItem(
    private val isChecked: Boolean,
    private val checkedListener: (Boolean) -> Unit
) : BaseBindableItem<ItemPrivacyConsentBinding>() {

    override fun bind(viewBinding: ItemPrivacyConsentBinding, position: Int) {
        viewBinding.termsAgree.isChecked = isChecked
        viewBinding.termsAgree.setOnCheckedChangeListener { _, isChecked ->
            checkedListener(isChecked)
        }
    }

    override fun getLayout(): Int = R.layout.item_privacy_consent
}