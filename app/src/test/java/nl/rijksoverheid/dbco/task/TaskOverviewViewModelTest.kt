/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */


package nl.rijksoverheid.dbco.task

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.Success
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.Error
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.CaseExpired
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireError
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireSuccess
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import java.lang.IllegalStateException

@RunWith(MockitoJUnitRunner::class)
class TaskOverviewViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `when cat is not the same, when the list is sorted, category one should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.NO_RISK
                    },
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.THREE_B
                    },
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.TWO_B
                    },
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.TWO_A
                    },
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Success)

            Assert.assertEquals((result as Success).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `when cat is the same, when the list is sorted, then the later date should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-02"
                        category = Category.ONE
                    },
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Success)

            Assert.assertEquals((result as Success).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `given tasks with same category and date, when the list is sorted, then the label a should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Success)

            Assert.assertEquals((result as Success).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `given a expiry date in the future, when case is fetched it should be in success state`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Success)
            Assert.assertEquals((result as Success).case, case)
        }

    @Test
    fun `given a expiry date equal to now, when case is fetched it should return cached in expired state`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseExpired)
            Assert.assertEquals((result as CaseExpired).cachedCase, case)
        }

    @Test
    fun `given a expiry date in past, when case is fetched it should return cached in expired state`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.minusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseExpired)
            Assert.assertEquals((result as CaseExpired).cachedCase, case)
        }

    @Test
    fun `given a expiry date in past, when case is fetched it should be in error state with cached case`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(
                windowExpiresAt = "test",
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } throws IllegalStateException("test")

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Error)
            Assert.assertEquals((result as Error).cachedCase, case)
        }

    @Test
    fun `given a case without expiry date, when case is fetched it should be in success state with new case`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val cached = Case(
                windowExpiresAt = null,
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            val new = Case(
                tasks = listOf(
                    Task().apply {
                        label = "A"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    },
                    Task().apply {
                        label = "B"
                        dateOfLastExposure = "2021-01-01"
                        category = Category.ONE
                    }
                )
            )
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns cached
            coEvery { tasksMock.fetchCase() } returns new

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncTasks()

            // then
            val result: CaseResult? = viewModel.case.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Success)
            Assert.assertEquals((result as Success).case, new)
        }

    @Test
    fun `given questionnaire throws error, when questionnaire is synced, then pass error state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            coEvery { questionnaireMock.syncQuestionnaires() } throws IllegalStateException("test")

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncQuestionnaire()

            // then
            val result: QuestionnaireResult? = viewModel.questionnaire.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is QuestionnaireError)
        }

    @Test
    fun `given questionnaire throws no error, when questionnaire is synced, then pass success state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ITaskRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncQuestionnaire()

            // then
            val result: QuestionnaireResult? = viewModel.questionnaire.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is QuestionnaireSuccess)
        }

    private fun createViewModel(
        tasksRepository: ITaskRepository,
        questionnaireRepository: IQuestionnaireRepository
    ) = TasksOverviewViewModel(
        tasksRepository,
        questionnaireRepository,
        TestCoroutineDispatcher()
    )
}