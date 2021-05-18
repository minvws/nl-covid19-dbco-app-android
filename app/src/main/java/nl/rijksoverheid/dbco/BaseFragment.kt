/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.dbco.selfbco.onboarding.SelfBcoPermissionFragmentDirections
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

    internal fun requestPermission(requestCallback: ActivityResultLauncher<String>, permission: String, onGranted: (() -> Unit)) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                requestCallback.launch(permission)
            } else {
                activity?.let {
                    val builder = MaterialAlertDialogBuilder(it)
                    builder.setTitle(R.string.permissions_title)
                    builder.setCancelable(false)
                    builder.setMessage(R.string.permissions_some_permissions_denied)
                    builder.setPositiveButton(
                        R.string.permissions_go_to_settings
                    ) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        // Go to app settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", it.packageName, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.startActivity(intent)
                        it.finish()
                    }
                    builder.setNegativeButton(R.string.permissions_no) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    val alert: AlertDialog = builder.create()
                    alert.show()
                }
            }
        } else {
            onGranted()
        }
    }
}