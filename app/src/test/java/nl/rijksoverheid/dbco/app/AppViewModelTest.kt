/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import nl.rijksoverheid.dbco.AppViewModel
import nl.rijksoverheid.dbco.config.*
import nl.rijksoverheid.dbco.utils.createAppConfig
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.IllegalStateException
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.NotSupported.*
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.UpToDate
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.ConfigError

@RunWith(MockitoJUnitRunner::class)
class AppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `given app needs updating, when config is fetched, then update event should be present`() {
        // given
        val config = createAppConfig()
        val title = "title"
        val description = "description"
        val action = "action"
        val updateRequired = AppUpdateRequired(
            title = title,
            description = description,
            action = action
        )
        val mockManager = mockk<AppUpdateManager>()
        val mockConfig = mockk<AppConfigRepository>()
        coEvery { mockConfig.getAppConfig() } returns config
        every { mockManager.getAppLifecycleState(config) } returns updateRequired

        // when
        val vm = createViewModel(mockManager, mockConfig)
        vm.fetchConfig()

        // then
        Assert.assertEquals(vm.appLifecycleState.value, updateRequired)
    }

    @Test
    fun `given app does not needs updating, when config is fetched, then post config and up to date event should be present`() {
        // given
        val config = createAppConfig()
        val upToDate = UpToDate
        val mockManager = mockk<AppUpdateManager>()
        val mockConfig = mockk<AppConfigRepository>()
        coEvery { mockConfig.getAppConfig() } returns config
        every { mockManager.getAppLifecycleState(config) } returns upToDate

        // when
        val vm = createViewModel(mockManager, mockConfig)
        vm.fetchConfig()

        // then
        Assert.assertEquals(vm.appLifecycleState.value, UpToDate)
    }

    @Test
    fun `given app config throws error, when config is fetched, then post config error event`() {
        // given
        val mockManager = mockk<AppUpdateManager>()
        val mockConfig = mockk<AppConfigRepository>()
        coEvery { mockConfig.getAppConfig() } throws IllegalStateException("test")

        // when
        val vm = createViewModel(mockManager, mockConfig)
        vm.fetchConfig()

        // then
        Assert.assertEquals(vm.appLifecycleState.value, ConfigError)
    }

    @Test
    fun `given app manager throws error, when config is fetched, then post config error event`() {
        // given
        val config = createAppConfig()
        val mockManager = mockk<AppUpdateManager>()
        val mockConfig = mockk<AppConfigRepository>()
        coEvery { mockConfig.getAppConfig() } returns config
        every { mockManager.getAppLifecycleState(config) } throws IllegalStateException("test")

        // when
        val vm = createViewModel(mockManager, mockConfig)
        vm.fetchConfig()

        // then
        Assert.assertEquals(vm.appLifecycleState.value, ConfigError)
    }

    private fun createViewModel(
        appUpdateManager: AppUpdateManager,
        appConfigRepository: AppConfigRepository
    ) = AppViewModel(
        appUpdateManager,
        appConfigRepository,
        TestCoroutineDispatcher()
    )
}