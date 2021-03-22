package nl.rijksoverheid.dbco.contacts.mycontacts

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.faq.FAQItemDecoration
import nl.rijksoverheid.dbco.databinding.FragmentMycontactsMemoryBinding
import nl.rijksoverheid.dbco.items.ui.*

class MyContactsMemoryTipFragment : BaseFragment(R.layout.fragment_mycontacts_memory) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMycontactsMemoryBinding.bind(view)

        val content = Section(
            listOf(
                // Top
                IllustrationItem(R.drawable.illustration_general_user),
                HeaderItem(R.string.mycontacts_memory_header),
                ParagraphItem(getString(R.string.mycontacts_memory_summary)),
                // Section 1
                HeaderIconItem(getString(R.string.mycontacts_memory_sectionheader1), R.drawable.ic_section_one),
                ParagraphIconItem(getString(R.string.selfbco_memory_photos), R.drawable.ic_photos),
                ParagraphIconItem(getString(R.string.selfbco_memory_socials), R.drawable.ic_thumbsup),
                ParagraphIconItem(getString(R.string.selfbco_memory_agenda), R.drawable.ic_calendar),
                ParagraphIconItem(getString(R.string.selfbco_memory_pin), R.drawable.ic_creditcard),
                SubHeaderItem(getString(R.string.mycontacts_memory_forgotten)),
                ParagraphIconItem(getString(R.string.selfbco_memory_car), R.drawable.ic_car),
                ParagraphIconItem(getString(R.string.selfbco_memory_meeting), R.drawable.ic_people),
                ParagraphIconItem(getString(R.string.selfbco_memory_work), R.drawable.ic_chatballoons),
                // Section 2
                HeaderIconItem(getString(R.string.mycontacts_memory_sectionheader2), R.drawable.ic_section_two),
                ParagraphItem(getString(R.string.selfbco_timeline_explanation_summary)),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step1), R.drawable.ic_checkmark_round),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step2), R.drawable.ic_checkmark_round),
                ParagraphIconItem(getString(R.string.selfbco_timeline_explanation_step3), R.drawable.ic_questionmark_round),

                // Section 3
                HeaderIconItem(getString(R.string.mycontacts_memory_sectionheader3), R.drawable.ic_section_three),
                ParagraphItem(getString(R.string.mycontacts_memory_section3_summary))
            )
        )
        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.add(content)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.list_spacing)
            )
        )

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}