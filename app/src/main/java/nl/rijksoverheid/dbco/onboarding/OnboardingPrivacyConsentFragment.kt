/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.about.faq.FAQItemDecoration
import nl.rijksoverheid.dbco.databinding.FragmentOnboardingPrivacyBinding
import nl.rijksoverheid.dbco.items.input.PrivacyConsentItem
import nl.rijksoverheid.dbco.items.ui.HeaderItem
import nl.rijksoverheid.dbco.items.ui.ParagraphIconItem
import nl.rijksoverheid.dbco.items.ui.ParagraphItem
import nl.rijksoverheid.dbco.util.observeInLifecycle
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel.Navigation.MyTasks
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel.Navigation.Symptoms
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel.Navigation.AddContacts

@ExperimentalSerializationApi
class OnboardingPrivacyConsentFragment : BaseFragment(R.layout.fragment_onboarding_privacy) {

    private val viewModel by viewModels<OnboardingConsentViewModel>()

    private val args: OnboardingPrivacyConsentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnboardingPrivacyBinding.bind(view)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        val content = Section(
            listOf(
                HeaderItem(R.string.onboarding_privacy_title),
                ParagraphItem(getString(R.string.onboarding_privacy_summary), clickable = true),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item1)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item2)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item3)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item4)),
                PrivacyConsentItem(viewModel)
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

        binding.btnNext.setOnClickListener { viewModel.onNextClicked() }

        binding.backButton.setOnClickListener { findNavController().popBackStack() }

        binding.backButton.visibility = if (args.canGoBack) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        viewModel.navigationFlow
            .onEach {
                val direction = when (it) {
                    Symptoms -> OnboardingPrivacyConsentFragmentDirections.toSymptomSelectionFragment()
                    AddContacts -> OnboardingPrivacyConsentFragmentDirections.toSelfBcoPermissionFragment()
                    MyTasks -> OnboardingPrivacyConsentFragmentDirections.toMyContactsFragment()
                }
                findNavController().navigate(direction)
            }
            .observeInLifecycle(viewLifecycleOwner)
    }
}