/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemMemorytipTimelineGrayBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem

/**
 * Simple class showing a static memory tip
 */
class MemoryTipGrayItem : BaseBindableItem<ItemMemorytipTimelineGrayBinding>() {

    override fun bind(viewBinding: ItemMemorytipTimelineGrayBinding, position: Int) {
        /* NO OP */
    }

    override fun getLayout(): Int = R.layout.item_memorytip_timeline_gray

    override fun isClickable(): Boolean = true

    override fun isLongClickable() = true
}