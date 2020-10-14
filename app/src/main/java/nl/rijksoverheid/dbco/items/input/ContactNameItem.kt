/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.ContactName
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem
import nl.rijksoverheid.dbco.items.QuestionnaireItemViewState

class ContactNameItem(private var firstName: String = "", private var lastName: String = "") :
    BaseBindableItem<ItemContactNameBinding>(), QuestionnaireItem {
    override fun getLayout() = R.layout.item_contact_name


    override fun isRequired() = true
    override fun getItemType() = ItemType.INPUT_NAME

    private val currentViewState: MutableLiveData<QuestionnaireItemViewState> = MutableLiveData()

    init {
        currentViewState.value = QuestionnaireItemViewState()
    }


    private var binding: ItemContactNameBinding? = null;

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
                currentViewState.value = currentViewState().copy(isCompleted = isCompleted())
            }
        }

        viewBinding.lastName.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                currentViewState.value = currentViewState().copy(isCompleted = isCompleted())
            }
        }
    }

    private fun currentViewState(): QuestionnaireItemViewState = currentViewState.value!!

    fun getFirstNameAndLastName(): ContactName {
        return ContactName(
            firstName,
            lastName
        )
    }

    override fun isCompleted(): Boolean {
        return (firstName.isNotEmpty() && lastName.isNotEmpty())
    }

    override fun getViewStateLiveData(): MutableLiveData<QuestionnaireItemViewState> {
        return currentViewState
    }


}