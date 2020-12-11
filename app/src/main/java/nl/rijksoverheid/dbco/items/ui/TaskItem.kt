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
import nl.rijksoverheid.dbco.databinding.ItemTaskBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import java.io.Serializable

class TaskItem(
    val task: Task
) : BaseBindableItem<ItemTaskBinding>(), Serializable {

    override fun bind(viewBinding: ItemTaskBinding, position: Int) {
        viewBinding.task = task

        val displayName = task.linkedContact?.getDisplayName()
        var labelToDisplay = when {
            displayName?.isNotEmpty() == true -> displayName
            task.label?.isNotEmpty() == true -> task.label
            else -> viewBinding.root.resources.getString(R.string.mycontacts_name_unknown)
        }
        if(!task.taskContext.isNullOrEmpty()){
            labelToDisplay = "$labelToDisplay (${task.taskContext})"
        }
        viewBinding.indexContactName.text = labelToDisplay

        viewBinding.indexContactSubtitle.setText(getCommunicationContext())

        // completeness indicator
        if(task.getStatus() == 0 || task.linkedContact == null) {
            viewBinding.indexContactState.setImageResource(R.drawable.ic_warning_status)
            viewBinding.indexTaskProgress.visibility = View.GONE
        } else {
            setStatus(task.getStatus() ,viewBinding)
        }
    }

    private fun setStatus(progress: Int, viewBinding: ItemTaskBinding){
        viewBinding.indexContactState.setImageResource(R.drawable.ic_check_icon)
        viewBinding.indexTaskProgress.visibility = View.VISIBLE
        viewBinding.indexTaskProgress.progress = progress
    }

    private fun getCommunicationContext() : Int{
        return if(task.getStatus() > 0 && task.communication == CommunicationType.Staff){
            R.string.communication_context_staff_data
        }else if(task.getStatus() == 0) {
           R.string.communication_context_no_data
        }else if (task.communication == CommunicationType.Index && !task.didInform && task.getStatus() > 0){
           R.string.communication_context_index_not_informed
        }else if(task.communication == CommunicationType.Index && task.didInform){
            R.string.communication_context_index_informed
        }else{
            R.string.communication_context_blank
        }

    }

    override fun getLayout() = R.layout.item_task

    override fun isClickable(): Boolean {
        return true
    }
}
