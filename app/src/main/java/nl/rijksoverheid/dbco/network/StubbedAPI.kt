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
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.applifecycle.config.AppConfig
import nl.rijksoverheid.dbco.contacts.data.entity.CaseResponse
import nl.rijksoverheid.dbco.contacts.data.entity.QuestionnairyResponse
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import nl.rijksoverheid.dbco.user.data.entity.UploadCaseBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

interface StubbedAPI {

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


    companion object {
        fun create(
            context: Context,
            client: OkHttpClient = createOkHttpClient(context),
            baseUrl: String = BuildConfig.BASE_API_URL
        ): StubbedAPI {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(Json {ignoreUnknownKeys = true}.asConverterFactory(contentType))
                .baseUrl(baseUrl)
                .build().create(StubbedAPI::class.java)
        }
    }

}