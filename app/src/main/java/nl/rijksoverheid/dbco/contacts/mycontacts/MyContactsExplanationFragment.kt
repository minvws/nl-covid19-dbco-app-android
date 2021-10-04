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
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsExplanationBinding

class MyContactsExplanationFragment : BaseFragment(R.layout.fragment_my_contacts_explanation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyContactsExplanationBinding.bind(view)

        binding.btnNext.setOnClickListener {
            findNavController().navigate(
                MyContactsExplanationFragmentDirections.toMyContactsFragment()
            )
        }
    }
}
