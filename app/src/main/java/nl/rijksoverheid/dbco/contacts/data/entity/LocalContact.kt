/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.Constants
import java.util.*

typealias JavaSerializable = java.io.Serializable

/**
 * Contact attached to a [Task]
 */
@Serializable
@Keep
data class LocalContact(
    val id: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var numbers: Set<String> = emptySet(),
    var emails: Set<String> = emptySet(),
) : JavaSerializable {

    fun hasValidEmailOrPhone(): Boolean {
        return hasValidPhoneNumber() || hasValidEmailAddress()
    }

    fun hasValidPhoneNumber() : Boolean {
        return numbers.count { Constants.PHONE_VALIDATION_MATCHER.matcher(it).matches() } > 0
    }

    fun hasSingleValidPhoneNumber() : Boolean {
        return numbers.count { Constants.PHONE_VALIDATION_MATCHER.matcher(it).matches() } == 1
    }

    fun hasValidEmailAddress() : Boolean {
        return emails.count { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() } > 0
    }

    fun getDisplayName(): String {
        var result = ""
        firstName?.let {
            result += it
        }
        lastName?.let {
            if (result.isNotEmpty()) {
                result += " "
            }
            result += it
        }
        return result
    }

    companion object {
        fun fromLabel(label: String?, id: String? = null): LocalContact {
            val localContact = LocalContact(id ?: UUID.randomUUID().toString())
            label?.let {
                val nameParts = it.split(" ", limit = 2)
                localContact.firstName = nameParts[0]
                if (nameParts.size > 1) {
                    localContact.lastName = nameParts[1]
                }
            }
            return localContact
        }
    }
}