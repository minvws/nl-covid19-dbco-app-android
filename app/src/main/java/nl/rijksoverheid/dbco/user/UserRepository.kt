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
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import nl.rijksoverheid.dbco.util.Obfuscator
import org.libsodium.jni.NaCl
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import org.libsodium.jni.crypto.Util

/**
 * HA stands for Health Authority (in our case GGD)
 * Documentation: https://github.com/minvws/nl-covid19-dbco-app-coordination-private/blob/main/architecture/api/apispec-app.yaml
 */
class UserRepository(context: Context) : UserInterface { // TODO move to dagger

    private var encryptedSharedPreferences: SharedPreferences = LocalStorageRepository.getInstance(context).getSharedPreferences()

    private val api: StubbedAPI = StubbedAPI.create(context)

    private var rx: String? = null
    private var token: String? = null

    init {
        encryptedSharedPreferences.getString(KEY_RX, null)?.let {
            rx = it
        }
        encryptedSharedPreferences.getString(KEY_TOKEN, null)?.let {
            token = it
        }
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun pair(pincode: String): PairingResponse {
        NaCl.sodium() // init

        val clientSecretKeyBytes = Util.zeros(SodiumConstants.SECRETKEY_BYTES)
        val clientPublicKeyBytes = Util.zeros(SodiumConstants.PUBLICKEY_BYTES)
        Sodium.crypto_kx_keypair(clientSecretKeyBytes, clientPublicKeyBytes)

        val haPubKey = Obfuscator.deObfuscate(BuildConfig.GGD_PUBLIC_KEY)
        val haPubKeyBytes = Base64.decode(haPubKey, BASE64_FLAGS)

        val cipherTextLength = 48  + clientPublicKeyBytes.size
        val sealedClientPublicKeyBytes = Util.zeros(cipherTextLength)
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

        val sealedHaPublicKeyBytes = Base64.decode(pairingResponse.sealedHealthAuthorityPublicKey, BASE64_FLAGS)
        val haSpecificPublicKeyBytes = Util.zeros(1000) // TODO measure
        Sodium.crypto_box_seal_open(
            haSpecificPublicKeyBytes,
            sealedHaPublicKeyBytes,
            sealedHaPublicKeyBytes.size,
            clientPublicKeyBytes,
            clientSecretKeyBytes
        )

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

        val token = Util.zeros(Sodium.crypto_generichash_bytes())

        Sodium.crypto_generichash(token, token.size, rxPlusTx, rxPlusTx.size, null, 0 )

        encryptedSharedPreferences.edit()
            .putString(KEY_TX, Base64.encodeToString(txBytes, BASE64_FLAGS))
            .putString(KEY_RX, Base64.encodeToString(rxBytes, BASE64_FLAGS))
            .putString(KEY_TOKEN, Base64.encodeToString(token, BASE64_FLAGS))
            .commit()

        return pairingResponse
    }

    override fun getRx(): String? {
        return rx
    }

    override fun getToken(): String? {
        return token
    }

    companion object {
        const val BASE64_FLAGS: Int = Base64.NO_WRAP

        const val KEY_TX = "KEY_TX"
        const val KEY_RX = "KEY_RX"
        const val KEY_TOKEN = "KEY_TOKEN"
    }
}