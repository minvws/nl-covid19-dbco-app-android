/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.core.widget.doAfterTextChanged
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.toJsonPrimitive

class ContactNameItem(
    private var firstName: String?,
    private var lastName: String?,
    question: Question?,
    private val changeListener: (String?, String?) -> Unit
) : BaseQuestionItem<ItemContactNameBinding>(question) {

    override fun getLayout() = R.layout.item_contact_name

    private var binding: ItemContactNameBinding? = null

    override fun bind(viewBinding: ItemContactNameBinding, position: Int) {
        this.binding = viewBinding

        viewBinding.firstName.editText?.setText(firstName)
        viewBinding.lastName.editText?.setText(lastName)

        viewBinding.firstName.editText?.doAfterTextChanged {
            firstName = it.toString()
            changeListener.invoke(firstName, lastName)
        }

        viewBinding.lastName.editText?.doAfterTextChanged {
            lastName = it.toString()
            changeListener.invoke(firstName, lastName)
        }
    }

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        answers["firstName"] = (firstName ?: "").toJsonPrimitive()
        answers["lastName"] = (lastName ?: "").toJsonPrimitive()
        return answers
    }
}