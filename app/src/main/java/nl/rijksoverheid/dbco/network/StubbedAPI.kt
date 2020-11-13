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
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse
import nl.rijksoverheid.dbco.user.data.entity.PairingRequestBody
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

interface StubbedAPI {

    @GET("v1/questionnaires")
    @Streaming
    suspend fun getQuestionnaires(): Response<ContactDetailsResponse>

    @GET("v1/cases/{token}")
    @Streaming
    suspend fun getTasks(@Path("token") token: String): Response<TasksResponse>

    @POST("/pairings")
    suspend fun pair(@Body body: PairingRequestBody): PairingResponse

    @GET("v1/config")
    @Streaming
    suspend fun getAppConfig(): Response<AppConfig>


    companion object {
        fun create(
            context: Context,
            client: OkHttpClient = createOkHttpClient(context),
            baseUrl: String = BuildConfig.STUBBED_API_URL
        ): StubbedAPI {
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .client(client)
                .addConverterFactory(Json.asConverterFactory(contentType))
                .baseUrl(baseUrl)
                .build().create(StubbedAPI::class.java)
        }
    }

}