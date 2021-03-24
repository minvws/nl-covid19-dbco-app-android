/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.content.Context
import androidx.core.widget.doAfterTextChanged
import com.xwray.groupie.Item
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSingleInputBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive

class SingleInputItem(
    val context: Context,
    question: Question,
    private val previousAnswerValue: JsonObject? = null
) :
    BaseQuestionItem<ItemSingleInputBinding>(question) {

    private var input: String? = null
    override fun getLayout() = R.layout.item_single_input

    override fun bind(viewBinding: ItemSingleInputBinding, position: Int) {
        viewBinding.item = this

        if (input == null) {
            fillInPreviousAnswer()
        }

        input?.let {
            viewBinding.editText.setText(it)
        }
        viewBinding.editText.doAfterTextChanged {
            input = it.toString()
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is SingleInputItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is SingleInputItem && other.input == input

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        val input = input ?: ""
        answers["value"] = input.toJsonPrimitive()
        return answers
    }

    private fun fillInPreviousAnswer() {
        previousAnswerValue?.let { prevAnswer ->
            prevAnswer["value"]?.jsonPrimitive?.content?.let { value ->
                input = value
            }
        }
    }
}