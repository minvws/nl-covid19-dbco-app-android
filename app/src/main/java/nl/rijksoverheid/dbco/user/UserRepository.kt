/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import android.content.Context
import android.util.Base64
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import nl.rijksoverheid.dbco.util.Obfuscator
import org.libsodium.jni.Sodium
import org.libsodium.jni.SodiumConstants
import org.libsodium.jni.crypto.Random
import org.libsodium.jni.keys.KeyPair

/**
 * Documentation: https://github.com/minvws/nl-covid19-dbco-app-coordination-private/blob/main/architecture/api/apispec-app.yaml
 */
class UserRepository(context: Context) : UserInterface { // TODO move to dagger

    private val BASE64_FLAGS: Int = Base64.NO_PADDING or Base64.NO_WRAP

    private val api: StubbedAPI = StubbedAPI.create(context)

    override suspend fun pair(pincode: String): PairingResponse {

        val seed = Random().randomBytes(SodiumConstants.SECRETKEY_BYTES)
        val encryptionKeyPair = KeyPair(seed)
        val clientPublicKeyBytes = encryptionKeyPair.publicKey.toBytes()

        val ggdPubKey = Obfuscator.deObfuscate(BuildConfig.GGD_PUBLIC_KEY)
        val ggdPubKeyBytes = Base64.decode(ggdPubKey, BASE64_FLAGS)

        val cipherTextLength = 48  + clientPublicKeyBytes.size
        val sealedClientPublicKeyBytes = ByteArray(cipherTextLength)
        Sodium.crypto_box_seal(sealedClientPublicKeyBytes, clientPublicKeyBytes, clientPublicKeyBytes.size, ggdPubKeyBytes)
        val sealedClientPublicKey = Base64.encodeToString(sealedClientPublicKeyBytes, BASE64_FLAGS)

        // get response from GGD server
        val pairingBody = PairingRequestBody(pincode, sealedClientPublicKey)
        val pairingResponse = api.pair(pairingBody)


        val sealedHealthAuthorityPublicKey = pairingResponse.sealedHealthAuthorityPublicKey

        return pairingResponse
    }
}