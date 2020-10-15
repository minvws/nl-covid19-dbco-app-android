/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import nl.rijksoverheid.dbco.contacts.data.entity.Question
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.QuestionnaireItemViewState

abstract class BaseQuestionItem<T : ViewDataBinding>(val question: Question? = null) : BaseBindableItem<T>() {

    val currentViewState: MutableLiveData<QuestionnaireItemViewState> = MutableLiveData()

    init {
        currentViewState.value = QuestionnaireItemViewState()
    }

    fun getUserAnswers(): Map<String, String> {
        return HashMap()
    }

    abstract fun isRequired(): Boolean
    abstract fun isCompleted(): Boolean

}