package nl.rijksoverheid.dbco.selfbco.reverse.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ReversePairingResponse(
	val code: String? = null,
	val refreshDelay: Int? = null,
	val expiresAt: String? = null,
	val token: String? = null,
	val status: ReversePairingState? = null
)

