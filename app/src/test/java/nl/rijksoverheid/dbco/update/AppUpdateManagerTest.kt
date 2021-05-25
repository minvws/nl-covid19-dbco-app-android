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
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.utils.createAppConfig
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppUpdateManagerTest {

    @Test
    fun `given a min version that is higher than the current version, when update state is fetched, then the state should require an update`() {
        // given
        val packageName = "test"
        val minVersion = 10
        val config = createAppConfig(androidMinimumVersionCode = minVersion)
        val mockContext = mockk<Context>()
        val currentVersionCode = 9
        every { mockContext.packageName } returns packageName
        every { mockContext.packageManager.getInstallerPackageName(packageName) } returns packageName

        // when
        val manager = createManager(mockContext, currentVersionCode)

        // then
        Assert.assertEquals(manager.getUpdateState(config), AppUpdateManager.UpdateState.UpdateRequired(packageName))
    }

    @Test
    fun `given a min version that is the same than the current version, when update state is fetched, then the state should be up to date`() {
        // given
        val minVersion = 10
        val config = createAppConfig(androidMinimumVersionCode = minVersion)
        val mockContext = mockk<Context>()
        val currentVersionCode = 10

        // when
        val manager = createManager(mockContext, currentVersionCode)

        // then
        Assert.assertEquals(manager.getUpdateState(config), AppUpdateManager.UpdateState.UpToDate)
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
        Assert.assertEquals(manager.getUpdateState(config), AppUpdateManager.UpdateState.UpToDate)
    }

    private fun createManager(
        context: Context,
        currentVersionCode: Int
    ) = AppUpdateManager(
        context,
        currentVersionCode
    )
}