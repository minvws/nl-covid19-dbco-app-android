package nl.rijksoverheid.dbco.contacts.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Task(
    val taskType: String? = null,
    val taskContext: String? = null,
    val source: String? = null,
    val label: String? = null,
    val category: String? = null,
    val communication: CommunicationType? = null,
    val uuid: String? = null,
    var linkedContact: LocalContact? = null
) : Parcelable

@Keep
enum class State { PRESENT, REMOVED }

@Serializable
@Keep
enum class CommunicationType {
    @SerialName("index")
    Index,

    @SerialName("staff")
    Staff,

    @SerialName("none")
    None
}

