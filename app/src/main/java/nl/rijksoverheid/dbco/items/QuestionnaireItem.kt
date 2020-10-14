/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData

interface QuestionnaireItem {
    fun isRequired(): Boolean
    fun getItemType(): ItemType
    fun isCompleted(): Boolean
    fun getViewStateLiveData(): MutableLiveData<QuestionnaireItemViewState>
}

@Keep
enum class ItemType { OUTPUT, INPUT_GENERAL, INPUT_SEARCH, INPUT_BUTTON, INPUT_NAME, INPUT_EMAIL, INPUT_PHONE, INPUT_COMMENT, INPUT_BIRTHDAY, INPUT_MULTIPLE_CHOICE }