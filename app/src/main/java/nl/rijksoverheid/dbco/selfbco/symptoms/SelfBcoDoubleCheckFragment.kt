/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.symptoms

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.picker.ContactPickerPermissionFragment
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDoublecheckBindingImpl
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import org.joda.time.DateTime

class SelfBcoDoubleCheckFragment : BaseFragment(R.layout.fragment_selfbco_doublecheck) {

    private val args: SelfBcoDoubleCheckFragmentArgs by navArgs()
    private val selfBcoViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            SelfBcoCaseViewModel::class.java
        )
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoDoublecheckBindingImpl.bind(view)


        val selectedDate = DateTime(args.dateSelected).minusDays(1)
        when(args.dateCheckingFlow){
            SelfBcoDateCheckFragment.SYMPTOM_CHECK_FLOW -> {
                binding.datecheckHeader.text = String.format(getString(R.string.selfbco_checkdate_symptoms_title), selectedDate.toString(
                    DateFormats.selfBcoDateCheck))
                binding.datecheckSubtext.text = getString(R.string.selfbco_checkdate_summary)
            }

            SelfBcoDateCheckFragment.COVID_CHECK_FLOW -> {
                binding.datecheckHeader.text = getString(R.string.selfbco_checkdate_covid_title)
                binding.datecheckSubtext.text = getString(R.string.selfbco_checkdate_summary)
            }
        }

        binding.btnHadSymptoms.setOnClickListener {
            findNavController().navigate(SelfBcoDoubleCheckFragmentDirections.toSymptomSelectionFragment())
        }

        binding.btnNext.setOnClickListener {
            selfBcoViewModel.generateSelfBcoCase(DateTime(args.dateSelected))

            findNavController().navigate(SelfBcoDoubleCheckFragmentDirections.toContactPickerPermission(null,ContactPickerPermissionFragment.SELF_BCO_FLOW))
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


    }
}