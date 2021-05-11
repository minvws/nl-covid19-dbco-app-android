package nl.rijksoverheid.dbco.bcocase

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.bcocase.data.entity.TaskType
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.config.Symptom
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SelfBcoCaseViewModelTest {

    @Test
    fun `given a repository with symptoms, when symptoms are retrieved from viewmodel, then they should be the same`() {
        // given
        val symptoms = listOf(Symptom("test", "test"))
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { appConfigMockk.getSymptoms() } returns symptoms

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(symptoms, viewModel.getSymptoms())
    }

    @Test
    fun `given a repository with supported zip code, when zipcode supported is checked, it should be true`() {
        // given
        val zipCode = 1000
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { appConfigMockk.isSelfBcoSupportedForZipCode(zipCode = zipCode) } returns true

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertTrue(viewModel.isZipCodeSupported(zipCode))
    }

    @Test
    fun `given a repository with unsupported zip code, when zipcode supported is checked, it should be false`() {
        // given
        val zipCode = 1000
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { appConfigMockk.isSelfBcoSupportedForZipCode(zipCode = zipCode) } returns false

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertFalse(viewModel.isZipCodeSupported(zipCode))
    }

    @Test
    fun `given a list of selected symptoms, when these are saved, then they should be saved in the repository`() {
        // given
        val symptoms = listOf(Symptom("test", "test"))
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.setSelectedSymptoms(symptoms)

        // then
        verify { tasksMock.setSymptoms(symptoms) }
    }

    @Test
    fun `given a repository with roommates, when roommates are retrieved from viewmodel, then they should be the same`() {
        // given
        val roommates = listOf(Task())
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { tasksMock.getContactsByCategory(Category.ONE) } returns roommates

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(roommates, viewModel.getRoommates())
    }

    @Test
    fun `given a repository with contacts, when contacts are retrieved from viewmodel, then they should be the same`() {
        // given
        val contacts = listOf(Task())
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { tasksMock.getContactsByCategory(null) } returns contacts

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(contacts, viewModel.getTimelineContacts())
    }

    @Test
    fun `given a contact uuid, when it is removed, then it should be removed in the repository`() {
        // given
        val id = "test"
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.removeContact(id)

        // then
        verify { tasksMock.deleteTask(id) }
    }

    @Test
    fun `given a name, when it is saved as a contact, then it should be saved in the repository if the name is not empty`() {
        // given
        val name = "test"
        val uuid = "uuid"
        val category = Category.ONE
        val lastExposureDate = "date"
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns uuid
        val task = Task(
            taskType = TaskType.Contact,
            source = Source.App,
            category = category,
            label = name,
            uuid = UUID.randomUUID().toString(),
            dateOfLastExposure = lastExposureDate
        )
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.addContact(name = name, dateOfLastExposure = lastExposureDate, category = category)

        // then
        verify { tasksMock.saveTask(task, any(), any()) }
    }

    @Test
    fun `given an empty name, when it is saved as a contact, then it should not be saved in the repository if the name is not empty`() {
        // given
        val name = ""
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.addContact(name = name, dateOfLastExposure = "test", category = null)

        // then
        verify(inverse = true) { tasksMock.saveTask(any(), any(), any()) }
    }

    @Test
    fun `given a start of contagious period in repository, when it is fetched in viewmodel then return the same`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getStartOfContagiousPeriod() } returns date

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getStartOfContagiousPeriod(), date)
    }

    @Test
    fun `given a symptom onset date in repository, when view model is in symptom state, the symptom onset date should be the start date`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getSymptomOnsetDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.setTypeOfFlow(SelfBcoConstants.SYMPTOM_CHECK_FLOW)

        // then
        Assert.assertEquals(viewModel.getStartDate(), date)
    }

    @Test
    fun `given a symptom onset date in repository, when view model is in test state, the test date should be the start date`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getTestDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.setTypeOfFlow(SelfBcoConstants.COVID_CHECK_FLOW)

        // then
        Assert.assertEquals(viewModel.getStartDate(), date)
    }

    @Test
    fun `given a test date in repository, when test date is retrieved, it should be the same`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getTestDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getDateOfTest(), date)
    }

    @Test
    fun `given a symptom onset date in repository, when symptom onset date is retrieved, it should be the same`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getSymptomOnsetDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getDateOfSymptomOnset(), date)
    }

    @Test
    fun `given a negative test date in repository, when negative test date is retrieved, it should be the same`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getNegativeTestDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getDateOfNegativeTest(), date)
    }

    @Test
    fun `given a positive test date in repository, when positive test date is retrieved, it should be the same`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getPositiveTestDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getDateOfPositiveTest(), date)
    }

    @Test
    fun `given a symptoms worsened date in repository, when symptom worsened date is retrieved, it should be the same`() {
        // given
        mockkStatic(LocalDate::class)
        val date = LocalDate.now(DateTimeZone.UTC)
        every { LocalDate.parse("test", DateFormats.dateInputData) } returns date
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)
        every { tasksMock.getIncreasedSymptomDate() } returns "test"

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getDateOfIncreasedSymptoms(), date)
    }

    @Test
    fun `given a symptoms worsened date, when the date is updated, it should be updated in the repository`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.updateDateOfIncreasedSymptoms(date)

        // then
        verify { tasksMock.updateIncreasedSymptomDate(date.toString(DateFormats.dateInputData)) }
    }

    @Test
    fun `given a negative test date, when the date is updated, it should be updated in the repository`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.updateDateOfNegativeTest(date)

        // then
        verify { tasksMock.updateNegativeTestDate(date.toString(DateFormats.dateInputData)) }
    }

    @Test
    fun `given a positive test date, when the date is updated, it should be updated in the repository`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.updateDateOfPositiveTest(date)

        // then
        verify { tasksMock.updatePositiveTestDate(date.toString(DateFormats.dateInputData)) }
    }

    @Test
    fun `given a test date, when the date is updated, it should be updated in the repository`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.updateTestDate(date)

        // then
        verify { tasksMock.updateTestDate(date.toString(DateFormats.dateInputData)) }
    }

    @Test
    fun `given a symptom onset date, when the date is updated, it should be updated in the repository`() {
        // given
        val date = LocalDate.now(DateTimeZone.UTC)
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.updateDateOfSymptomOnset(date)

        // then
        verify { tasksMock.updateSymptomOnsetDate(date.toString(DateFormats.dateInputData)) }
    }

    @Test
    fun `given a test flow, when the flow is updated, it should be the same when retrieved`() {
        // given
        val flow = SelfBcoConstants.COVID_CHECK_FLOW
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.setTypeOfFlow(flow)

        // then
        Assert.assertEquals(viewModel.getTypeOfFlow(), flow)
    }

    @Test
    fun `given a symptom flow, when the flow is updated, it should be the same when retrieved`() {
        // given
        val flow = SelfBcoConstants.SYMPTOM_CHECK_FLOW
        val appConfigMockk = mockk<AppConfigRepository>(relaxed = true)
        val tasksMock = mockk<ICaseRepository>(relaxed = true)

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)
        viewModel.setTypeOfFlow(flow)

        // then
        Assert.assertEquals(viewModel.getTypeOfFlow(), flow)
    }

    @Test
    fun `given a repository with selected symptom, when symptom size is retrieved from view model, then it should be one`() {
        // given
        val symptoms = listOf("test")
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { tasksMock.getSymptoms() } returns symptoms

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertTrue(viewModel.getSelectedSymptomsSize() == 1)
    }

    @Test
    fun `given a repository with selected symptoms, when symptom size is retrieved from view model, then they should be the size of the symptoms`() {
        // given
        val symptoms = listOf("test", "test2")
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { tasksMock.getSymptoms() } returns symptoms

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertTrue(viewModel.getSelectedSymptomsSize() == 2)
    }

    @Test
    fun `given a repository with selected symptoms, when symptoms are retrieved from viewmodel, then they should be the same`() {
        // given
        val symptoms = listOf("test", "test2")
        val appConfigMockk = mockk<AppConfigRepository>()
        val tasksMock = mockk<ICaseRepository>()
        every { tasksMock.getSymptoms() } returns symptoms

        // when
        val viewModel = SelfBcoCaseViewModel(tasksMock, appConfigMockk)

        // then
        Assert.assertEquals(viewModel.getSelectedSymptoms(), symptoms)
    }
}