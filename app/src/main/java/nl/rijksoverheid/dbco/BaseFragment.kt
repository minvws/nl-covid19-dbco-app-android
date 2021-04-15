/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.util.hideKeyboard

abstract class BaseFragment constructor(@LayoutRes layout: Int) : Fragment(layout) {

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return requireActivity().defaultViewModelProviderFactory
    }

    fun showErrorDialog(message: String, tryAgainAction: () -> Unit, throwable: Throwable? = null) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.error)
        builder.setCancelable(true)
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

    fun showProgressDialog(message: Int, dismissAction: (() -> Unit)? = null): AlertDialog {
        return showProgressDialog(getString(message), dismissAction)
    }

    private fun showProgressDialog(
        message: String,
        dismissAction: (() -> Unit)? = null
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setCancelable(false)

        val view = layoutInflater.inflate(R.layout.view_loading, null)
        view.findViewById<TextView>(R.id.message).text = message
        builder.setView(view)

        builder.setOnDismissListener {
            dismissAction?.invoke()
        }

        val alert = builder.create()
        alert.show()

        return alert
    }

    override fun onPause() {
        super.onPause()
        view?.hideKeyboard()
        view?.clearFocus()
    }
}