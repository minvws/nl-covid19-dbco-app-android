/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
import nl.rijksoverheid.dbco.items.QuestionnaireItem
import timber.log.Timber

class QuestionnaireSection(
    private val lifecycleOwner: LifecycleOwner,
    private val headerItem: QuestionnaireSectionHeader,
    expandedByDefault: Boolean = false
) : ExpandableGroup(headerItem, expandedByDefault) {


    override fun add(group: Group) {
        super.add(group)
        if (group is QuestionnaireItem) {
            group.getViewStateLiveData().observe(lifecycleOwner, Observer { state ->
                if (state.isCompleted) {
                    Timber.d("Received isCompleted = true from $group")
                    checkReadiness()
                }
            })
        }
    }

    override fun addAll(groups: Collection<out Group>) {
        super.addAll(groups)
        groups.forEach {
            if (it is QuestionnaireItem) {
                it.getViewStateLiveData().observe(lifecycleOwner, Observer { state ->
                    if (state.isCompleted) {
                        Timber.d("Received isCompleted = true from $it")
                        checkReadiness()
                    }
                })
            }
        }
    }

    private fun checkReadiness() {
        var totalReady = true
        for (i in 0 until (childCount + 1)) {
            val child = getGroup(i)
            if (child is QuestionnaireItem && (child.isRequired() && !child.isCompleted())) {
                totalReady = false
            }
        }
        headerItem.setSectionDone(totalReady)
    }


}