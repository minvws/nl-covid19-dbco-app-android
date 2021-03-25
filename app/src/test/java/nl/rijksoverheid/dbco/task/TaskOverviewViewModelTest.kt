/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */


package nl.rijksoverheid.dbco.task

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.Resource
import nl.rijksoverheid.dbco.util.resolve
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TaskOverviewViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var taskRepository: ITaskRepository

    @Mock
    lateinit var questionnaireRepository: IQuestionnaireRepository

    @Test
    fun `when cat is not the same, when the list is sorted, category one should be first`() =
        runBlockingTest {
            // given
            val case = Case(
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
            whenever(taskRepository.fetchCase()).thenReturn(case)

            // when
            val viewModel = createViewModel()
            viewModel.syncTasks()

            // then
            val result = viewModel.fetchCase.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Resource.Success)
            result!!.resolve(onSuccess = { sortedCase ->
                Assert.assertEquals(sortedCase.tasks.first(), case.tasks.last())
            })
        }

    @Test
    fun `when cat is the same, when the list is sorted, then the later date should be first`() =
        runBlockingTest {
            // given
            val case = Case(
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
            whenever(taskRepository.fetchCase()).thenReturn(case)

            // when
            val viewModel = createViewModel()
            viewModel.syncTasks()

            // then
            val result = viewModel.fetchCase.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Resource.Success)
            result!!.resolve(onSuccess = { sortedCase ->
                Assert.assertEquals(sortedCase.tasks.first(), case.tasks.last())
            })
        }

    @Test
    fun `given tasks with same category and date, when the list is sorted, then the label a should be first`() =
        runBlockingTest {
            // given
            val case = Case(
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
            whenever(taskRepository.fetchCase()).thenReturn(case)

            // when
            val viewModel = createViewModel()
            viewModel.syncTasks()

            // then
            val result = viewModel.fetchCase.value
            Assert.assertTrue(result != null)
            Assert.assertTrue(result is Resource.Success)
            result!!.resolve(onSuccess = { sortedCase ->
                Assert.assertEquals(sortedCase.tasks.first(), case.tasks.last())
            })
        }

    private fun createViewModel() = TasksOverviewViewModel(
        taskRepository,
        questionnaireRepository,
        TestCoroutineDispatcher()
    )
}