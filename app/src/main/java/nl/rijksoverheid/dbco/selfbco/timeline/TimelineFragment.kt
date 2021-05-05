/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoTimelineBinding
import nl.rijksoverheid.dbco.items.input.ButtonItem
import nl.rijksoverheid.dbco.items.input.ButtonType
import nl.rijksoverheid.dbco.items.ui.*
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.SelfBcoConstants
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.util.hideKeyboard
import org.joda.time.Days
import org.joda.time.LocalDate
import java.io.Serializable

class TimelineFragment : BaseFragment(R.layout.fragment_selfbco_timeline) {

    private val selfBcoViewModel: SelfBcoCaseViewModel by activityViewModels()

    private val contactsViewModel: ContactsViewModel by viewModels()

    val adapter = GroupAdapter<GroupieViewHolder>()

    private val sections = ArrayList<TimelineSection>()
    private var contactNames = ArrayList<String>()

    lateinit var header: StringHeaderItem
    lateinit var binding: FragmentSelfbcoTimelineBinding
    lateinit var firstDayInTimeLine: LocalDate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoTimelineBinding.bind(view)
        adapter.clear()
        sections.clear()
        contactNames.clear()
        val content = Section()

        firstDayInTimeLine = selfBcoViewModel.getStartOfContagiousPeriod()

        val state: State = State.fromBundle(savedInstanceState) ?: State(
            selfBcoViewModel.getTimelineContacts().map {
                State.Contact(
                    name = it.label!!,
                    uuid = it.uuid,
                    date = it.dateOfLastExposure!!
                )
            }
        )

        initToolbar()
        initContent(content)

