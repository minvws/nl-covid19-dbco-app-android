/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.databinding.ViewDataBinding
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question

abstract class BaseQuestionItem<T : ViewDataBinding>(val question: Question? = null) : BaseBindableItem<T>() {

    open fun getUserAnswers(): Map<String, JsonElement> {
        return HashMap()
    }

}