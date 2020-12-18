/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.dbco.applifecycle

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.dbco.BaseFragment
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.databinding.FragmentAppUpdateRequiredBinding
import timber.log.Timber

private const val APP_GALLERY_PACKAGE = "com.huawei.appmarket"

class AppUpdateRequiredFragment : BaseFragment(R.layout.fragment_app_update_required) {
    private val args: AppUpdateRequiredFragmentArgs by navArgs()
    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppUpdateRequiredBinding.bind(view)

        val updateMessage = appLifecycleViewModel.getUpdateMessage()
        binding.text = updateMessage

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.next.setOnClickListener {
            openAppStore()
        }
    }

    private fun openAppStore() {
        when (args.appStorePackage) {
            APP_GALLERY_PACKAGE -> openAppGallery()
            else -> openPlayStore()
        }
    }

    private fun openAppGallery() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("appmarket://details?id=${BuildConfig.APPLICATION_ID}")
        ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            requireActivity().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Timber.w("Could not open app gallery!")
        }
    }

    private fun openPlayStore() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            requireActivity().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            requireActivity().startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
