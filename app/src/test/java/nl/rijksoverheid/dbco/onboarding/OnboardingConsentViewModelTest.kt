/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.utils.CoroutineTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OnboardingConsentViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `given onboarding is complete, then is paired should be true`() {
        // given
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns null
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)

        // then
        Assert.assertTrue(vm.isPaired)
    }

    @Test
    fun `given user has token, then is paired should be true`() {
        // given
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns "token"
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)

        // then
        Assert.assertTrue(vm.isPaired)
    }

    @Test
    fun `given user has token and onboarding is complete, then is paired should be true`() {
        // given
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns "token"
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)

        // then
        Assert.assertTrue(vm.isPaired)
    }

    @Test
    fun `given user has no token and onboarding is no complete, then is paired should be false`() {
        // given
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns null
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)

        // then
        Assert.assertFalse(vm.isPaired)
    }

    @Test
    fun `given a case with no tasks and a paired user and symptoms known, when consent is given, then consent should be stored and add contacts should be opened`() = runTest {
        // given
        val case = Case(tasks = emptyList(), symptomsKnown = true)
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns "token"
        every { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() } just Runs
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true
        every { mockCase.getCase() } returns case

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)
        vm.onNextClicked()

        // then
        verify { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() }
        Assert.assertEquals(vm.navigationFlow.first(), OnboardingConsentViewModel.Navigation.AddContacts)
    }

    @Test
    fun `given a case with no tasks and a paired user and no symptoms known, when consent is given, then consent should be stored and add symptoms should be opened`() = runTest {
        // given
        val case = Case(tasks = emptyList(), symptomsKnown = false)
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns "token"
        every { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() } just Runs
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true
        every { mockCase.getCase() } returns case

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)
        vm.onNextClicked()

        // then
        verify { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() }
        Assert.assertEquals(vm.navigationFlow.first(), OnboardingConsentViewModel.Navigation.Symptoms)
    }

    @Test
    fun `given a case with tasks and a paired user, when consent is given, then consent should be stored and tasks should be opened`() = runTest {
        // given
        val case = Case(tasks = listOf(Task()))
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns "token"
        every { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() } just Runs
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true
        every { mockCase.getCase() } returns case

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)
        vm.onNextClicked()

        // then
        verify { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() }
        Assert.assertEquals(vm.navigationFlow.first(), OnboardingConsentViewModel.Navigation.MyTasks)
    }

    @Test
    fun `given a case without tasks and no paired user, when consent is given, then consent should be stored and add symptoms should be opened`() = runTest {
        // given
        val case = Case(tasks = emptyList())
        val mockCase = mockk<ICaseRepository>()
        val mockUser = mockk<IUserRepository>()
        val mockStorage = mockk<SharedPreferences>()
        every { mockUser.getToken() } returns null
        every { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() } just Runs
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false
        every { mockCase.getCase() } returns case

        // when
        val vm = createViewModel(mockCase, mockUser, mockStorage)
        vm.onNextClicked()

        // then
        verify { mockStorage.edit().putBoolean(Constants.USER_GAVE_CONSENT, true).apply() }
        Assert.assertEquals(vm.navigationFlow.first(), OnboardingConsentViewModel.Navigation.Symptoms)
    }

    private fun createViewModel(
        caseRepository: ICaseRepository,
        userRepository: IUserRepository,
        storage: SharedPreferences
    ) = OnboardingConsentViewModel(
        caseRepository,
        userRepository,
        storage,
        coroutineTestRule.testDispatcherProvider
    )
}