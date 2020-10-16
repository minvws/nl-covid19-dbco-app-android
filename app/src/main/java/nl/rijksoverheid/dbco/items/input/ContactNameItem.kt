/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.core.widget.doAfterTextChanged
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding

class ContactNameItem(private var firstName: String = "", private var lastName: String = "") :
    BaseQuestionItem<ItemContactNameBinding>() {

    override fun getLayout() = R.layout.item_contact_name

    override fun isRequired() = true

    override fun isCompleted(): Boolean {
        return (firstName.isNotEmpty() && lastName.isNotEmpty())
    }

    private var binding: ItemContactNameBinding? = null

    override fun bind(viewBinding: ItemContactNameBinding, position: Int) {
        this.binding = viewBinding
        viewBinding.firstName.editText?.setText(firstName)
        viewBinding.lastName.editText?.setText(lastName)

        viewBinding.firstName.editText?.doAfterTextChanged {
            firstName = it.toString()
        }

        viewBinding.lastName.editText?.doAfterTextChanged {
            lastName = it.toString()
        }

        viewBinding.firstName.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted()
            }
        }

        viewBinding.lastName.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkCompleted()
            }
        }
    }

    override fun getUserAnswers() : Map<String, Any> {
        val answers = HashMap<String, Any>()
        // TODO
        return answers
    }
}