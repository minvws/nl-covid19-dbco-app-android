/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import android.view.View
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactAddBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.selfbco.timeline.TimelineSection

class TimelineContactAddItem(section: TimelineSection, private val addListener: OnAddClickedListener) : BaseBindableItem<ItemContactAddBinding>() {

    private val onClickListener = View.OnClickListener {
        addListener.onAddClicked(section)
    }
    override fun bind(viewBinding: ItemContactAddBinding, position: Int) {
        viewBinding.root.setOnClickListener(onClickListener)
    }
    override fun getLayout(): Int = R.layout.item_contact_add
    override fun isClickable(): Boolean = true
    override fun isLongClickable() = true


    interface OnAddClickedListener {
        fun onAddClicked(section: TimelineSection)
    }

}