/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.Group
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.faq.FAQItemDecoration
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

class TimelineFragment : BaseFragment(R.layout.fragment_selfbco_timeline) {

    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }

    private val contactsViewModel by viewModels<ContactsViewModel>()

    val adapter = GroupAdapter<GroupieViewHolder>()
    val content = Section()

    private val sections = ArrayList<TimelineSection>()
    private var contactNames = ArrayList<String>()

    lateinit var header: StringHeaderItem
    lateinit var binding: FragmentSelfbcoTimelineBinding
    lateinit var firstDayInTimeLine: LocalDate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoTimelineBinding.bind(view)
        adapter.clear()

        firstDayInTimeLine = selfBcoViewModel.getStartOfContagiousPeriod()

        content.addAll(
            listOf(
                createHeader(),
                ParagraphItem(
                    getString(R.string.selfbco_timeline_summary),
                    clickable = true
                ),
                MemoryTipOrangeItem()
            )
        )
        adapter.add(content)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.list_spacing)
            )
        )

        setFooterForContent()

        // Only check for contacts if we have the permission, otherwise we'll use the empty list instead
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            contactsViewModel.fetchLocalContacts()
        } else {
            // If no contacts can be found no sections are made (no callback), so we add them manually
            createTimelineSections()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }

        contactsViewModel.localContactsLiveDataItem.observe(
            viewLifecycleOwner, {
                contactNames = contactsViewModel.getLocalContactNames()
                createTimelineSections()
            }
        )
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

    private fun addExtraDay() {

        val newSymptomOnsetDate = selfBcoViewModel.getDateOfSymptomOnset().minusDays(1)

        selfBcoViewModel.updateDateOfSymptomOnset(newSymptomOnsetDate)

        firstDayInTimeLine = selfBcoViewModel.getStartOfContagiousPeriod()

        val section = TimelineSection(
            firstDayInTimeLine,
            contactNames.toTypedArray(),
            newSymptomOnsetDate,
            selfBcoViewModel.getTypeOfFlow()
        )
        for (existingSection in sections) {
            existingSection.refreshHeader(newSymptomOnsetDate)
        }
        sections.add(section)
        content.add(section)
        setHeaderForContent()
        setFooterForContent()
        binding.content.smoothScrollToPosition(adapter.itemCount)
    }

    fun createTimelineSections() {
        val days = Days.daysBetween(firstDayInTimeLine, LocalDate.now())
        var memoryItemAdded = false
        var daysAdded = 0
        for (i in days.days downTo 0) {
            daysAdded++
            val section = TimelineSection(
                date = firstDayInTimeLine.plusDays(i),
                contactNames = contactNames.toTypedArray(),
                startDate = selfBcoViewModel.getStartDate(),
                flowType = selfBcoViewModel.getTypeOfFlow()
            )
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

    private fun setHeaderForContent() {
        content.remove(header)
        content.add(0, createHeader())
    }

    private fun setFooterForContent() {
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
                    { addExtraDay() },
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

    private fun checkInput() {
        var filledInAll = true
        val dates = ArrayList<String>()
        sections.forEach {
            if (it.items.size == 0) {
                dates.add(it.date.toString(DateFormats.selfBcoDateCheck))
                filledInAll = false
            }
        }

        if (!filledInAll) {

            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(getString(R.string.selfbco_timeline_error_header))
            builder.setCancelable(false)
            builder.setMessage(
                String.format(
                    getString(R.string.selfbco_timeline_error_message),
                    dates.asReversed().joinToString()
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
        } else {
            handleInput()
        }
    }

    private fun handleInput() {
        sections.forEach { section ->
            section.items.forEach { contact ->
                selfBcoViewModel.addSelfBcoContact(
                    contact.contactName,
                    section.date.toString(DateFormats.dateInputData),
                    category = null
                )
            }
        }

        findNavController().navigate(TimelineFragmentDirections.toMyContactsFragment())
        val encryptedSharedPreferences: SharedPreferences =
            LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
        encryptedSharedPreferences.edit().putBoolean(Constants.USER_COMPLETED_ONBOARDING, true)
            .apply()
        encryptedSharedPreferences.edit().putBoolean(Constants.USER_LOCAL_CASE, true).apply()
    }
}