/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.debug.usertest

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.user.UserInterface
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import timber.log.Timber
import java.security.MessageDigest

class UsertestUserRepository(context: Context) : UserInterface { // TODO move to dagger

    private val api: StubbedAPI = StubbedAPI.create(context)
    private var userKey: String? = null

    private var mockedResponses: List<String> = listOf(
        "{\n" +
                "  \"caseId\": \"\",\n" +
                "  \"caseExpiresAt\": \"2020-12-11T00:00:00.000Z\",\n" +
                "  \"signingKey\": \"string\"\n" +
                "}", "{\n" +
                "  \"caseId\": \"123e4567-e89b-12d3-a456-789014172000\",\n" +
                "  \"caseExpiresAt\": \"2020-12-11T00:00:00.000Z\",\n" +
                "  \"signingKey\": \"string\"\n" +
                "}"
    )

    private val validCodesHashes: List<String> = listOf(
        "a6f3cb251db58a4cceaaec27090241216e69bdf8b28900dbcacb0cdf1f8c9813",
        "022819c49acf699b5030995565cdb86bb183e23ed155300980e86baada1b908a"
    )


    override suspend fun pair(pincode: String): PairingResponse {
        return getResponse(pincode)
    }

    private fun getResponse(pincode: String): PairingResponse {
        val hash = hash(pincode)
        Timber.d("hashed pincode to $hash")

        return if (validCodesHashes.contains(hash)) {
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString(mockedResponses[1])
        } else {
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString(mockedResponses[0])
        }

    }

    fun hash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}