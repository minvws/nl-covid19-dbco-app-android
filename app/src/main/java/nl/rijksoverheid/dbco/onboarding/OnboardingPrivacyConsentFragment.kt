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
import java.io.Serializable

@ExperimentalSerializationApi
class OnboardingPrivacyConsentFragment : BaseFragment(R.layout.fragment_onboarding_privacy) {

    private lateinit var binding: FragmentOnboardingPrivacyBinding

    private val viewModel: OnboardingConsentViewModel by viewModels()

    private val args: OnboardingPrivacyConsentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOnboardingPrivacyBinding.bind(view)

        val isChecked = State.fromBundle(savedInstanceState)?.isChecked ?: false

        initToolbar()
        initContent(isChecked)

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

    override fun onSaveInstanceState(outState: Bundle) {
        getState()?.addToBundle(outState)
        super.onSaveInstanceState(outState)
    }

    private fun initToolbar() {
        binding.backButton.setOnClickListener { findNavController().popBackStack() }
        binding.backButton.visibility = if (args.canGoBack) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun initContent(isChecked: Boolean) {
        val content = Section(
            listOf(
                HeaderItem(R.string.onboarding_privacy_title),
                ParagraphItem(getString(R.string.onboarding_privacy_summary), clickable = true),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item1)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item2)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item3)),
                ParagraphIconItem(getString(R.string.onboarding_privacy_item4)),
                PrivacyConsentItem(isChecked) { checked ->
                    binding.btnNext.isEnabled = checked
                }
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
        binding.btnNext.isEnabled = isChecked
    }

    private fun getState(): State? {
        return if (::binding.isInitialized) {
            State(binding.btnNext.isEnabled)
        } else null
    }

    private data class State(
        val isChecked: Boolean
    ) : Serializable {

        fun addToBundle(bundle: Bundle) {
            bundle.putSerializable(STATE_KEY, this)
        }

        companion object {
            private const val STATE_KEY = "OnboardingPrivacyConsentFragment_State"

            fun fromBundle(bundle: Bundle?): State? {
                return bundle?.getSerializable(STATE_KEY) as? State
            }
        }
    }
}