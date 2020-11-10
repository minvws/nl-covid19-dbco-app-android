package nl.rijksoverheid.dbco.applifecycle.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
	val androidMinimumVersionMessage: String? = null,
	val iosMinimumVersion: String? = null,
	val iosMinimumVersionMessage: String? = null,
	val androidMinimumVersion: Int = 0,
	val iosAppStoreURL: String? = null
)
