/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.Constants
import java.util.*

@Keep
@Serializable
@Parcelize
data class LocalContact(
    val id: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var number: String? = null,
    var email: String? = null,
) : Parcelable {

    fun hasValidEmailOrPhone(): Boolean {
        return (!number.isNullOrEmpty() && Constants.PHONE_VALIDATION_MATCHER.matcher(number!!).matches()) || (!email.isNullOrEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email!!)
            .matches())
    }

    fun hasValidPhoneNumber() : Boolean {
        return (!number.isNullOrEmpty() && Constants.PHONE_VALIDATION_MATCHER.matcher(number!!).matches())
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