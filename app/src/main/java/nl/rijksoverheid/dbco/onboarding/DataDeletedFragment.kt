package nl.rijksoverheid.dbco.onboarding

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentDataDeletedBinding

class DataDeletedFragment : BaseFragment(R.layout.fragment_data_deleted) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDataDeletedBinding.bind(view)

        binding.btnNext.setOnClickListener {
            findNavController().navigate(DataDeletedFragmentDirections.toOnboardingStartFragment())
        }
    }
}