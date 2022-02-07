/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.items.input

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemInputWithOptionsBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.items.ui.BottomSheetDialogPicker
import nl.rijksoverheid.dbco.util.setCompleted
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import nl.rijksoverheid.dbco.items.input.InputQuestionMultipleOptionsItem.ViewState.MULTIPLE_OPTIONS
import nl.rijksoverheid.dbco.items.input.InputQuestionMultipleOptionsItem.ViewState.SINGLE_EDIT
import nl.rijksoverheid.dbco.util.setError
import nl.rijksoverheid.dbco.util.showKeyboard
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Warning
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Error
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Valid

abstract class InputQuestionMultipleOptionsItem(
    question: Question?,
    private var items: Set<String>,
    private val validator: InputItemValidator,
    private val changeListener: (Set<String>) -> Unit,
    private val key: String,
    private val type: Int,
    @StringRes private val singleHint: Int,
    @StringRes private val multipleHint: Int,
    private val isEnabled: Boolean,
) : BaseQuestionItem<ItemInputWithOptionsBinding>(question), TextWatcher {

    private val hasMultipleItems: Boolean
        get() = items.size > 1

    private var state: ViewState = if (hasMultipleItems) MULTIPLE_OPTIONS else SINGLE_EDIT

    override fun getLayout(): Int = R.layout.item_input_with_options

    override fun bind(viewBinding: ItemInputWithOptionsBinding, position: Int) {
        val editText = viewBinding.requireEditText()
        with(editText) {
            inputType = type
            imeOptions = EditorInfo.IME_ACTION_DONE
            addTextChangedListener(this@InputQuestionMultipleOptionsItem)
        }
        initInput(viewBinding = viewBinding)
    }

    override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder<ItemInputWithOptionsBinding>) {
        super.onViewDetachedFromWindow(viewHolder)
        viewHolder.binding.requireEditText().removeTextChangedListener(this)
    }

    private fun initInput(viewBinding: ItemInputWithOptionsBinding, forceFocus: Boolean = false) {
        val editText = viewBinding.requireEditText()
        val layout = viewBinding.inputField
        if (state == MULTIPLE_OPTIONS) {
            editText.clearFocus()
            layout.hint = viewBinding.requireContext().getString(multipleHint)
            layout.defaultHintTextColor = ContextCompat.getColorStateList(viewBinding.requireContext(), R.color.black)
            layout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            layout.endIconDrawable = ContextCompat.getDrawable(viewBinding.requireContext(), R.drawable.ic_chevron_down)
            editText.disableInput { showOptionsPicker(viewBinding, layout.context) }
        } else {
            layout.hint = viewBinding.requireContext().getString(singleHint)
            layout.defaultHintTextColor = ContextCompat.getColorStateList(viewBinding.requireContext(), R.color.secondary_text)
            layout.endIconMode = TextInputLayout.END_ICON_NONE
            layout.endIconDrawable = null
            editText.setText(items.firstOrNull())
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    checkCompleted(viewBinding)
                } else {
                    layout.error = null
                }
            }
            editText.enableInput()
        }

        if (state == SINGLE_EDIT) {
            checkCompleted(viewBinding)
        }
        if (forceFocus && isEnabled) {
            editText.postDelayed({
                editText.requestFocus()
                editText.showKeyboard()
            }, 100)
        }
        layout.isEnabled = isEnabled
    }

    private fun showOptionsPicker(viewBinding: ItemInputWithOptionsBinding, context: Context) {
        val dialog = BottomSheetDialogPicker(context, items) { value ->
            viewBinding.requireEditText().setText(value)
            state = SINGLE_EDIT
            initInput(viewBinding = viewBinding, forceFocus = value.isEmpty())
        }
        dialog.show()
    }

    private fun checkCompleted(viewBinding: ItemInputWithOptionsBinding) {
        val input = viewBinding.requireEditText().text.toString()
        val result = validator.validate(input)
        when (result) {
            is Error -> {
                viewBinding.inputField.setError(R.drawable.ic_error_24, result.errorRes, R.color.red)
            }
            is Warning -> {
                viewBinding.inputField.setError(
                    R.drawable.ic_warning_24,
                    result.warningRes,
                    R.color.purple
                )
            }
            else -> {
                viewBinding.inputField.error = null
            }
        }
        viewBinding.inputField.setCompleted(
            result is Valid && result.isComplete
        )
    }

    private fun ItemInputWithOptionsBinding.requireEditText(): EditText = this.inputField.editText!!

    private fun ItemInputWithOptionsBinding.requireContext(): Context = this.inputField.context!!

    private fun EditText.disableInput(onClick: View.OnClickListener) {
        isFocusableInTouchMode = false
        isLongClickable = false
        isCursorVisible = false
        setOnClickListener(onClick)
    }

    private fun EditText.enableInput() {
        isFocusableInTouchMode = true
        isLongClickable = true
        isCursorVisible = true
        setOnClickListener(null)
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is InputQuestionMultipleOptionsItem && other.items == items

    override fun hasSameContentAs(other: Item<*>) =
        other is InputQuestionMultipleOptionsItem && other.items == items

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        val item = if (items.size == 1) items.first() else ""
        answers[key] = item.toJsonPrimitive()
        return answers
    }

    internal enum class ViewState {
        SINGLE_EDIT,
        MULTIPLE_OPTIONS
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* */ }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* */ }

    override fun afterTextChanged(text: Editable?) {
        items = setOf(text.toString()).also { changeListener(it) }
    }
}