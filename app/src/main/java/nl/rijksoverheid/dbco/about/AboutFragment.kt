/*
 *   Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *    SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.about

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.faq.FAQItem
import nl.rijksoverheid.dbco.about.faq.FAQItemDecoration
import nl.rijksoverheid.dbco.about.faq.FAQItemId
import nl.rijksoverheid.dbco.databinding.FragmentListBinding

class AboutFragment : BaseFragment(R.layout.fragment_list) {
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.add(AboutContent())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentListBinding.bind(view)

        binding.toolbar.setTitle(getString(R.string.about_app_title))

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        adapter.setOnItemClickListener { item, _ ->
            when (item) {
                is FAQItem -> findNavController().navigate(
                    AboutFragmentDirections.toAboutDetailFragment(
                        item.id
                    )
                )
            }
        }

    }


    class AboutContent : Section(
        listOf(
            FAQItem(FAQItemId.DUMMY, R.string.placeholder),
            FAQItem(FAQItemId.DUMMY, R.string.app_name),
            FAQItem(FAQItemId.DUMMY, R.string.app_name_develop)
        )
    )
}