/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.task

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.JsonObject
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.util.toJsonPrimitive
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TasksDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `given a task with contact with valid phone or email, when view model init with task, livedata should have true value`() {
        // given
        val id = "test"
        val localContact = mockk<LocalContact>()
        every { localContact.hasValidEmailOrPhone() } returns true
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.hasEmailOrPhone.value == true)
    }

    @Test
    fun `given a task with contact with no valid phone or email, when view model init with task, livedata should have false value`() {
        // given
        val id = "test"
        val localContact = mockk<LocalContact>()
        every { localContact.hasValidEmailOrPhone() } returns false
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.hasEmailOrPhone.value == true)
    }

    @Test
    fun `given a task with valid phone number and feature flags with calling enabled, when view model init with task, calling should be enabled`() {
        // given
        val id = "test"
        val featureFlags = createFeatureFlags(callingEnabled = true)
        val localContact = mockk<LocalContact>()
        every { localContact.hasSingleValidPhoneNumber() } returns true
        every { localContact.hasValidEmailOrPhone() } returns true
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.callingEnabled(featureFlags))
    }

    @Test
    fun `given a task without valid phone number and feature flags with calling enabled, when view model init with task, calling should be disabled`() {
        // given
        val id = "test"
        val featureFlags = createFeatureFlags(callingEnabled = true)
        val localContact = mockk<LocalContact>()
        every { localContact.hasSingleValidPhoneNumber() } returns false
        every { localContact.hasValidEmailOrPhone() } returns true
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.callingEnabled(featureFlags))
    }

    @Test
    fun `given a task with valid phone number and feature flags with calling disabled, when view model init with task, calling should be disabled`() {
        // given
        val id = "test"
        val featureFlags = createFeatureFlags(callingEnabled = false)
        val localContact = mockk<LocalContact>()
        every { localContact.hasSingleValidPhoneNumber() } returns true
        every { localContact.hasValidEmailOrPhone() } returns true
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.callingEnabled(featureFlags))
    }

    @Test
    fun `given a task without valid phone number and feature flags with calling disabled, when view model init with task, calling should be disabled`() {
        // given
        val id = "test"
        val featureFlags = createFeatureFlags(callingEnabled = false)
        val localContact = mockk<LocalContact>()
        every { localContact.hasSingleValidPhoneNumber() } returns false
        every { localContact.hasValidEmailOrPhone() } returns true
        every { localContact.firstName } returns "test"
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.callingEnabled(featureFlags))
    }

    @Test
    fun `given feature flags with copy disabled then copy should be disabled`() {
        // given
        val featureFlags = createFeatureFlags(copyEnabled = false)

        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)

        // then
        Assert.assertFalse(viewModel.copyEnabled(featureFlags))
    }

    @Test
    fun `given feature flags with copy enabled then copy should be enabled`() {
        // given
        val featureFlags = createFeatureFlags(copyEnabled = true)

        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)

        // then
        Assert.assertTrue(viewModel.copyEnabled(featureFlags))
    }

    @Test
    fun `given a task with contact first name, when view model init with task, livedata should have first name value`() {
        // given
        val id = "test"
        val name = "name"
        val localContact = mockk<LocalContact>()
        every { localContact.hasValidEmailOrPhone() } returns false
        every { localContact.firstName } returns name
        val task = Task().apply {
            linkedContact = localContact
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.name.value == name)
    }

    @Test
    fun `given a task with communication type, when view model init with task, livedata should have that type`() {
        // given
        val id = "test"
        val communicationType = CommunicationType.Index
        val task = Task().apply {
            communication = communicationType
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.communicationType.value == communicationType)
    }

    @Test
    fun `given a task with last exposure date, when view model init with task, livedata should have that date`() {
        // given
        val id = "test"
        val date = "date"
        val task = Task().apply {
            dateOfLastExposure = date
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.dateOfLastExposure.value == date)
    }

    @Test
    fun `given a task with category, when view model init with task, livedata should have that category`() {
        // given
        val id = "test"
        val cat = Category.THREE_B
        val task = Task().apply {
            category = cat
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.category.value == cat)
    }

    @Test
    fun `given a task with questionnaire, when view model init with task, text answers in questionnare should be cached`() {
        // given
        val id = "test"
        val questionId = "questionId"
        val result = "result"
        val task = Task().apply {
            questionnaireResult = QuestionnaireResult(
                questionnaireUuid = "questionnaireId",
                answers = listOf(
                    Answer(
                        questionUuid = questionId,
                        value = JsonObject(
                            content = mapOf("value" to result.toJsonPrimitive())
                        )
                    )
                )
            )
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.textAnswers[questionId] == result)
    }

    @Test
    fun `given a task with questionnaire, when view model init with task, non text answers in questionnare should not be cached`() {
        // given
        val id = "test"
        val questionId = "questionId"
        val result = "result"
        val task = Task().apply {
            questionnaireResult = QuestionnaireResult(
                questionnaireUuid = "questionnaireId",
                answers = listOf(
                    Answer(
                        questionUuid = questionId,
                        value = JsonObject(
                            content = mapOf("not-a-value" to result.toJsonPrimitive())
                        )
                    )
                )
            )
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.textAnswers.isEmpty())
    }

    @Test
    fun `given a task with category one, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.ONE }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == true)
        Assert.assertTrue(viewModel.distanceRisk.value == null)
        Assert.assertTrue(viewModel.physicalContactRisk.value == null)
        Assert.assertTrue(viewModel.sameRoomRisk.value == null)
    }

    @Test
    fun `given a task with category two a, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.TWO_A }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == false)
        Assert.assertTrue(viewModel.distanceRisk.value == Pair(first = true, second = true))
        Assert.assertTrue(viewModel.physicalContactRisk.value == null)
        Assert.assertTrue(viewModel.sameRoomRisk.value == null)
    }

    @Test
    fun `given a task with category two b, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.TWO_B }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == false)
        Assert.assertTrue(viewModel.distanceRisk.value == Pair(first = true, second = false))
        Assert.assertTrue(viewModel.physicalContactRisk.value == true)
        Assert.assertTrue(viewModel.sameRoomRisk.value == null)
    }

    @Test
    fun `given a task with category three a, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.THREE_A }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == false)
        Assert.assertTrue(viewModel.distanceRisk.value == Pair(first = true, second = false))
        Assert.assertTrue(viewModel.physicalContactRisk.value == false)
        Assert.assertTrue(viewModel.sameRoomRisk.value == null)
    }

    @Test
    fun `given a task with category three b, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.THREE_B }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == false)
        Assert.assertTrue(viewModel.distanceRisk.value == Pair(first = false, second = false))
        Assert.assertTrue(viewModel.physicalContactRisk.value == null)
        Assert.assertTrue(viewModel.sameRoomRisk.value == true)
    }

    @Test
    fun `given a task with category without risk, when view model init with task, correct risk flags are set`() {
        // given
        val id = "test"
        val task = Task().apply { category = Category.NO_RISK }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.sameHouseholdRisk.value == false)
        Assert.assertTrue(viewModel.distanceRisk.value == Pair(first = false, second = false))
        Assert.assertTrue(viewModel.physicalContactRisk.value == false)
        Assert.assertTrue(viewModel.sameRoomRisk.value == false)
    }

    @Test
    fun `given a task without local contact, when view model init with task, local contact should be set with label from task`() {
        // given
        val id = "test"
        val task = Task()
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.task.linkedContact != null)
    }

    @Test
    fun `given a task with questionnaire, when view model init with task, view model should return those answers`() {
        // given
        val id = "test"
        val questionId = "questionId"
        val result = "result"
        val answers = listOf(
            Answer(
                questionUuid = questionId,
                value = JsonObject(
                    content = mapOf("value" to result.toJsonPrimitive())
                )
            )
        )
        val task = Task().apply {
            questionnaireResult = QuestionnaireResult(
                questionnaireUuid = "questionnaireId",
                answers = answers
            )
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertEquals(viewModel.getQuestionnaireAnswers(), answers)
    }

    @Test
    fun `given a task with exposure date, when view model init with task and task member exposure date is altered, view model should return true for updated exposure date`() {
        // given
        val id = "test"
        val task = Task().apply {
            dateOfLastExposure = "date"
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.task.dateOfLastExposure = "newDate"

        // then
        Assert.assertTrue(viewModel.hasUpdatedExposureDate())
    }

    @Test
    fun `given a task with exposure date, when view model init with task and task member exposure date is not altered, view model should return false for updated exposure date`() {
        // given
        val id = "test"
        val task = Task().apply {
            dateOfLastExposure = "date"
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.hasUpdatedExposureDate())
    }

    @Test
    fun `given the repository returns a case reference, when case reference is fetched then return that reference`() {
        // given
        val reference = "reference"
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getCaseReference() } returns reference

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)

        // then
        Assert.assertEquals(viewModel.getCaseReference(), reference)
    }

    @Test
    fun `when repository has start of allowed contagious period, when earliest exposure date is fetched, then it should be the same`() {
        // given
        val tasksMock = mockk<ICaseRepository>()
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { tasksMock.getStartOfAllowedContagiousPeriod() } returns date

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)

        // then
        Assert.assertEquals(viewModel.getEarliestExposureDateOption(), date)
    }

    @Test
    fun `given a task with uuid, when view model init with task and task is deleted, repository should delete task`() {
        // given
        val id = "test"
        val uid = "uuid"
        val task = Task().apply {
            uuid = uid
        }
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.deleteCurrentTask()

        // then
        verify { tasksMock.deleteTask(uid) }
    }

    @Test
    fun `given a task without uuid, when view model init with task and task is deleted, repository should delete task`() {
        // given
        val id = "test"
        val task = Task()
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.deleteCurrentTask()

        // then
        verify(inverse = true) { tasksMock.deleteTask(any()) }
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.NO_RISK
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = true
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.ONE)
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value of two a`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.NO_RISK
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = false
        viewModel.distanceRisk.value = Pair(first = true, second = true)
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.TWO_A)
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value of two b`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.NO_RISK
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = false
        viewModel.distanceRisk.value = Pair(first = true, second = false)
        viewModel.physicalContactRisk.value = true
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.TWO_B)
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value of three a`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.NO_RISK
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = false
        viewModel.distanceRisk.value = Pair(first = true, second = false)
        viewModel.physicalContactRisk.value = false
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.THREE_A)
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value of three b`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.NO_RISK
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = false
        viewModel.distanceRisk.value = Pair(first = false, second = false)
        viewModel.physicalContactRisk.value = false
        viewModel.sameRoomRisk.value = true
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.THREE_B)
    }

    @Test
    fun `given a task with category, when view model init with task and risk flags are, category should be set to the correct value of no risk`() {
        // given
        val id = "test"
        val task = Task(
            category = Category.ONE
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        // when
        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.sameHouseholdRisk.value = false
        viewModel.distanceRisk.value = Pair(first = false, second = false)
        viewModel.physicalContactRisk.value = false
        viewModel.sameRoomRisk.value = false
        viewModel.updateCategoryFromRiskFlags()

        // then
        Assert.assertTrue(viewModel.category.value == Category.NO_RISK)
    }

    @Test
    fun `given changes are enabled and a local task with category and no exposure date, then task should be deletable`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            category = Category.ONE,
            source = Source.App
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are enabled and a local task with no category and exposure date, then task should be deletable`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            dateOfLastExposure = "date",
            source = Source.App
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are enabled and a local task with both a category and exposure date, then task should be deletable`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            dateOfLastExposure = "date",
            source = Source.App,
            category = Category.ONE
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertTrue(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are enabled and a local task with no a category and exposure date, then task should not be deletable`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            source = Source.App,
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are enabled and a remote task with a category and exposure date, then task should not be deletable`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            source = Source.Portal,
            category = Category.ONE,
            dateOfLastExposure = "date"
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are not enabled and a local task with a category and exposure date, then task should not be deletable`() {
        // given
        val id = "test"
        val changesEnabled = false
        val task = Task(
            source = Source.App,
            category = Category.ONE,
            dateOfLastExposure = "date"
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)

        // then
        Assert.assertFalse(viewModel.isDeletionPossible(changesEnabled))
    }

    @Test
    fun `given changes are not enabled, when task is cancelled, then task should not be deleted`() {
        // given
        val id = "test"
        val changesEnabled = false
        val task = Task(
            source = Source.App,
            category = Category.ONE,
            dateOfLastExposure = "date"
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.onCancelled(changesEnabled)

        // then
        verify(exactly = 0) { tasksMock.deleteTask(id) }
    }

    @Test
    fun `given changes are enabled and local task with no information, when task is cancelled, then task should be deleted`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            uuid = id,
            source = Source.App,
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.onCancelled(changesEnabled)

        // then
        verify { tasksMock.deleteTask(id) }
    }

    @Test
    fun `given changes are enabled and local task with category, when task is cancelled, then task should not be deleted`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            uuid = id,
            source = Source.App,
            category = Category.ONE
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.onCancelled(changesEnabled)

        // then
        verify(exactly = 0) { tasksMock.deleteTask(id) }
    }

    @Test
    fun `given changes are enabled and local task with exposure date, when task is cancelled, then task should not be deleted`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            uuid = id,
            source = Source.App,
            dateOfLastExposure = "date"
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.onCancelled(changesEnabled)

        // then
        verify(exactly = 0) { tasksMock.deleteTask(id) }
    }

    @Test
    fun `given changes are enabled and local task which is saved, when task is cancelled, then task should not be deleted`() {
        // given
        val id = "test"
        val changesEnabled = true
        val task = Task(
            uuid = id,
            source = Source.App,
            questionnaireResult = QuestionnaireResult(id, emptyList())
        )
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        val questionnaireMock = mockk<IQuestionnaireRepository>(relaxed = true)
        every { tasksMock.getTask(id) } returns task

        val viewModel = createViewModel(tasksMock, questionnaireMock)
        viewModel.init(id)
        viewModel.onCancelled(changesEnabled)

        // then
        verify(exactly = 0) { tasksMock.deleteTask(id) }
    }

    private fun createFeatureFlags(
        callingEnabled: Boolean = true,
        copyEnabled: Boolean = true
    ) = FeatureFlags(
        enableContactCalling = callingEnabled,
        enablePerspectiveCopy = copyEnabled,
        enableSelfBCO = true
    )

    private fun createViewModel(
        tasksRepository: ICaseRepository,
        questionnaireRepository: IQuestionnaireRepository
    ) = TasksDetailViewModel(
        tasksRepository,
        questionnaireRepository
    )
}