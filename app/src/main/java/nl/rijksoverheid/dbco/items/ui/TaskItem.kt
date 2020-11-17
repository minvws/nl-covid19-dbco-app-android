/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemTaskBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.PieProgressDrawable
import java.io.Serializable

class TaskItem(
    val task: Task
) : BaseBindableItem<ItemTaskBinding>(), Serializable {

    override fun bind(viewBinding: ItemTaskBinding, position: Int) {
        viewBinding.task = task

        val displayName = task.linkedContact?.getDisplayName()
        viewBinding.indexContactName.text = if (displayName.isNullOrEmpty()) task.label else displayName

        // completeness indicator
        when (task.status) {
            0 -> viewBinding.indexContactState.setImageResource(R.drawable.ic_warning_status)
            1 -> getPieDrawable(33, viewBinding)
            2 -> getPieDrawable(66, viewBinding)
            else -> viewBinding.indexContactState.setImageResource(R.drawable.ic_completed)
        }
    }

    private fun getPieDrawable(progress: Int, viewBinding: ItemTaskBinding) {
        val resources = viewBinding.root.resources
        val color = resources.getColor(R.color.color_primary)
        val pieProgressDrawable = PieProgressDrawable()
        pieProgressDrawable.setColor(color)
        pieProgressDrawable.onLevelChange(progress)
        pieProgressDrawable.setBorderWidth(2f, resources.displayMetrics)
        viewBinding.indexContactState.setImageDrawable(pieProgressDrawable)
    }

    override fun getLayout() = R.layout.item_task

    override fun isClickable(): Boolean {
        return true
    }
}
