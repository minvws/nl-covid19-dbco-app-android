package nl.rijksoverheid.dbco.selfbco.reverse.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Serializable
@Keep
data class ReversePairingStatusResponse(
	val refreshDelay: Int? = null, // In seconds
	val expiresAt: String? = null, // ISO8601 date format
	val status: ReversePairingState? = null,
	val pairingCode : String? = null,
)

