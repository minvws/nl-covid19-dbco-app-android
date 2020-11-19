/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

abstract class BaseFragment @JvmOverloads constructor(
    @LayoutRes layout: Int
) : Fragment(layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return requireActivity().defaultViewModelProviderFactory
    }

    fun showErrorDialog(message: String, tryAgainAction: () -> Unit, throwable: Throwable? = null) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.error)
        builder.setCancelable(false)
        var finalMessage = message
        throwable?.let {
            finalMessage += "\n" + it.message
        }

        builder.setMessage(finalMessage)
        builder.setPositiveButton(
            R.string.error_try_again
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
            tryAgainAction.invoke()
        }
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}