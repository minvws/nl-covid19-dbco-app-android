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
import nl.rijksoverheid.dbco.util.toDateTimes
import org.joda.time.DateTime
import org.joda.time.Interval
import timber.log.Timber

class TimelineFragment : BaseFragment(R.layout.fragment_selfbco_timeline) {
    val adapter = GroupAdapter<GroupieViewHolder>()
    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }


    private val sections = ArrayList<TimelineSection>()

    val content = Section()
    private val contactsViewModel by viewModels<ContactsViewModel>()
    private var contactNames = ArrayList<String>()
    lateinit var binding: FragmentSelfbcoTimelineBinding
    lateinit var firstDay: DateTime

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoTimelineBinding.bind(view)

        firstDay = selfBcoViewModel.getDateOfSymptomOnset()
        // Set First day to selected date minus 2 days
        firstDay = firstDay.minusDays(2)

        // Add headers before sections get created
        content.addAll(
            listOf(
                StringHeaderItem(
                    String.format(
                        getString(R.string.selfbco_timeline_title),
                        firstDay.toString(DateFormats.selfBcoDateCheck)
                    )
                ),
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
            viewLifecycleOwner,
            {
                contactNames = contactsViewModel.getLocalContactNames()
                Timber.d("Found names ${contactNames}")

                createTimelineSections()
            })

    }

    private fun addExtraDay() {
        // Add day to the bottom of the list
        val newDate = firstDay.minusDays(1).withTimeAtStartOfDay()
        firstDay = newDate

        // Update date of first symptom when adding extra dates
        selfBcoViewModel.updateDateOfSymptomOnset(newDate)

        val section = TimelineSection(
            firstDay,
            contactNames.toTypedArray(),
            selfBcoViewModel.getDateOfSymptomOnset(),
            selfBcoViewModel.getTypeOfFlow()
        )
        sections.add(section)
        content.add(section)
        binding.content.smoothScrollToPosition(adapter.itemCount)
        setFooterForContent()
    }

    fun createTimelineSections() {
        val interval = Interval(firstDay, DateTime.now())
        var memoryItemAdded = false
        interval.toDateTimes().toList().reversed().forEachIndexed { index, dateTime ->
            Timber.d("Adding timeline item for $dateTime")
            val section = TimelineSection(
                dateTime.withTimeAtStartOfDay(),
                contactNames.toTypedArray(),
                selfBcoViewModel.getDateOfSymptomOnset(),
                selfBcoViewModel.getTypeOfFlow()
            )
            sections.add(section)
            content.add(section)
            // Add tip after 4 days, or at the end if there are less than 4
            if(index == 3) {
                content.add(MemoryTipGrayItem())
                memoryItemAdded = true
            }
        }
        if(!memoryItemAdded) {
            // Add memory tip after original timeline items
            content.add(MemoryTipGrayItem())
        }
    }

    private fun setFooterForContent() {
        val groups = mutableListOf<Group>()
        if (selfBcoViewModel.getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
            groups.add(SubHeaderItem(
                getString(
                    R.string.selfbco_timeline_extra_day_header,
                    firstDay.toString(DateFormats.selfBcoDateOnly)
                )
            ))
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