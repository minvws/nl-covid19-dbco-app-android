/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemTaskBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.util.setImageResource
import java.io.Serializable
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType.Staff

class TaskItem(
    val task: Task
) : BaseBindableItem<ItemTaskBinding>(), Serializable {

    override fun bind(viewBinding: ItemTaskBinding, position: Int) {
        viewBinding.task = task
        viewBinding.indexContactName.text = task.getDisplayName(
            viewBinding.root.resources.getString(R.string.mycontacts_name_unknown)
        )

        if (!task.hasEssentialData()) {
            setWarningStatus(viewBinding)
        } else if (task.shouldInform) {
            setInformStatus(viewBinding)
        } else {
            setCompletionStatus(task.getPercentageCompletion(), viewBinding)
        }
    }

    private fun setWarningStatus(viewBinding: ItemTaskBinding) {
        viewBinding.indexContactState.setImageResource(
            R.drawable.ic_warning_status,
            R.string.important
        )
        viewBinding.indexTaskProgress.visibility = View.GONE
        viewBinding.indexContactSubtitle.setText(R.string.communication_context_no_data)
        viewBinding.indexContactSubtitle.setTextColor(
            ContextCompat.getColor(viewBinding.root.context, R.color.secondary_text)
        )
    }

    private fun setInformStatus(viewBinding: ItemTaskBinding) {
        viewBinding.indexContactState.setImageResource(
            R.drawable.ic_warning_status_filled,
            R.string.important
        )
        viewBinding.indexTaskProgress.isVisible = false
        viewBinding.indexContactSubtitle.setText(R.string.communication_context_index_not_informed)
        viewBinding.indexContactSubtitle.setTextColor(
            ContextCompat.getColor(viewBinding.root.context, R.color.orange)
        )
    }

    private fun setCompletionStatus(progress: Int, viewBinding: ItemTaskBinding) {
        viewBinding.indexContactState.setImageResource(R.drawable.ic_check_icon, R.string.completed)
        viewBinding.indexTaskProgress.isVisible = true
        viewBinding.indexTaskProgress.progress = progress
        val subtext = when {
            task.communication == Staff -> R.string.communication_context_staff_data
            task.notGoingToBeInformedByIndex -> R.string.communication_context_index_informed_denied
            else -> R.string.communication_context_index_informed
        }
        viewBinding.indexContactSubtitle.setText(subtext)
        viewBinding.indexContactSubtitle.setTextColor(
            ContextCompat.getColor(viewBinding.root.context, R.color.secondary_text)
        )
    }

    override fun getLayout() = R.layout.item_task

    override fun isClickable(): Boolean = true
}
