/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.task

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.CaseResult.CaseSuccess
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.CaseResult.CaseError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.CaseResult.CaseExpired
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.CaseResult
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.QuestionnaireResult
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireSuccess
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadSuccess
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.mockito.ArgumentMatchers.any
import java.lang.IllegalStateException

@RunWith(MockitoJUnitRunner::class)
class TaskOverviewViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `when repository has cache, when cached case is fetched, return that case`() {
        // given
        val case = Case()
        val tasksMock = mockk<ICaseRepository>()
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getCase() } returns case

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        val result = viewModel.getCachedCase()

        Assert.assertEquals(case, result)
    }

    @Test
    fun `when repository has questionnaire, when cached questionnaire is fetched, return that questionnaire`() {
        // given
        val questionnaire = Questionnaire()
        val tasksMock = mockk<ICaseRepository>()
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { questionnaireMock.getCachedQuestionnaire() } returns questionnaire

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        val result = viewModel.getCachedQuestionnaire()

        Assert.assertEquals(questionnaire, result)
    }

    @Test
    fun `when case is uploaded, when error is encountered, post error to live data`() = runBlockingTest {
        // given
        val tasksMock = mockk<ICaseRepository>()
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        coEvery { tasksMock.uploadCase() } throws IllegalStateException("test")

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.uploadCurrentCase()

        Assert.assertTrue(viewModel.uploadStatus.value == UploadError)
    }

    @Test
    fun `when case is uploaded, when no error is encountered, post success to live data`() = runBlockingTest {
        // given
        val tasksMock = mockk<ICaseRepository>()
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        coEvery { tasksMock.uploadCase() } just Runs

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.uploadCurrentCase()

        Assert.assertTrue(viewModel.uploadStatus.value == UploadSuccess)
    }

    @Test
    fun `when current case is expired, when case expiration is checked, then the case should be expired`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(windowExpiresAt = "test")
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.minusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncData()

            // then
            Assert.assertTrue(viewModel.isCurrentCaseExpired())
        }

    @Test
    fun `when current case is not expired, when case expiration is checked, then the case should not be expired`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = Case(windowExpiresAt = "test")
            every { LocalDateTime.parse("test", DateFormats.expiryData) } returns now.plusDays(1)
            every { tasksMock.getCase() } returns case
            coEvery { tasksMock.fetchCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncData()

            // then
            Assert.assertFalse(viewModel.isCurrentCaseExpired())
        }

    @Test
    fun `when case has task with not all data, when essential data is checked, then it should be false`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = mockk<Case>()
            every { case.hasEssentialTaskData() } returns false
            every { tasksMock.getCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)

            // then
            Assert.assertFalse(viewModel.hasEssentialTaskData())
        }

    @Test
    fun `when case has tasks with all data, when essential data is checked, then it should be true`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val case = mockk<Case>()
            every { case.hasEssentialTaskData() } returns true
            every { tasksMock.getCase() } returns case

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)

            // then
            Assert.assertTrue(viewModel.hasEssentialTaskData())
        }

    @Test
    fun `when repository has start of contagious period, when start date is fetched, then it should be the same`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            val date = LocalDate.now(DateTimeZone.UTC)
            every { tasksMock.getStartOfContagiousPeriod() } returns date

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)

            // then
            Assert.assertEquals(viewModel.getStartOfContagiousPeriod(), date)
        }


    @Test
    fun `when cat is not the same, when the list is sorted, category one should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseSuccess)

            Assert.assertEquals((result as CaseSuccess).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `when cat is the same, when the list is sorted, then the later date should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseSuccess)

            Assert.assertEquals((result as CaseSuccess).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `given tasks with same category and date, when the list is sorted, then the label a should be first`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseSuccess)

            Assert.assertEquals((result as CaseSuccess).case.tasks.first(), case.tasks.last())
        }

    @Test
    fun `given a expiry date in the future, when case is fetched it should be in success state`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseSuccess)
            Assert.assertEquals((result as CaseSuccess).case, case)
        }

    @Test
    fun `given a expiry date equal to now, when case is fetched it should return cached in expired state`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
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
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
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
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseError)
            Assert.assertEquals((result as CaseError).cachedCase, case)
        }

    @Test
    fun `given a case without expiry date, when case is fetched it should be in success state with new case`() =
        runBlockingTest {
            // given
            val now = LocalDateTime.now(DateTimeZone.UTC)
            mockkStatic(LocalDateTime::class)
            val tasksMock = mockk<ICaseRepository>()
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
            viewModel.syncData()

            // then
            val result: CaseResult? = viewModel.viewData.value?.caseResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is CaseSuccess)
            Assert.assertEquals((result as CaseSuccess).case, new)
        }

    @Test
    fun `given questionnaire throws error, when questionnaire is synced, then pass error state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            coEvery { questionnaireMock.syncQuestionnaires() } throws IllegalStateException("test")
            every { tasksMock.getCase() } returns Case()
            coEvery { tasksMock.fetchCase() } returns Case()

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncData()

            // then
            val result: QuestionnaireResult? = viewModel.viewData.value?.questionnaireResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is QuestionnaireError)
        }

    @Test
    fun `given questionnaire throws no error, when questionnaire is synced, then pass CaseSuccess state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            every { tasksMock.getCase() } returns Case()
            coEvery { tasksMock.fetchCase() } returns Case()

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.syncData()

            // then
            val result: QuestionnaireResult? = viewModel.viewData.value?.questionnaireResult
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is QuestionnaireSuccess)
        }

    @Test
    fun `given upload fails, when case is uploaded, then pass error state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>()
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
            coEvery { tasksMock.uploadCase() } throws IllegalStateException("test")

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.uploadCurrentCase()

            // then
            val result: UploadStatus? = viewModel.uploadStatus.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is UploadError)
        }

    @Test
    fun `given upload succeeds, when case is uploaded, then pass success state`() =
        runBlockingTest {
            // given
            val tasksMock = mockk<ICaseRepository>(relaxed = true)
            val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)

            // when
            val viewModel = createViewModel(tasksMock, questionnaireMock)
            viewModel.uploadCurrentCase()

            // then
            val result: UploadStatus? = viewModel.uploadStatus.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is UploadSuccess)
        }

    private fun createViewModel(
        tasksRepository: ICaseRepository,
        questionnaireRepository: IQuestionnaireRepository
    ) = TasksOverviewViewModel(
        tasksRepository,
        questionnaireRepository,
        TestCoroutineDispatcher()
    )
}