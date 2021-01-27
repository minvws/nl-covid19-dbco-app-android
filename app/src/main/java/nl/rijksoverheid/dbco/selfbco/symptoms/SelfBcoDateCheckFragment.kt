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
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentSelfbcoDateCheckBindingImpl
import nl.rijksoverheid.dbco.util.getDate
import java.util.*

/**
 * Handles both date checking for testing and symptoms
 */
class SelfBcoDateCheckFragment : BaseFragment(R.layout.fragment_selfbco_date_check) {

    private val args: SelfBcoDateCheckFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSelfbcoDateCheckBindingImpl.bind(view)

        when(args.dateCheckingFlow){
            SYMPTOM_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_symptoms_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_symptoms_summary)
                binding.btnNotSure.visibility = View.VISIBLE
                binding.btnNotSure.setOnClickListener {
                    Toast.makeText(context, "Open nieuw scherm, tekst was nog niet bekend", Toast.LENGTH_SHORT).show()
                }
            }

            COVID_CHECK_FLOW -> {
                binding.selfBcoDateHeader.text = getString(R.string.selfbco_date_covid_title)
                binding.selfBcoDateSummary.text = getString(R.string.selfbco_date_covid_summary)
            }
        }


        binding.datePicker.maxDate = System.currentTimeMillis()

        binding.btnNext.setOnClickListener {
            val dateSelected = binding.datePicker.getDate()
            findNavController().navigate(SelfBcoDateCheckFragmentDirections.toSelfBcoDoubleCheckFragment(args.dateCheckingFlow, dateSelected.time))
        }

    }


    companion object {
        const val SYMPTOM_CHECK_FLOW = 0
        const val COVID_CHECK_FLOW = 1
    }

}