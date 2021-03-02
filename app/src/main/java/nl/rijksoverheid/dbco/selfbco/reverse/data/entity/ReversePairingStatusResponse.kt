package nl.rijksoverheid.dbco.selfbco.reverse.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable


@Serializable
@Keep
data class ReversePairingStatusResponse(
	val refreshDelay: Int? = null,
	val expiresAt: String? = null,
	val status: ReversePairingState? = null,
	val pairingCode : String? = null,
	val pairingCodeExpiresAt : String? = null
)

