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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSingleInputBinding
import nl.rijksoverheid.dbco.questionnary.data.entity.Question

class SingleInputItem(val context: Context, question: Question) :
    BaseQuestionItem<ItemSingleInputBinding>(question) {

    private var input: String? = null
    override fun getLayout() = R.layout.item_single_input
    override fun isRequired() = true

    override fun bind(viewBinding: ItemSingleInputBinding, position: Int) {

        viewBinding.item = this
        input?.let {
            viewBinding.editText.setText(it)
        }
        viewBinding.editText.doAfterTextChanged {
            input = it.toString()
        }

        viewBinding.editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted()
            }
        }
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is SingleInputItem && other.question?.uuid == question?.uuid

    override fun hasSameContentAs(other: Item<*>) =
        other is SingleInputItem && other.input == input

    override fun isCompleted(): Boolean {
        return !input.isNullOrEmpty()
    }

    override fun getUserAnswers(): Map<String, Any> {
        val answers = HashMap<String, Any>()
        input?.let {
            answers.put("value", it)
        }
        return answers
    }
}