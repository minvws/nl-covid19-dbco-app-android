/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.InputType
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import com.xwray.groupie.Item
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemSingleInputBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.items.ItemType
import nl.rijksoverheid.dbco.items.QuestionnaireItem
import nl.rijksoverheid.dbco.items.QuestionnaireItemViewState

class PhoneNumberItem(private var phoneNumber: String?) :
    BaseBindableItem<ItemSingleInputBinding>(), QuestionnaireItem {
    override fun getLayout() = R.layout.item_single_input
    override fun isRequired() = true
    override fun getItemType() = ItemType.INPUT_PHONE

    private val currentViewState: MutableLiveData<QuestionnaireItemViewState> = MutableLiveData()

    init {
        currentViewState.value = QuestionnaireItemViewState()
    }

    override fun bind(viewBinding: ItemSingleInputBinding, position: Int) {
        viewBinding.inputField.editText?.apply {
            inputType = InputType.TYPE_CLASS_PHONE
            setText(phoneNumber)
        }

        viewBinding.inputField.apply {
            this.hint = this.context.getString(R.string.hint_phone_number)
        }

        viewBinding.inputField.editText?.doAfterTextChanged {
            phoneNumber = it.toString()
        }

        viewBinding.inputField.editText?.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                currentViewState.value = currentViewState.value!!.copy(isCompleted = isCompleted())
            }
        }

    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun hasSameContentAs(other: Item<*>) =
        other is PhoneNumberItem && other.phoneNumber == phoneNumber

    override fun isCompleted(): Boolean {
        return !phoneNumber.isNullOrEmpty()
    }

    override fun getViewStateLiveData(): MutableLiveData<QuestionnaireItemViewState> {
        return currentViewState
    }
}