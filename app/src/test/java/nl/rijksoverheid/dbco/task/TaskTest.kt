package nl.rijksoverheid.dbco.task

import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import org.junit.Assert
import org.junit.Test

class TaskTest {

    @Test
    fun `given a task with informedAt, then didInform should be true`() {
        // given
        val task = Task(informedByIndexAt = "test")

        // then
        Assert.assertTrue(task.didInform)
    }

    @Test
    fun `given a task without informedAt, then didInform should be false`() {
        // given
        val task = Task(informedByIndexAt = null)

        // then
        Assert.assertFalse(task.didInform)
    }

    @Test
    fun `given a task without essential data, then task does not have all essential data`() {
        // given
        val task = Task()

        // then
        Assert.assertFalse(task.hasEssentialData())
    }

    @Test
    fun `given a task with essential data, then task does have all essential data`() {
        // given
        val contact = mockk<LocalContact>()
        every { contact.hasValidEmailOrPhone() } returns true
        every { contact.firstName } returns "test"
        every { contact.lastName } returns "test"
        val task = Task(
            linkedContact = contact,
            dateOfLastExposure = "test",
            category = Category.ONE
        )

        // then
        Assert.assertTrue(task.hasEssentialData())
    }

    @Test
    fun `given a task with empty questionnaire, percentage of completion should be 0`() {
        // given
        val task = Task()

        // then
        Assert.assertEquals(0, task.getPercentageCompletion())
    }

    @Test
    fun `given a task with questionnaire with all filled in answers, percentage of completion should be 100`() {
        // given
        val task = Task(
            questionnaireResult = QuestionnaireResult(
                questionnaireUuid = "test",
                answers = listOf(
                    Answer(
                        questionUuid = "test",
                        value = JsonObject(
                            content = mapOf("value" to "result".toJsonPrimitive())
                        )
                    ),
                    Answer(
                        questionUuid = "test2",
                        value = JsonObject(
                            content = mapOf("value" to "another".toJsonPrimitive())
                        )
                    )
                )
            )
        )

        // then
        Assert.assertEquals(100, task.getPercentageCompletion())
    }

    @Test
    fun `given a task with questionnaire with half filled in answers, percentage of completion should be 50`() {
        // given
        val task = Task(
            questionnaireResult = QuestionnaireResult(
                questionnaireUuid = "test",
                answers = listOf(
                    Answer(
                        questionUuid = "test",
                        value = JsonObject(
                            content = mapOf("value" to "result".toJsonPrimitive())
                        )
                    ),
                    Answer(
                        questionUuid = "test2",
                        value = JsonObject(content = emptyMap())
                    )
                )
            )
        )

        // then
        Assert.assertEquals(50, task.getPercentageCompletion())
    }

    @Test
    fun `given a task linked contact with display name and no context, display name for the task should be the same`() {
        // given
        val name = "name"
        val contact = mockk<LocalContact>()
        every { contact.getDisplayName() } returns name
        val task = Task(linkedContact = contact)

        // then
        Assert.assertEquals(name, task.getDisplayName(""))
    }

    @Test
    fun `given a task linked contact with display name and context, display name for the task should be the same`() {
        // given
        val name = "name"
        val context = "context"
        val contact = mockk<LocalContact>()
        every { contact.getDisplayName() } returns name
        val task = Task(linkedContact = contact, taskContext = context)

        // then
        Assert.assertEquals("$name ($context)", task.getDisplayName(""))
    }

    @Test
    fun `given a task with a label and a linked contact without display name and no context, display name for the task should be the same`() {
        // given
        val name = "name"
        val task = Task(linkedContact = null, label = name)

        // then
        Assert.assertEquals(name, task.getDisplayName(""))
    }

    @Test
    fun `given a task with a label and a linked contact without display name and a context, display name for the task should be the same`() {
        // given
        val name = "name"
        val context = "context"

        val task = Task(linkedContact = null, label = name, taskContext = context)

        // then
        Assert.assertEquals("$name ($context)", task.getDisplayName(""))
    }

    @Test
    fun `given a task with no label and a linked contact without display name and no context, display name for the task should be the fallback`() {
        // given
        val fallback = "name"

        val task = Task(linkedContact = null)

        // then
        Assert.assertEquals(fallback, task.getDisplayName(fallback))
    }

    @Test
    fun `given a task with no label and a linked contact without display name and a context, display name for the task should be the fallback and context`() {
        // given
        val fallback = "name"
        val context = "name"

        val task = Task(linkedContact = null, taskContext = context)

        // then
        Assert.assertEquals("$fallback ($context)", task.getDisplayName(fallback))
    }
}