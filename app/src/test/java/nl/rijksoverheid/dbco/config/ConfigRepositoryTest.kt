/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.config

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.utils.createAppConfig
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.config.AppConfigRepository.Companion.CACHE_VALIDITY_DAYS
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import okhttp3.ResponseBody.Companion.toResponseBody
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.tz.UTCProvider
import java.lang.IllegalStateException

@RunWith(MockitoJUnitRunner::class)
class ConfigRepositoryTest {

    @Test
    fun `given api returns a valid config, when fetching config, then store config and return it`() =
        runBlockingTest {
            // given
            val config = createAppConfig()
            val mockContext = mockk<Context>()
            val mockApi = mockk<DbcoApi>()
            val mockStorage = mockk<SharedPreferences>()
            val configString = Defaults.json.encodeToString(config)
            coEvery { mockApi.getAppConfig() } returns Response.success(config)
            every {
                mockStorage.edit()
                    .putString(AppConfigRepository.KEY_CONFIG, configString)
                    .putString(AppConfigRepository.KEY_CONFIG_CACHE_DATE, any())
                    .apply()
            } just Runs

            // when
            val repo = createRepository(mockContext, mockApi, mockStorage)
            val result = repo.getAppConfig()

            // then
            verify {
                mockStorage.edit()
                    .putString(AppConfigRepository.KEY_CONFIG, configString)
                    .putString(AppConfigRepository.KEY_CONFIG_CACHE_DATE, any())
                    .apply()
            }
            Assert.assertEquals(config, result)
        }

    @Test
    fun `given api returns an error and a recent cached config exists, when fetching config, then return stored cache`() =
        runBlockingTest {
            // given
            mockkStatic(LocalDate::class)
            val cacheConfig = createAppConfig()
            val cacheConfigString = Defaults.json.encodeToString(cacheConfig)
            val cacheDate = LocalDate.now(DateTimeZone.UTC).minusDays(CACHE_VALIDITY_DAYS - 1)
            val cacheDateString = cacheDate.toString(DateFormats.dateInputData)
            val mockContext = mockk<Context>()
            val mockApi = mockk<DbcoApi>()
            val mockStorage = mockk<SharedPreferences>()
            coEvery { mockApi.getAppConfig() } returns Response.error(500, "test".toResponseBody())
            every {
                mockStorage.getString(
                    AppConfigRepository.KEY_CONFIG,
                    null
                )
            } returns cacheConfigString
            every {
                mockStorage.getString(
                    AppConfigRepository.KEY_CONFIG_CACHE_DATE,
                    null
                )
            } returns cacheDateString
            every { LocalDate.parse(cacheDateString, DateFormats.dateInputData) } returns cacheDate

            // when
            val repo = createRepository(mockContext, mockApi, mockStorage)
            val result = repo.getAppConfig()

            // then
            Assert.assertEquals(result, cacheConfig)
        }

    @Test(expected = IllegalStateException::class)
    fun `given api returns an error and no recent cached config exists, when fetching config, then error should be thrown`() =
        runBlockingTest {
            // given
            mockkStatic(LocalDate::class)
            val cacheConfig = createAppConfig()
            val cacheConfigString = Defaults.json.encodeToString(cacheConfig)
            val cacheDate = LocalDate.now(DateTimeZone.UTC).minusDays(CACHE_VALIDITY_DAYS + 1)
            val cacheDateString = cacheDate.toString(DateFormats.dateInputData)
            val mockContext = mockk<Context>()
            val mockApi = mockk<DbcoApi>()
            val mockStorage = mockk<SharedPreferences>()
            coEvery { mockApi.getAppConfig() } throws IllegalStateException("test")
            every {
                mockStorage.getString(
                    AppConfigRepository.KEY_CONFIG,
                    null
                )
            } returns cacheConfigString
            every {
                mockStorage.getString(
                    AppConfigRepository.KEY_CONFIG_CACHE_DATE,
                    null
                )
            } returns cacheDateString
            every { LocalDate.parse(cacheDateString, DateFormats.dateInputData) } returns cacheDate

            // when
            val repo = createRepository(mockContext, mockApi, mockStorage)
            repo.getAppConfig()
        }

    @Test
    fun `given a config, when config is stored, then it should be saved in storage`() {
        // given
        val config = createAppConfig()
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every {
            mockStorage.edit()
                .putString(AppConfigRepository.KEY_CONFIG, configString)
                .putString(AppConfigRepository.KEY_CONFIG_CACHE_DATE, any())
                .apply()
        } just Runs

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        repo.storeConfig(config)

        // then
        verify {
            mockStorage.edit()
                .putString(AppConfigRepository.KEY_CONFIG, configString)
                .putString(AppConfigRepository.KEY_CONFIG_CACHE_DATE, any())
                .apply()
        }
    }

