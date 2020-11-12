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

@Keep
@Serializable
@Parcelize
data class LocalContact(
    val id: String,
    val displayName: String,
    var number: String? = null,
    var email: String? = null,
    var name: ContactName = ContactName("", "")
) : Parcelable {
    fun hasEmailOrPhone(): Boolean {
        return !number.isNullOrEmpty() || !email.isNullOrEmpty()
    }
}