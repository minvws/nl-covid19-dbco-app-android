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
import com.xwray.groupie.viewbinding.GroupieViewHolder
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactInputBinding
import nl.rijksoverheid.dbco.items.BaseBindableItem
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.showKeyboard
import java.util.*

class ContactInputItem(
    private var focusOnBind: Boolean = false,
    private val contactNames: Array<String>,
    var contactName: String = "",
    var contactUuid: String? = null,
    private val contentDescriptionSuffix: String? = null,
    val trashListener: OnTrashClickedListener
) : BaseBindableItem<ItemContactInputBinding>(), TextWatcher {

    private val onClickListener = View.OnClickListener {
        trashListener.onTrashClicked(this@ContactInputItem)
    }

    override fun bind(viewBinding: ItemContactInputBinding, position: Int) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            viewBinding.contactInput.context,
            android.R.layout.simple_dropdown_item_1line, contactNames
        )
        with(viewBinding) {
            contactInput.setText(contactName)
            contactInput.setAdapter(adapter)
            contactInput.contentDescription = contentDescriptionSuffix?.let {
                "${contactInput.contentDescription} $contentDescriptionSuffix"
            } ?: contactInput.contentDescription
            contactInput.addTextChangedListener(this@ContactInputItem)
            iconTrash.setOnClickListener(onClickListener)
            contactInput.setOnItemClickListener { _, _, _, _ ->
                viewBinding.contactInput.clearFocus()
                viewBinding.contactInput.hideKeyboard()
            }
        }
    }

    override fun getLayout(): Int = R.layout.item_contact_input

    override fun onViewAttachedToWindow(viewHolder: GroupieViewHolder<ItemContactInputBinding>) {
        super.onViewAttachedToWindow(viewHolder)
        if (focusOnBind) {
            viewHolder.binding.contactInput.requestFocus()
            viewHolder.binding.contactInput.showKeyboard()
            focusOnBind = false
        }
    }

    override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder<ItemContactInputBinding>) {
        super.onViewDetachedFromWindow(viewHolder)
        viewHolder.binding.contactInput.removeTextChangedListener(this)
    }

    override fun isClickable(): Boolean = true

    interface OnTrashClickedListener {
        fun onTrashClicked(item: ContactInputItem)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* */
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* */
    }

    override fun afterTextChanged(s: Editable?) {
        contactName = s.toString()
    }
}