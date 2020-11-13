/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.user.UserInterface.Companion.BASE64_FLAGS
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_CLIENT_SECRET_KEY
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_RX
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_TOKEN
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_TX
import nl.rijksoverheid.dbco.user.UserInterface.Companion.HEX_ARRAY
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.util.Obfuscator
import nl.rijksoverheid.dbco.util.toHexString
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import org.libsodium.jni.crypto.Util
import timber.log.Timber

/**
 * HA stands for Health Authority (in our case GGD)
 * Documentation: https://github.com/minvws/nl-covid19-dbco-app-coordination-private/blob/main/architecture/api/apispec-app.yaml
 */
class UserRepository(context: Context) : UserInterface { // TODO move to dagger

    private var encryptedSharedPreferences: SharedPreferences = LocalStorageRepository.getInstance(context).getSharedPreferences()

    private val api: StubbedAPI = StubbedAPI.create(context)

    private var rx: String? = null
    private var tx: String? = null
    private var token: String? = null

    init {
        encryptedSharedPreferences.getString(KEY_RX, null)?.let {
            rx = it
        }
        encryptedSharedPreferences.getString(KEY_TOKEN, null)?.let {
            token = it
        }
    }

    @ExperimentalUnsignedTypes
    @SuppressLint("ApplySharedPref")
    override suspend fun pair(pincode: String) {

        val clientSecretKeyBytes = ByteArray(Sodium.crypto_box_secretkeybytes())
        val clientPublicKeyBytes = ByteArray(Sodium.crypto_box_publickeybytes())
        Sodium.crypto_box_keypair(clientPublicKeyBytes, clientSecretKeyBytes)

        val haPubKey = Obfuscator.deObfuscate(BuildConfig.GGD_PUBLIC_KEY)
        val haPubKeyBytes = Base64.decode(haPubKey, BASE64_FLAGS)

        val cipherTextLength = 48  + clientPublicKeyBytes.size
        val sealedClientPublicKeyBytes = ByteArray(cipherTextLength)
        Sodium.crypto_box_seal(
            sealedClientPublicKeyBytes,
            clientPublicKeyBytes,
            clientPublicKeyBytes.size,
            haPubKeyBytes
        )
        val sealedClientPublicKey = Base64.encodeToString(sealedClientPublicKeyBytes, BASE64_FLAGS)

        // get response from HA server
        val pairingBody = PairingRequestBody(pincode, sealedClientPublicKey)
        val pairingResponse = api.pair(pairingBody)

        val sealedHaPublicKeyBytes = Base64.decode(
            pairingResponse.sealedHealthAuthorityPublicKey,
            BASE64_FLAGS
        )
        val haSpecificPublicKeyBytes = Util.zeros(SodiumConstants.ZERO_BYTES)

        val cryptoBoxSealOpenResult = Sodium.crypto_box_seal_open(
            haSpecificPublicKeyBytes,
            sealedHaPublicKeyBytes,
            sealedHaPublicKeyBytes.size,
            clientPublicKeyBytes,
            clientSecretKeyBytes
        )
        Timber.d("cryptoBoxSealOpenResult = $cryptoBoxSealOpenResult")

        val rxBytes = Util.zeros(SodiumConstants.SESSIONKEYBYTES)
        val txBytes = Util.zeros(SodiumConstants.SESSIONKEYBYTES)

        Sodium.crypto_kx_client_session_keys(
            rxBytes,
            txBytes,
            clientPublicKeyBytes,
            clientSecretKeyBytes,
            haSpecificPublicKeyBytes
        )

        val rxPlusTx = Util.merge(rxBytes, txBytes)

        val tokenBytes = Util.zeros(Sodium.crypto_generichash_bytes())

        Sodium.crypto_generichash(tokenBytes, tokenBytes.size, rxPlusTx, rxPlusTx.size, Util.zeros(0), 0)

        tx = Base64.encodeToString(txBytes, BASE64_FLAGS)
        rx = Base64.encodeToString(rxBytes, BASE64_FLAGS)
        token = tokenBytes.toHexString()

        val clienSecretKey = Base64.encodeToString(clientSecretKeyBytes, BASE64_FLAGS)
        encryptedSharedPreferences.edit()
            .putString(KEY_TX, tx)
            .putString(KEY_RX, rx)
            .putString(KEY_TOKEN, token)
            .putString(KEY_CLIENT_SECRET_KEY, clienSecretKey)
            .commit()
    }

    override fun getRx(): String? {
        return rx
    }

    override fun getToken(): String? {
        return token
    }
}