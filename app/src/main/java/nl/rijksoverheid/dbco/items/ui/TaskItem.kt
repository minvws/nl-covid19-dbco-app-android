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
import nl.rijksoverheid.dbco.util.setImageResource
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
        if (task.getStatus() == 0 || task.linkedContact == null || (task.communication == CommunicationType.Index && !task.didInform)) {
            viewBinding.indexContactState.setImageResource(R.drawable.ic_warning_status, R.string.important)
            viewBinding.indexTaskProgress.visibility = View.GONE
        } else {
            setStatus(task.getStatus() ,viewBinding)
        }
    }

    private fun setStatus(progress: Int, viewBinding: ItemTaskBinding) {
        viewBinding.indexContactState.setImageResource(R.drawable.ic_check_icon, R.string.completed)
        viewBinding.indexTaskProgress.visibility = View.VISIBLE
        viewBinding.indexTaskProgress.progress = progress
    }

    private fun getCommunicationContext() : Int{
        // If status is not fully complete, always show the 'add more data' context
        return if(task.getStatus() < 100) {
            R.string.communication_context_no_data
        }
        // If fully filled in, and communication is staff, show staff-oriented message
        else if(task.getStatus() == 100 && task.communication == CommunicationType.Staff){
            R.string.communication_context_staff_data
        }
        // If fully filled in, and communication is index, show index-oriented message based on inform state
        else if(task.getStatus() == 100 && task.communication == CommunicationType.Index && !task.didInform) {
            R.string.communication_context_index_not_informed
        }else if (task.getStatus() == 100 && task.communication == CommunicationType.Index && task.didInform){
            R.string.communication_context_index_informed
        }else{
            // Catch-all, shouldn't arrive here
            R.string.communication_context_blank
        }

    }

    override fun getLayout() = R.layout.item_task

    override fun isClickable(): Boolean {
        return true
    }
}
