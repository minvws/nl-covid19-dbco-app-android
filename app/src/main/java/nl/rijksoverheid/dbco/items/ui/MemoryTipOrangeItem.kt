/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemMemorytipTimelineOrangeBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

/**
 * Simple class showing a static memory tip
 */
class MemoryTipOrangeItem: BaseBindableItem<ItemMemorytipTimelineOrangeBinding>() {

    override fun bind(viewBinding: ItemMemorytipTimelineOrangeBinding, position: Int) {
        /* NO OP */
    }

    override fun getLayout(): Int = R.layout.item_memorytip_timeline_orange

    override fun isClickable(): Boolean = true

    override fun isLongClickable() = true
}