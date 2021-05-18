package nl.rijksoverheid.dbco.splash

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.config.*
import nl.rijksoverheid.dbco.onboarding.SplashViewModel
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.utils.createAppConfig
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SplashViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `given the app was last edited longer than 2 weeks ago, when config is loaded, then wipe storage and reset config and navigate to deletion`() {
        // given
        val config = createAppConfig()
        val mockStorage = mockk<SharedPreferences>()
        val mockUser = mockk<IUserRepository>()
        val mockCase = mockk<ICaseRepository>()
        val mockConfig = mockk<AppConfigRepository>()
        val lastEdited = LocalDateTime.now(DateTimeZone.UTC).minusDays(15)
        every { mockCase.getLastEdited() } returns lastEdited
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true
        every { mockStorage.getBoolean(Constants.USER_GAVE_CONSENT, false) } returns true
        every { mockStorage.edit().clear().commit() } returns true
        every { mockConfig.storeConfig(config) } just Runs
        every { mockUser.getToken() } returns null

        // when
        val vm = createViewModel(mockStorage, mockUser, mockCase, mockConfig)
        vm.onConfigLoaded(config)

        // then
        verify { mockStorage.edit().clear().commit() }
        verify { mockConfig.storeConfig(config) }
        Assert.assertEquals(vm.navigation.value, SplashViewModel.Navigation.DataDeletion)
    }

    @Test
    fun `given the app was last edited earlier than 2 weeks ago and onboarding is completed, when config is loaded, then open tasks`() {
        // given
        val config = createAppConfig()
        val mockStorage = mockk<SharedPreferences>()
        val mockUser = mockk<IUserRepository>()
        val mockCase = mockk<ICaseRepository>()
        val mockConfig = mockk<AppConfigRepository>()
        val lastEdited = LocalDateTime.now(DateTimeZone.UTC).minusDays(13)
        every { mockCase.getLastEdited() } returns lastEdited
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns true
        every { mockStorage.getBoolean(Constants.USER_GAVE_CONSENT, false) } returns true
        every { mockUser.getToken() } returns null

        // when
        val vm = createViewModel(mockStorage, mockUser, mockCase, mockConfig)
        vm.onConfigLoaded(config)

        // then
        verify(exactly = 0) { mockStorage.edit().clear().commit() }
        Assert.assertEquals(vm.navigation.value, SplashViewModel.Navigation.MyTasks)
    }

    @Test
    fun `given the app was last edited earlier than 2 weeks ago and onboarding not completed and user is paired and did not gave consent, when config is loaded, then open consent`() {
        // given
        val config = createAppConfig()
        val mockStorage = mockk<SharedPreferences>()
        val mockUser = mockk<IUserRepository>()
        val mockCase = mockk<ICaseRepository>()
        val mockConfig = mockk<AppConfigRepository>()
        val lastEdited = LocalDateTime.now(DateTimeZone.UTC).minusDays(13)
        every { mockCase.getLastEdited() } returns lastEdited
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false
        every { mockStorage.getBoolean(Constants.USER_GAVE_CONSENT, false) } returns false
        every { mockUser.getToken() } returns "token"

        // when
        val vm = createViewModel(mockStorage, mockUser, mockCase, mockConfig)
        vm.onConfigLoaded(config)

        // then
        verify(exactly = 0) { mockStorage.edit().clear().commit() }
        Assert.assertEquals(vm.navigation.value, SplashViewModel.Navigation.Consent)
    }

    @Test
    fun `given the app was last edited earlier than 2 weeks ago and onboarding not completed and user is paired and did gave consent, when config is loaded, then open tasks`() {
        // given
        val config = createAppConfig()
        val mockStorage = mockk<SharedPreferences>()
        val mockUser = mockk<IUserRepository>()
        val mockCase = mockk<ICaseRepository>()
        val mockConfig = mockk<AppConfigRepository>()
        val lastEdited = LocalDateTime.now(DateTimeZone.UTC).minusDays(13)
        every { mockCase.getLastEdited() } returns lastEdited
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false
        every { mockStorage.getBoolean(Constants.USER_GAVE_CONSENT, false) } returns true
        every { mockUser.getToken() } returns "token"

        // when
        val vm = createViewModel(mockStorage, mockUser, mockCase, mockConfig)
        vm.onConfigLoaded(config)

        // then
        verify(exactly = 0) { mockStorage.edit().clear().commit() }
        Assert.assertEquals(vm.navigation.value, SplashViewModel.Navigation.MyTasks)
    }

    @Test
    fun `when user is not paired, when config is loaded, then open start`() {
        // given
        val config = createAppConfig()
        val mockStorage = mockk<SharedPreferences>()
        val mockUser = mockk<IUserRepository>()
        val mockCase = mockk<ICaseRepository>()
        val mockConfig = mockk<AppConfigRepository>()
        val lastEdited = LocalDateTime.now(DateTimeZone.UTC).minusDays(13)
        every { mockCase.getLastEdited() } returns lastEdited
        every { mockStorage.getBoolean(Constants.USER_COMPLETED_ONBOARDING, false) } returns false
        every { mockStorage.getBoolean(Constants.USER_GAVE_CONSENT, false) } returns true
        every { mockUser.getToken() } returns null

        // when
        val vm = createViewModel(mockStorage, mockUser, mockCase, mockConfig)
        vm.onConfigLoaded(config)

        // then
        verify(exactly = 0) { mockStorage.edit().clear().commit() }
        Assert.assertEquals(vm.navigation.value, SplashViewModel.Navigation.Start)
    }

    private fun createViewModel(
        storage: SharedPreferences,
        userRepository: IUserRepository,
        caseRepository: ICaseRepository,
        appConfigRepository: AppConfigRepository
    ) = SplashViewModel(
        storage,
        userRepository,
        caseRepository,
        appConfigRepository
    )
}