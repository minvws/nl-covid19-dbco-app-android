/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.dbco.items.input

import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemNoRiskBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

class NoRiskItem : BaseBindableItem<ItemNoRiskBinding>() {

    override fun getLayout() = R.layout.item_no_risk

    override fun isSameAs(other: Item<*>): Boolean = false

    override fun hasSameContentAs(other: Item<*>) = false

    override fun bind(viewBinding: ItemNoRiskBinding, position: Int) {
        // do nothing
    }
}