        // Only check for contacts if we have the permission, otherwise we'll use the empty list instead
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactsViewModel.fetchLocalContacts()
        } else {
            // If no contacts can be found no sections are made (no callback), so we add them manually
            createTimelineSections(content, state.contacts)
        }

        contactsViewModel.localContactsLiveDataItem.observe(
            viewLifecycleOwner, {
                contactNames = contactsViewModel.getLocalContactNames()
                createTimelineSections(content, state.contacts)
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val state = getState()
        if (state.contacts.isNotEmpty()) {
            state.addToBundle(outState)
        }
        super.onSaveInstanceState(outState)
    }

    private fun initToolbar() {
        binding.toolbar.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }
    }

    private fun initContent(content: Section) {
        content.addAll(
            listOf(
                createHeader(),
                ParagraphItem(
                    getString(R.string.selfbco_timeline_summary), clickable = true
                ),
                LinkItem(getString(R.string.selfbco_timeline_summary_more), ::onMoreInfoClicked),
                MemoryTipOrangeItem()
            )
        )
        adapter.add(content)

        binding.content.adapter = adapter

        setFooterForContent(content)
    }

    private fun createHeader(): StringHeaderItem {
        return StringHeaderItem(
            String.format(
                getString(R.string.selfbco_timeline_title),
                firstDayInTimeLine.toString(DateFormats.selfBcoDateCheck)
            )
        ).also {
            header = it
        }
    }

    private fun addExtraDay(content: Section) {

        val newSymptomOnsetDate = selfBcoViewModel.getDateOfSymptomOnset().minusDays(1)

        selfBcoViewModel.updateDateOfSymptomOnset(newSymptomOnsetDate)

        firstDayInTimeLine = selfBcoViewModel.getStartOfContagiousPeriod()

        val section = createTimelineSection(
            date = firstDayInTimeLine,
            startDate = newSymptomOnsetDate,
            flowType = selfBcoViewModel.getTypeOfFlow()
        )
        for (existingSection in sections) {
            existingSection.refreshHeader(newSymptomOnsetDate)
        }
        sections.add(section)
        content.add(section)
        setHeaderForContent(content)
        setFooterForContent(content)
        binding.content.smoothScrollToPosition(adapter.itemCount)
    }

    private fun createTimelineSections(content: Section, contacts: List<State.Contact>) {
        val days = Days.daysBetween(firstDayInTimeLine, LocalDate.now())
        var memoryItemAdded = false
        var daysAdded = 0
        for (i in days.days downTo 0) {
            daysAdded++
            val date = firstDayInTimeLine.plusDays(i)
            val section = createTimelineSection(
                date = date,
                startDate = selfBcoViewModel.getStartDate(),
                flowType = selfBcoViewModel.getTypeOfFlow()
            )

            val contactsForSection = contacts.filter {
                it.date == date.toString(DateFormats.dateInputData)
            }
            for (contact in contactsForSection) {
                section.addContactToTimeline(
                    name = contact.name,
                    uuid = contact.uuid,
                    focusOnBind = false
                )
            }
            sections.add(section)
            content.add(section)
            // Add tip after 4 days, or at the end if there are less than 4
            if (daysAdded == 3) {
                content.add(MemoryTipGrayItem())
                memoryItemAdded = true
            }
        }
        if (!memoryItemAdded) {
            // Add memory tip after original timeline items
            content.add(MemoryTipGrayItem())
        }
    }

    private fun createTimelineSection(
        date: LocalDate,
        startDate: LocalDate,
        flowType: Int
    ): TimelineSection {
        return TimelineSection(
            date = date,
            contactNames = contactNames.toTypedArray(),
            startDate = startDate,
            flowType = flowType
        ) { uuid ->
            uuid?.let {
                // when a contact already has an uuid it means that it was already added
                // to the case before so it needs to be removed
                selfBcoViewModel.removeContact(it)
            }
        }
    }

    private fun setHeaderForContent(content: Section) {
        content.remove(header)
        content.add(0, createHeader())
    }

    private fun setFooterForContent(content: Section) {
        val groups = mutableListOf<Group>()
        if (selfBcoViewModel.getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
            groups.add(
                SubHeaderItem(
                    getString(
                        R.string.selfbco_timeline_extra_day_header,
                        selfBcoViewModel.getDateOfSymptomOnset()
                            .toString(DateFormats.selfBcoDateOnly)
                    )
                )
            )
            groups.add(
                ButtonItem(
                    getString(R.string.selfbco_add_extra_day),
                    { addExtraDay(content) },
                    type = ButtonType.LIGHT
                )
            )
        }
        groups.add(
            ButtonItem(
                getString(R.string.ready), {
                    checkInput()
                }, type = ButtonType.DARK
            )
        )
        content.setFooter(Section(groups))
    }

    private fun onMoreInfoClicked() {
        saveInput()
        findNavController().navigate(
            TimelineFragmentDirections.toSelfBcoPermissionExplanationFragment()
        )
    }

    private fun checkInput() {
        var filledInAll = true
        val dates = ArrayList<String>()
        sections.forEach {
            if (it.getContactItems().isEmpty()) {
                dates.add(it.date.toString(DateFormats.selfBcoDateCheck))
                filledInAll = false
            }
        }
        if (!filledInAll) {
            showEmptySectionsWarning(dates)
        } else {
            handleInput()
        }
    }

    private fun showEmptySectionsWarning(dates: List<String>) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.selfbco_timeline_error_header))
        builder.setCancelable(false)
        builder.setMessage(
            String.format(
                getString(R.string.selfbco_timeline_error_message),
                dates.asReversed().joinToString(separator = "\n")
            )
        )
        builder.setPositiveButton(
            getString(R.string.str_continue)
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            handleInput()

        }
        builder.setNegativeButton(R.string.back) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun handleInput() {
        markOnboardingAsComplete()
        saveInput()
        findNavController().navigate(TimelineFragmentDirections.toMyContactsFragment())
    }

    private fun markOnboardingAsComplete() {
        LocalStorageRepository.getInstance(requireContext())
            .getSharedPreferences()
            .edit()
            .putBoolean(Constants.USER_COMPLETED_ONBOARDING, true)
            .apply()
    }

    private fun saveInput() {
        getState().contacts.forEach { contact ->
            selfBcoViewModel.addContact(
                contact.name,
                contact.date,
                category = null
            )
        }
    }

    private fun getState(): State {
        val contacts = mutableListOf<State.Contact>()
        sections.forEach { section ->
            for (contact in section.getContactItems()) {
                contacts.add(
                    State.Contact(
                        contact.contactName,
                        contact.contactUuid,
                        section.date.toString(DateFormats.dateInputData),
                    )
                )
            }
        }
        return State(contacts)
    }

    private data class State(
        val contacts: List<Contact>
    ) : Serializable {

        fun addToBundle(bundle: Bundle) {
            bundle.putSerializable(STATE_KEY, this)
        }

        data class Contact(
            val name: String,
            val uuid: String?,
            val date: String
        ) : Serializable

        companion object {
            private const val STATE_KEY = "TimelineFragment_State"

            fun fromBundle(bundle: Bundle?): State? {
                return bundle?.getSerializable(STATE_KEY) as? State
            }
        }
    }
}