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
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.QuestionnaireItemViewState
import nl.rijksoverheid.dbco.questionnary.data.entity.Question

abstract class BaseQuestionItem<T : ViewDataBinding>(val question: Question? = null) : BaseBindableItem<T>() {

    val currentViewState: MutableLiveData<QuestionnaireItemViewState> = MutableLiveData()

    init {
        currentViewState.value = QuestionnaireItemViewState()
    }

    open fun getUserAnswers(): Map<String, Any> {
        return HashMap()
    }

    fun checkCompleted(isCompleted: Boolean? = null) {
        val finalCompleted = isCompleted ?: isCompleted()
        currentViewState.value?.let {
            currentViewState.value = it.copy(isCompleted = finalCompleted)
        }
    }

    abstract fun isRequired(): Boolean
    abstract fun isCompleted(): Boolean

}