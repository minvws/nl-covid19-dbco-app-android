/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.mockk.*
import kotlinx.coroutines.test.*
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.user.UserRepository
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingSuccess
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingError
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingStatus.PairingInvalid
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingCredentials
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingResponse
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.ReversePairing
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.ReversePairingExpired
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.ReversePairingSuccess
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.ReversePairingStopped
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingState
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingStatusResponse
import nl.rijksoverheid.dbco.utils.CoroutineTestRule
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.junit.*

@RunWith(MockitoJUnitRunner::class)
class PairingViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `given pairing throws no exception and case can be retrieved, when pairing is done with pin, pairing status should be success`() {
        runTest {
            // given
            val pin = "pin"
            val case = Case()
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingSuccess(case))
        }
    }

    @Test
    fun `given pairing throws regular exception and case can be retrieved, when pairing is done with pin, pairing status should be error`() {
        runTest {
            // given
            val pin = "pin"
            val error = IllegalStateException("test")
            val case = Case()
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { userMock.pair(pin) } throws error
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingError(error))
        }
    }

    @Test
    fun `given pairing throws http 400 exception and case can be retrieved, when pairing is done with pin, pairing status should be invalid`() {
        runTest {
            // given
            val pin = "pin"
            val response = mockk<Response<Any>>()
            every { response.code() } returns 400
            every { response.message() } returns "test"
            val error = HttpException(response)
            val case = Case()
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { userMock.pair(pin) } throws error
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingInvalid)
        }
    }

    @Test
    fun `given pairing throws http 500 exception and case can be retrieved, when pairing is done with pin, pairing status should be error`() {
        runTest {
            // given
            val pin = "pin"
            val response = mockk<Response<Any>>()
            every { response.code() } returns 500
            every { response.message() } returns "test"
            val error = HttpException(response)
            val case = Case()
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { userMock.pair(pin) } throws error
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingError(error))
        }
    }

    @Test
    fun `given pairing succeeds but case retrieval gives error, when pairing is done with pin, pairing status should be error`() {
        runTest {
            // given
            val pin = "pin"
            val error = IllegalStateException("test")
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { tasksMock.fetchCase() } throws error

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingError(error))
        }
    }

    @Test
    fun `given pairing succeeds but case retrieval gives http 400 error, when pairing is done with pin, pairing status should be invalid`() {
        runTest {
            // given
            val pin = "pin"
            val response = mockk<Response<Any>>()
            every { response.code() } returns 400
            every { response.message() } returns "test"
            val error = HttpException(response)
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { tasksMock.fetchCase() } throws error

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingInvalid)
        }
    }

    @Test
    fun `given pairing succeeds but case retrieval gives http 500 error, when pairing is done with pin, pairing status should be error`() {
        runTest {
            // given
            val pin = "pin"
            val response = mockk<Response<Any>>()
            every { response.code() } returns 500
            every { response.message() } returns "test"
            val error = HttpException(response)
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { tasksMock.fetchCase() } throws error

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.pair(pin)

            // then
            Assert.assertEquals(viewModel.pairingStatus.value, PairingError(error))
        }
    }

    @Test
    fun `given no pairing credentials and pairing process succeeds, when reverse pairing starts, then pairing code should have code and reverse pairing should be successful`() {
        // give
        runTest {
            val code = "testCode"
            val token = "testToken"
            val pairingCode = "pairingCode"
            val credentialsResponse = Response.success(
                ReversePairingResponse(
                    code = code,
                    token = token
                )
            )
            val reversePairingResponse = Response.success(
                ReversePairingStatusResponse(
                    status = ReversePairingState.COMPLETED,
                    pairingCode = pairingCode
                )
            )
            val credentials = ReversePairingCredentials(
                code = code,
                token = token
            )
            val case = Case()
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            coEvery { tasksMock.fetchCase() } returns case
            coEvery { userMock.retrieveReversePairingCode() } returns credentialsResponse
            coEvery { userMock.checkReversePairingStatus(credentials.token) } returns reversePairingResponse
            val observer =
                mockk<Observer<ReversePairingStatus>> { every { onChanged(any()) } just Runs }

            // when
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.reversePairingStatus.observeForever(observer)
            viewModel.startReversePairing(credentials = null)

            // then
            Assert.assertEquals(viewModel.pairingCode.value, code)
            verifySequence {
                observer.onChanged(ReversePairing(credentials))
                observer.onChanged(ReversePairingSuccess(pairingCode))
            }
        }
    }

    @Test
    fun `given some pairing credentials and pairing process succeeds, when reverse pairing starts, then pairing code should have code and reverse pairing should be successful`() {
        // given
        val code = "testCode"
        val token = "testToken"
        val pairingCode = "pairingCode"
        val reversePairingResponse = Response.success(
            ReversePairingStatusResponse(
                status = ReversePairingState.COMPLETED,
                pairingCode = pairingCode
            )
        )
        val credentials = ReversePairingCredentials(
            code = code,
            token = token
        )
        val case = Case()
        val userMock = mockk<UserRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        coEvery { tasksMock.fetchCase() } returns case
        coEvery { userMock.checkReversePairingStatus(credentials.token) } returns reversePairingResponse
        val observer =
            mockk<Observer<ReversePairingStatus>> { every { onChanged(any()) } just Runs }

        // when
        val viewModel = createViewModel(userMock, tasksMock)
        viewModel.reversePairingStatus.observeForever(observer)
        viewModel.startReversePairing(credentials = credentials)

        // then
        Assert.assertEquals(viewModel.pairingCode.value, code)
        Assert.assertEquals(viewModel.userHasSharedCode.value, true)
        verifySequence {
            observer.onChanged(ReversePairing(credentials))
            observer.onChanged(ReversePairingSuccess(pairingCode))
        }
    }

    @Test
    fun `given some pairing credentials and reverse pairing is expired, when reverse pairing starts, then pairing code should have code and reverse pairing should be expired`() {
        // given
        mockkStatic(LocalDateTime::class)
        val now = LocalDateTime.now(DateTimeZone.UTC)
        val expiryDate = now.minusDays(1)
        val expiryDateString = now.minusDays(1).toString(DateFormats.pairingData)
        every { LocalDateTime.parse(expiryDateString, DateFormats.pairingData) } returns expiryDate
        val code = "testCode"
        val token = "testToken"
        val pairingCode = "pairingCode"
        val reversePairingResponse = Response.success(
            ReversePairingStatusResponse(
                status = ReversePairingState.PENDING,
                refreshDelay = 1000,
                expiresAt = expiryDateString,
                pairingCode = pairingCode
            )
        )
        val credentials = ReversePairingCredentials(
            code = code,
            token = token
        )
        val case = Case()
        val userMock = mockk<UserRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        coEvery { tasksMock.fetchCase() } returns case
        coEvery { userMock.checkReversePairingStatus(credentials.token) } returns reversePairingResponse
        val observer =
            mockk<Observer<ReversePairingStatus>> { every { onChanged(any()) } just Runs }

        // when
        val viewModel = createViewModel(userMock, tasksMock)
        viewModel.reversePairingStatus.observeForever(observer)
        viewModel.startReversePairing(credentials = credentials)

        // then
        Assert.assertEquals(viewModel.pairingCode.value, code)
        verifySequence {
            observer.onChanged(ReversePairing(credentials))
            observer.onChanged(ReversePairingExpired)
        }
    }

    @Test
    fun `when polling is stopped, then stopped status should be present`() = runTest {
        // given
        val code = "testCode"
        val token = "testToken"
        val pairingCode = "pairingCode"
        val reversePairingResponse = Response.success(
            ReversePairingStatusResponse(
                status = ReversePairingState.COMPLETED,
                pairingCode = pairingCode
            )
        )
        val credentials = ReversePairingCredentials(
            code = code,
            token = token
        )
        val case = Case()
        val userMock = mockk<UserRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        coEvery { tasksMock.fetchCase() } returns case
        coEvery { userMock.checkReversePairingStatus(credentials.token) } returns reversePairingResponse
        val observer =
            mockk<Observer<ReversePairingStatus>> { every { onChanged(any()) } just Runs }

        // when
        val viewModel = createViewModel(userMock, tasksMock)
        viewModel.reversePairingStatus.observeForever(observer)
        viewModel.startReversePairing(credentials = credentials)
        viewModel.cancelPairing()

        // then
        Assert.assertEquals(viewModel.pairingCode.value, code)
        Assert.assertEquals(viewModel.userHasSharedCode.value, true)
        verifySequence {
            observer.onChanged(ReversePairing(credentials))
            observer.onChanged(ReversePairingSuccess(pairingCode))
            observer.onChanged(ReversePairingStopped)
        }
    }

    @Test
    fun `when user has shared code, then the value should be propagated in livedata`() {
        runTest {
            // when
            val value = true
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.setUserHasSharedCode(value)

            Assert.assertEquals(value, viewModel.userHasSharedCode.value)
        }
    }

    @Test
    fun `when user has not shared code, then the value should be propagated in livedata`() {
        runTest {
            // when
            val value = false
            val userMock = mockk<UserRepository>(relaxed = true)
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            val viewModel = createViewModel(userMock, tasksMock)
            viewModel.setUserHasSharedCode(value)

            Assert.assertEquals(value, viewModel.userHasSharedCode.value)
        }
    }

    private fun createViewModel(
        userRepository: IUserRepository,
        tasksRepository: ICaseRepository,
    ) = PairingViewModel(
        userRepository,
        tasksRepository,
        coroutineTestRule.testDispatcherProvider
    )
}