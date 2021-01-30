/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactInputBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem


class ContactInputItem(private val contactNames : Array<String>, var contactName: String = "", val trashListener: OnTrashClickedListener) : BaseBindableItem<ItemContactInputBinding>() {

    private var binding: ItemContactInputBinding? = null
    private val onClickListener = View.OnClickListener {
        trashListener.onTrashClicked(this@ContactInputItem)
    }
    override fun bind(viewBinding: ItemContactInputBinding, position: Int) {
        this.binding = viewBinding



        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            viewBinding.contactInput.context,
            android.R.layout.simple_dropdown_item_1line, contactNames
        )
        binding?.contactInput?.setAdapter(adapter)
        viewBinding.contactInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                contactName = s.toString()
            }
        })

        viewBinding.iconTrash.setOnClickListener(onClickListener)


    }

    override fun getLayout(): Int = R.layout.item_contact_input
    override fun isClickable(): Boolean = true


    interface OnTrashClickedListener {
        fun onTrashClicked(item: ContactInputItem)
    }
}