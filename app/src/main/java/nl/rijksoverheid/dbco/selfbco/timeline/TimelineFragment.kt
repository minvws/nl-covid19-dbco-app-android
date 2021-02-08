/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.android.synthetic.main.fragment_selfbco_timeline.*
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.faq.FAQItemDecoration
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoTimelineBinding
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoTimelineExplanationBinding
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.items.ui.StringHeaderItem
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.util.hideKeyboard
import nl.rijksoverheid.dbco.util.toDateTimes
import org.joda.time.DateTime
import org.joda.time.Interval
import timber.log.Timber
import java.util.stream.Collectors

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
    lateinit var binding : FragmentSelfbcoTimelineBinding
    lateinit var firstDay : DateTime

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelfbcoTimelineBinding.bind(view)

        contactsViewModel.fetchLocalContacts()
        firstDay = selfBcoViewModel.getDateOfSymptomOnset() ?: DateTime.now().withTimeAtStartOfDay()
        // Set First day to selected date minus 2 days
        firstDay = firstDay.minusDays(2)

        // Add headers before sections get created
        content.addAll(
            listOf(
                StringHeaderItem(
                    String.format(
                        getString(R.string.selfbco_timeline_title),
                        firstDay
                    )
                ),
                ParagraphItem(
                    getString(R.string.selfbco_timeline_summary),
                    clickable = true
                ),
            )
        )

        contactsViewModel.localContactsLiveDataItem.observe(
            viewLifecycleOwner,
            {
                contactNames = contactsViewModel.getLocalContactNames()
                Timber.d("Found names ${contactNames}")

                createTimelineSections()
            })


        adapter.add(content)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.list_spacing)
            )
        )

        binding.btnNext.setOnClickListener {
            checkInput()
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
            it.hideKeyboard()
        }

        binding.btnAddDay.setOnClickListener {
            // Add day to the bottom of the list
            val newDate = firstDay.minusDays(1).withTimeAtStartOfDay()
            firstDay = newDate

            // Unsure if this is required or not, commented out for now
            //selfBcoViewModel.updateDateOfSymptomOnset(newDate)

            val section = TimelineSection(firstDay, contactNames.toTypedArray())
            sections.add(section)
            content.add(section)
            binding.content.smoothScrollToPosition(adapter.itemCount)
        }


    }

    fun createTimelineSections() {
        val interval = Interval(firstDay, DateTime.now())
        interval.toDateTimes().toList().reversed().forEach {
            Timber.d("Adding timeline item for $it")
            val section = TimelineSection(it.withTimeAtStartOfDay(), contactNames.toTypedArray())
            sections.add(section)
            content.add(section)

        }
    }
    
    private fun checkInput(){
        var filledInAll = true
        val dates = ArrayList<String>()
        sections.forEach {
            if(it.items.size == 0){
                dates.add(it.date.toString(DateFormats.selfBcoDateCheck))
                filledInAll = false
            }
        }
        
        if(!filledInAll) {

            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.selfbco_timeline_error_header))
            builder.setCancelable(false)
            builder.setMessage(String.format(getString(R.string.selfbco_timeline_error_message),dates.asReversed().joinToString()))
            builder.setPositiveButton(
                getString(R.string.str_continue)
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()

            }
            builder.setNegativeButton(R.string.cd_back) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }
    }
}