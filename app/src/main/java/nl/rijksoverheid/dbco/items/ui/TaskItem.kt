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
import java.io.Serializable

class TaskItem(
    val task: Task
) : BaseBindableItem<ItemTaskBinding>(), Serializable {


    override fun bind(viewBinding: ItemTaskBinding, position: Int) {
        viewBinding.task = task

    }

    override fun getLayout() = R.layout.item_task


    override fun isClickable(): Boolean {
        return true
    }


}
