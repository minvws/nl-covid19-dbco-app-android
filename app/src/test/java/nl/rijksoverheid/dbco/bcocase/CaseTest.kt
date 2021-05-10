package nl.rijksoverheid.dbco.bcocase

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import org.junit.Assert
import org.junit.Test

class CaseTest {

    @Test
    fun `when tasks have essential data, then case has essential data`() {
        // given
        val task = mockk<Task>()
        val case = Case(tasks = listOf(task))
        every { task.hasEssentialData() } returns true

        // then
        Assert.assertTrue(case.hasEssentialTaskData())
    }

    @Test
    fun `when not all tasks have essential data, then case does not have essential data`() {
        // given
        val task = mockk<Task>()
        val case = Case(tasks = listOf(task))
        every { task.hasEssentialData() } returns false

        // then
        Assert.assertFalse(case.hasEssentialTaskData())
    }
}