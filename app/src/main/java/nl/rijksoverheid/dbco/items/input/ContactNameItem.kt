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
import com.google.android.material.textfield.TextInputLayout
import kotlinx.serialization.json.JsonElement
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.ItemContactNameBinding
import nl.rijksoverheid.dbco.questionnaire.data.entity.Question
import nl.rijksoverheid.dbco.util.setError
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import kotlin.collections.HashMap
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Warning
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Error
import nl.rijksoverheid.dbco.items.input.InputValidationResult.Valid

class ContactNameItem(
    private var firstName: String?,
    private var lastName: String?,
    question: Question?,
    private val isEnabled: Boolean,
    private val canShowEmptyWarning: Boolean = false,
    private val canShowFakeNameWarning: Boolean = false,
    private val changeListener: (String?, String?) -> Unit
) : BaseQuestionItem<ItemContactNameBinding>(question) {

    override fun getLayout() = R.layout.item_contact_name

    private var binding: ItemContactNameBinding? = null

    override fun bind(viewBinding: ItemContactNameBinding, position: Int) {
        this.binding = viewBinding

        val validator = NameValidator(
            context = viewBinding.firstName.context,
            canShowEmptyWarning = canShowEmptyWarning,
            canShowFakeNameWarning = canShowFakeNameWarning
        )

        viewBinding.firstName.editText?.setText(firstName)
        viewBinding.lastName.editText?.setText(lastName)
        validateFirstName(viewBinding, validator)
        validateLastName(viewBinding, validator)

        viewBinding.firstName.editText?.doAfterTextChanged {
            firstName = it.toString()
            changeListener.invoke(firstName, lastName)
        }
        viewBinding.firstName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateFirstName(viewBinding, validator)
            } else {
                viewBinding.firstName.error = null
            }
        }

        viewBinding.lastName.editText?.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateLastName(viewBinding, validator)
            } else {
                viewBinding.lastName.error = null
            }
        }

        viewBinding.lastName.editText?.doAfterTextChanged {
            lastName = it.toString()
            changeListener.invoke(firstName, lastName)
        }

        viewBinding.firstName.isEnabled = isEnabled
        viewBinding.lastName.isEnabled = isEnabled
    }

    override fun getUserAnswers(): Map<String, JsonElement> {
        val answers = HashMap<String, JsonElement>()
        answers["firstName"] = (firstName ?: "").toJsonPrimitive()
        answers["lastName"] = (lastName ?: "").toJsonPrimitive()
        return answers
    }

    private fun validateFirstName(viewBinding: ItemContactNameBinding, validator: NameValidator) {
        validate(viewBinding.firstName, viewBinding.firstName.editText?.text.toString(), validator)
    }

    private fun validateLastName(viewBinding: ItemContactNameBinding, validator: NameValidator) {
        validate(viewBinding.lastName, viewBinding.lastName.editText?.text.toString(), validator)
    }

    private fun validate(
        layout: TextInputLayout,
        input: String?,
        validator: NameValidator
    ) {
        when (val result = validator.validate(input)) {
            is Warning -> {
                layout.setError(R.drawable.ic_warning_24, result.warningRes, R.color.purple)
            }
            is Error -> layout.setError(R.drawable.ic_error_24, result.errorRes, R.color.red)
            else -> {
                layout.error = null
            }
        }
    }

    internal class NameValidator(
        context: Context,
        private val canShowFakeNameWarning: Boolean,
        private val canShowEmptyWarning: Boolean,
    ) : InputItemValidator {

        private val invalidNames: List<String> = context
            .resources
            .openRawResource(R.raw.invalid_names)
            .bufferedReader()
            .use { it.readLines() }
            .map { it.lowercase() }

        private val invalidSuffixes: List<String> = context
            .resources
            .openRawResource(R.raw.invalid_name_suffixes)
            .bufferedReader()
            .use { it.readLines() }
            .map { it.lowercase() }

        private val vowels: List<Char> = "aeiouy".toList()

        private val consonants: List<Char> = ('a'..'z').toList() - vowels

        private val validCharacters: List<Char> = ('a'..'z').toList() + "-'’".toList()

        override fun validate(input: String?): InputValidationResult {
            return if (input.isNullOrEmpty()) {
                if (canShowEmptyWarning) {
                    Warning(warningRes = R.string.warning_necessary_short)
                } else {
                    Valid(isComplete = false)
                }
            } else {
                if (canShowFakeNameWarning) {
                    val onlyConsonants = input
                        .lowercase()
                        .toList()
                        .none { !consonants.contains(it) }

                    val onlyVowels = input
                        .lowercase()
                        .toList()
                        .none { !vowels.contains(it) }

                    val containsInvalidName = invalidNames
                        .any { it in input.lowercase() }

                    val endsWithInvalidSuffix = invalidSuffixes
                        .any { input.lowercase().endsWith(it) }

                    val containsInvalidCharacters = input
                        .lowercase()
                        .any { !validCharacters.contains(it) }

                    val conditions = mutableListOf(
                        onlyConsonants,
                        onlyVowels,
                        containsInvalidName,
                        endsWithInvalidSuffix,
                        containsInvalidCharacters
                    )

                    if (conditions.any { it }) {
                        Warning(warningRes = R.string.warning_name)
                    } else {
                        Valid(isComplete = false)
                    }
                } else {
                    Valid(isComplete = false)
                }
            }
        }
    }
}