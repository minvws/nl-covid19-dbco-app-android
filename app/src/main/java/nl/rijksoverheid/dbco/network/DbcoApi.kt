/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.applifecycle.config.AppConfig
import nl.rijksoverheid.dbco.contacts.data.entity.CaseResponse
import nl.rijksoverheid.dbco.contacts.data.entity.QuestionnairyResponse
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingResponse
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingStatusResponse
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DbcoApi {

    @GET("v1/questionnaires")
    @Streaming
    suspend fun getQuestionnaires(): QuestionnairyResponse

    @GET("v1/cases/{token}")
    @Streaming
    suspend fun getCase(@Path("token") token: String): Response<CaseResponse>

    @PUT("v1/cases/{token}")
    suspend fun uploadCase(@Path("token") token: String, @Body body: UploadCaseBody): Response<Unit>

    @POST("v1/pairings")
    suspend fun pair(@Body body: PairingRequestBody): PairingResponse

    @GET("v1/config")
    @Streaming
    suspend fun getAppConfig(): Response<AppConfig>

    @POST("v1/pairingrequests")
    suspend fun retrievePairingCode() : Response<ReversePairingResponse>

    @GET("v1/pairingrequests/{token}")
    suspend fun checkReversePairingStatus(@Path("token") token: String) : Response<ReversePairingStatusResponse>


    companion object {
        fun create(
            context: Context,
            client: OkHttpClient = createOkHttpClient(context),
            baseUrl: String = BuildConfig.BASE_API_URL
        ): DbcoApi {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(Defaults.json.asConverterFactory(contentType))
                .baseUrl(baseUrl)
                .build().create(DbcoApi::class.java)
        }
    }

}