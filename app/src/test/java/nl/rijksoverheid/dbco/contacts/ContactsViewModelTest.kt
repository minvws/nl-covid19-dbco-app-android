/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.*
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.utils.CoroutineTestRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContactsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Test
    fun `given a list of contacts, when local contacts are fetched, then livedata should contain those contacts`() {
        // given
        val contacts = arrayListOf(LocalContact(id = "1"), LocalContact(id = "2"))
        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()

        // then
        Assert.assertEquals(vm.localContactsLiveDataItem.value, contacts)
    }

    @Test
    fun `given a list of contacts and a query string, when local contacts are fetched and queried, then livedata should only contain those contacts who match that query`() {
        // given
        val query = "Niels"
        val niels = LocalContact(
            id = "1",
            firstName = "Niels"
        )
        val contacts = arrayListOf(
            niels,
            LocalContact(
                id = "2",
                firstName = "Henk"
            )
        )

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()
        vm.filterLocalContactsOnName(query)

        // then
        Assert.assertEquals(vm.localContactsLiveDataItem.value, arrayListOf(niels))
    }

    @Test
    fun `given a list of contacts and a query string with different caps, when local contacts are fetched and queried, then livedata should only contain those contacts who match that query`() {
        // given
        val query = "niels"
        val niels = LocalContact(
            id = "1",
            firstName = "Niels"
        )
        val contacts = arrayListOf(
            niels,
            LocalContact(
                id = "2",
                firstName = "Henk"
            )
        )

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()
        vm.filterLocalContactsOnName(query)

        // then
        Assert.assertEquals(vm.localContactsLiveDataItem.value, arrayListOf(niels))
    }

    @Test
    fun `given a list of contacts and a search query which matches both, suggested contacts are fetched, then return value should be both contacts`() {
        // given
        val query = "M"
        val mock1 = mockk<LocalContact>()
        val mock2 = mockk<LocalContact>()
        val contacts = arrayListOf(mock1, mock2)
        every { mock1.getDisplayName() } returns "Mock 1"
        every { mock2.getDisplayName() } returns "Mock 2"

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()

        // then
        Assert.assertEquals(vm.filterSuggestedContacts(query), arrayListOf(mock1, mock2))
    }

    @Test
    fun `given a list of contacts and a search query which matches only one, suggested contacts are fetched, then return value should be that only contact`() {
        // given
        val query = "Niels"
        val mock1 = mockk<LocalContact>()
        val mock2 = mockk<LocalContact>()
        val contacts = arrayListOf(mock1, mock2)
        every { mock1.getDisplayName() } returns "Niels de Jong"
        every { mock2.getDisplayName() } returns "Henk de Vries"

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()

        // then
        Assert.assertEquals(vm.filterSuggestedContacts(query), arrayListOf(mock1))
    }

    @Test
    fun `given a list of contacts, when contact names are fetched, then return value should be display names for contacts`() {
        // given
        val mock1 = mockk<LocalContact>()
        val mock2 = mockk<LocalContact>()
        val contacts = arrayListOf(mock1, mock2)
        every { mock1.getDisplayName() } returns "Mock 1"
        every { mock2.getDisplayName() } returns "Mock 2"

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        coEvery { contactsMock.fetchDeviceContacts() } returns contacts

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.fetchLocalContacts()

        // then
        Assert.assertEquals(vm.getLocalContactNames(), arrayListOf("Mock 1", "Mock 2"))
    }

    @Test
    fun `given a task with label, when label is fetched, then return value should be that label`() {
        // given
        val label = "Label"
        val uuid = "id"
        val task = Task(
            label = label,
            uuid = uuid
        )

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task

        // when
        val vm = createViewModel(caseMock, contactsMock)

        // then
        Assert.assertEquals(vm.getTaskLabel(uuid), label)
    }

    @Test
    fun `given a task and a contact, when contact is picked for that task, then contact should be stored in task and task should be saved`() {
        // given
        val uuid = "id"
        val task = Task(uuid = uuid)
        val contact = LocalContact(id = "test")

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task
        every { caseMock.saveTask(task, any(), any()) } just Runs

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.onContactPicked(uuid, contact)

        // then
        Assert.assertEquals(task.linkedContact, contact)
        verify { caseMock.saveTask(task, any(), any()) }
    }

    @Test
    fun `given a task without category or exposure date, when no contact is picked for that task, then then task should be deleted`() {
        // given
        val uuid = "id"
        val task = Task(uuid = uuid)

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task
        every { caseMock.deleteTask(uuid) } just Runs

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.onNoContactPicked(uuid)

        // then
        verify { caseMock.deleteTask(uuid) }
    }

    @Test
    fun `given a task with category and without exposure date, when no contact is picked for that task, then then task should not be deleted`() {
        // given
        val uuid = "id"
        val task = Task(uuid = uuid, category = Category.ONE)

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task
        every { caseMock.deleteTask(uuid) } just Runs

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.onNoContactPicked(uuid)

        // then
        verify(exactly = 0) { caseMock.deleteTask(uuid) }
    }

    @Test
    fun `given a task with category and with exposure date, when no contact is picked for that task, then then task should not be deleted`() {
        // given
        val uuid = "id"
        val task = Task(uuid = uuid, category = Category.ONE, dateOfLastExposure = "test")

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task
        every { caseMock.deleteTask(uuid) } just Runs

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.onNoContactPicked(uuid)

        // then
        verify(exactly = 0) { caseMock.deleteTask(uuid) }
    }

    @Test
    fun `given a task without category and with exposure date, when no contact is picked for that task, then then task should not be deleted`() {
        // given
        val uuid = "id"
        val task = Task(uuid = uuid, dateOfLastExposure = "test")

        val caseMock = mockk<ICaseRepository>()
        val contactsMock = mockk<ContactsRepository>()
        every { caseMock.getTask(uuid) } returns task
        every { caseMock.deleteTask(uuid) } just Runs

        // when
        val vm = createViewModel(caseMock, contactsMock)
        vm.onNoContactPicked(uuid)

        // then
        verify(exactly = 0) { caseMock.deleteTask(uuid) }
    }

    private fun createViewModel(
        caseRepository: ICaseRepository,
        contactsRepository: ContactsRepository
    ) = ContactsViewModel(
        contactsRepository,
        caseRepository,
        coroutineTestRule.testDispatcherProvider
    )
}