    @Test
    fun `given a update message in config, when update message is fetched, then it should be that message`() {
        // given
        val message = "test"
        val fallback = "fallback"
        val config = createAppConfig(androidMinimumVersionMessage = message)
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every { mockContext.getString(R.string.update_app_description) } returns fallback
        every {
            mockStorage.getString(
                AppConfigRepository.KEY_CONFIG,
                null
            )
        } returns configString

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        val result = repo.getUpdateMessage()

        // then
        Assert.assertEquals(result, message)
    }

    @Test
    fun `given no update message in config and a fallback, when update message is fetched, then it should be that fallback`() {
        // given
        val message = null
        val fallback = "fallback"
        val config = createAppConfig(androidMinimumVersionMessage = message)
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every { mockContext.getString(R.string.update_app_description) } returns fallback
        every {
            mockStorage.getString(
                AppConfigRepository.KEY_CONFIG,
                null
            )
        } returns configString

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        val result = repo.getUpdateMessage()

        // then
        Assert.assertEquals(result, fallback)
    }

    @Test
    fun `given symptoms in config, when symptoms are fetched, then it should be that list`() {
        // given
        val symptoms = listOf(Symptom("test", "test"))
        val config = createAppConfig(symptoms = symptoms)
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every {
            mockStorage.getString(
                AppConfigRepository.KEY_CONFIG,
                null
            )
        } returns configString

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        val result = repo.getSymptoms()

        // then
        Assert.assertEquals(result, symptoms)
    }

    @Test
    fun `given feature flags in config, when feature flags are fetched, then it should be that value`() {
        // given
        val flags = FeatureFlags(
            enableContactCalling = true,
            enablePerspectiveCopy = false,
            enableSelfBCO = true
        )
        val config = createAppConfig(featureFlags = flags)
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every {
            mockStorage.getString(
                AppConfigRepository.KEY_CONFIG,
                null
            )
        } returns configString

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        val result = repo.getFeatureFlags()

        // then
        Assert.assertEquals(result, flags)
    }

    @Test
    fun `given guidelines in config, when guidelines are fetched, then it should be that value`() {
        // given
        val guidelines = createGuidelines(cat1 = "mcTest")
        val config = createAppConfig(guidelines = guidelines)
        val mockContext = mockk<Context>()
        val mockApi = mockk<DbcoApi>()
        val mockStorage = mockk<SharedPreferences>()
        val configString = Defaults.json.encodeToString(config)
        every {
            mockStorage.getString(
                AppConfigRepository.KEY_CONFIG,
                null
            )
        } returns configString

        // when
        val repo = createRepository(mockContext, mockApi, mockStorage)
        val result = repo.getGuidelines()

        // then
        Assert.assertEquals(result, guidelines)
    }

    private fun createGuidelines(cat1: String) = GuidelinesContainer(
        introExposureDateKnown = Guidelines(
            category1 = cat1,
            category2 = "test",
            category3 = "test"
        ),
        introExposureDateUnknown = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        ),
        guidelinesExposureDateKnown = RangedGuidelines(
            category1 = "test",
            category2 = RangedGuideline(
                withinRange = "test",
                outsideRange = "test"
            ),
            category3 = "test"
        ),
        guidelinesExposureDateUnknown = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        ),
        referenceNumberItem = "test",
        outro = GenericGuidelines(
            category1 = "test",
            category2 = "test",
            category3 = "test"
        )
    )

    @Test
    fun `given a date and string with dynamic blocks, then return the correct formatted dates`() {
        // given
        DateTimeZone.setProvider(UTCProvider())
        val localDate = LocalDate.parse("2021-07-15", DateFormats.dateInputData)
        val input = "en {ExposureDate+10} en {ExposureDate+2} en {ExposureDate-1} en {ExposureDate-10} en {ExposureDate}"
        val expected = "en 25 juli en 17 juli en 14 juli en 5 juli en 15 juli"

        // then
        Assert.assertEquals(expected, Guidelines.replaceExposureDateInstances(input, localDate))
    }

    private fun createRepository(context: Context, api: DbcoApi, storage: SharedPreferences) =
        AppConfigRepository(
            context,
            api,
            storage,
            TestCoroutineDispatcher()
        )
}