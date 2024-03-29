/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.mycontacts

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentMycontactsMemoryBinding
import nl.rijksoverheid.dbco.items.ui.*

class MyContactsMemoryTipFragment : BaseFragment(R.layout.fragment_mycontacts_memory) {

    private val args: MyContactsMemoryTipFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMycontactsMemoryBinding.bind(view)
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(initContent())

        binding.content.adapter = adapter
        binding.toolbar.backButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initContent(): Section {
        return Section(
            listOf(
                // Top
                IllustrationItem(R.drawable.illustration_general_user),
                StringHeaderItem(
                    String.format(
                        getString(R.string.mycontacts_memory_header),
                        args.date
                    )
                ),
                ParagraphItem(getString(R.string.mycontacts_memory_summary)),
                // Section 1
                HeaderIconItem(
                    text = getString(R.string.mycontacts_memory_sectionheader1),
                    contentDescriptionPrefix = getString(R.string.contact_section_step_one),
                    icon = R.drawable.ic_section_one
                ),
                ParagraphIconItem(getString(R.string.selfbco_memory_photos), R.drawable.ic_photos),
                ParagraphIconItem(
                    getString(R.string.selfbco_memory_socials),
                    R.drawable.ic_thumbsup
                ),
                ParagraphIconItem(
                    getString(R.string.selfbco_memory_agenda),
                    R.drawable.ic_calendar
                ),
                ParagraphIconItem(getString(R.string.selfbco_memory_pin), R.drawable.ic_creditcard),
                SubHeaderItem(getString(R.string.mycontacts_memory_forgotten)),
                ParagraphIconItem(getString(R.string.selfbco_memory_car), R.drawable.ic_car),
                ParagraphIconItem(getString(R.string.selfbco_memory_meeting), R.drawable.ic_people),
                ParagraphIconItem(
                    getString(R.string.selfbco_memory_work),
                    R.drawable.ic_chatballoons
                ),
                // Section 2
                HeaderIconItem(
                    text = getString(R.string.mycontacts_memory_sectionheader2),
                    contentDescriptionPrefix = getString(R.string.contact_section_step_two),
                    icon = R.drawable.ic_section_two
                ),
                ParagraphItem(getString(R.string.selfbco_timeline_explanation_summary)),
                ParagraphIconItem(
                    getString(R.string.selfbco_timeline_explanation_step1),
                    R.drawable.ic_checkmark_round
                ),
                ParagraphIconItem(
                    getString(R.string.selfbco_timeline_explanation_step2),
                    R.drawable.ic_checkmark_round
                ),
                ParagraphIconItem(
                    getString(R.string.selfbco_timeline_explanation_step3),
                    R.drawable.ic_questionmark_round
                )
            )
        )
    }
}