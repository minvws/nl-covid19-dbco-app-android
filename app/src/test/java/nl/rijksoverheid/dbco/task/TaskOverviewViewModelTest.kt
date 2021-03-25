package nl.rijksoverheid.dbco.task

import com.nhaarman.mockitokotlin2.mock
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import org.junit.Assert
import org.junit.Test

class TaskOverviewViewModelTest {

    private val viewModel = TasksOverviewViewModel(mock(), mock())

    @Test
    fun `when cat is not the same, when the list is sorted, category one should be first`() {
        val tasks = listOf(
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

        val sorted = viewModel.sortTasks(tasks)

        Assert.assertEquals(sorted.first(), tasks.last())
    }

    @Test
    fun `when cat is the same, when the list is sorted, then the later date should be first`() {
        // given
        val tasks = listOf(
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

        // when
        val sorted = viewModel.sortTasks(tasks)

        // then
        Assert.assertEquals(sorted.first(), tasks.last())
    }

    @Test
    fun `given tasks with same category and date, when the list is sorted, then the label a should be first`() {
        // given
        val tasks = listOf(
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

        // when
        val sorted = viewModel.sortTasks(tasks)

        // then
        Assert.assertEquals(sorted.first(), tasks.last())
    }
}