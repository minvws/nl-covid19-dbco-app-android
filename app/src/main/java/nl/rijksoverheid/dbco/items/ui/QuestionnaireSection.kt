/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.ui

import com.xwray.groupie.ExpandableGroup

class QuestionnaireSection(
    private val headerItem: QuestionnaireSectionHeader,
    expandedByDefault: Boolean = false
) : ExpandableGroup(headerItem, expandedByDefault) {

    fun setCompleted(completed: Boolean) {
        headerItem.completed = completed
    }

    fun isCompleted() = headerItem.completed

    fun setEnabled(enabled: Boolean) {
        headerItem.enabled = enabled
    }

    fun isEnabled() = headerItem.enabled

    fun setBlocked(blocked: Boolean) {
        headerItem.blocked = blocked
    }

}