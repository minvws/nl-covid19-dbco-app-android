/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.update

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.utils.createAppConfig
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.NotSupported.*
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.UpToDate

@RunWith(MockitoJUnitRunner::class)
class AppUpdateManagerTest {

    @Test
    fun `given a min version that is higher than the current version, when update state is fetched, then the state should require an update`() {
        // given
        val minVersion = 10
        val updateTitle = "title"
        val minVersionMessage = "minVersionMessage"
        val updateAction = "action"
        val config = createAppConfig(
            androidMinimumVersionCode = minVersion,
            androidMinimumVersionMessage = minVersionMessage
        )
        val mockContext = mockk<Context>()
        val currentVersionCode = 9
        every { mockContext.getString(R.string.update_app_headline) } returns updateTitle
        every { mockContext.getString(R.string.update_app_action) } returns updateAction

        // when
        val manager = createManager(mockContext, currentVersionCode)

        // then
        Assert.assertEquals(
            manager.getAppLifecycleState(config),
            AppUpdateRequired(
                title = updateTitle,
                description = minVersionMessage,
                action = updateAction
            )
        )
    }

    @Test
    fun `given end of life, when update state is fetched, then the app should be end of life`() {
        // given
        val endOfLifeTitle = "title"
        val endOfLifeMessage = "minVersionMessage"
        val endOfLifeAction = "action"
        val endOfLifeActionUrl = "actionUrl"
        val config = createAppConfig(isEndOfLife = true)
        val mockContext = mockk<Context>()
        val currentVersionCode = 10

        every { mockContext.getString(R.string.end_of_life_headline) } returns endOfLifeTitle
        every { mockContext.getString(R.string.end_of_life_description) } returns endOfLifeMessage

        // when
        val manager = createManager(mockContext, currentVersionCode)

        // then
        Assert.assertEquals(
            manager.getAppLifecycleState(config), EndOfLife(
                title = endOfLifeTitle,
                description = endOfLifeMessage
            )
        )
    }

    @Test
    fun `given a min version that is higher than the current version, when update state is fetched, then the state should be up to date`() {
        // given
        val minVersion = 10
        val config = createAppConfig(androidMinimumVersionCode = minVersion)
        val mockContext = mockk<Context>()
        val currentVersionCode = 11

        // when
        val manager = createManager(mockContext, currentVersionCode)

        // then
        Assert.assertEquals(manager.getAppLifecycleState(config), UpToDate)
    }

    private fun createManager(
        context: Context,
        currentVersionCode: Int
    ) = AppUpdateManager(
        context,
        currentVersionCode
    )
}