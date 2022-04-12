/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.scottyab.rootbeer.RootBeer
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.ConfigError
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.Update
import nl.rijksoverheid.dbco.bcocase.CaseRepository
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.config.AppUpdateRequiredFragmentDirections
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.questionnaire.QuestionnaireRepository
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.user.UserRepository
import nl.rijksoverheid.dbco.util.hideKeyboard
import java.util.*


class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    private var factory: ViewModelFactory? = null
    private var configError: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.FEATURE_SECURE_WINDOW) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        appViewModel.updateEvent.observe(this, { updateEvent ->
            handleUpdateEvent(updateEvent)
        })

        checkIfRooted()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.fetchConfig()
    }

    override fun onPause() {
        super.onPause()
        currentFocus?.hideKeyboard()
        currentFocus?.clearFocus()
    }

    @Suppress("DEPRECATION")
    override fun attachBaseContext(newBase: Context) {
        val locale = Locale("nl") // force app language to NL
        var context = newBase
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            configuration.locale = locale
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context = context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        super.attachBaseContext(ContextWrapper(context))
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        if (factory != null) {
            return factory as ViewModelFactory
        }
        val userRepository = UserRepository(this)
        val storage = LocalStorageRepository.getInstance(baseContext).getSharedPreferences()
        val api = DbcoApi.create(baseContext)
        return ViewModelFactory(
            baseContext,
            CaseRepository(this, userRepository),
            ContactsRepository(this),
            QuestionnaireRepository(storage, api),
            userRepository,
            AppConfigRepository(this, api, storage),
            LocalStorageRepository.getInstance(baseContext).getSharedPreferences()
        ).also {
            factory = it
        }
    }

    private fun handleUpdateEvent(updateEvent: AppLifecycleStatus) {
        configError?.dismiss()
        configError = null
        when (updateEvent) {
            is Update -> {
                findNavController(R.id.nav_host_fragment).navigate(
                    AppUpdateRequiredFragmentDirections.actionAppUpdateRequired(
                        updateEvent.installerPackageName
                    )
                )
            }
            is ConfigError -> {
                configError = showErrorMessage(
                    title = R.string.invalid_config_warning_title,
                    message = R.string.invalid_config_warning_message,
                    positiveButton = R.string.invalid_config_warning_positive_button,
                    negativeButton = R.string.invalid_config_warning_negative_button,
                    positiveAction = { appViewModel.fetchConfig() },
                    closeAction = { finish() }
                )
            }
            else -> { /* NO-OP*/
            }
        }
    }

    private fun checkIfRooted() {
        if (RootBeer(this).isRooted) {
            showErrorMessage(
                title = R.string.rooted_device_warning_title,
                message = R.string.rooted_device_warning_message,
                positiveButton = R.string.rooted_device_warning_positive_button,
                negativeButton = R.string.close,
                positiveAction = {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.rooted_device_warning_link))
                        )
                    )
                },
                closeAction = { checkIfUnsecureDevice() }
            )
        } else {
            checkIfUnsecureDevice()
        }
    }

    private fun checkIfUnsecureDevice() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyguardManager.isKeyguardSecure) {
            showErrorMessage(
                title = R.string.unsecure_device_warning_title,
                message = R.string.unsecure_device_warning_message,
                positiveButton = R.string.unsecure_device_warning_positive_button,
                negativeButton = R.string.unsecure_device_warning_negative_button,
                positiveAction = {
                    try {
                        startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    } catch (ex: Exception) {
                        startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }
            )
        }
    }

    private fun showErrorMessage(
        @StringRes title: Int,
        @StringRes message: Int,
        @StringRes positiveButton: Int,
        @StringRes negativeButton: Int,
        positiveAction: (() -> Unit),
        closeAction: (() -> Unit)? = null
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this).apply {
            setCancelable(false)
            setTitle(title)
            setMessage(message)
            setPositiveButton(positiveButton) { dialogInterface, _ ->
                positiveAction.invoke()
                dialogInterface.dismiss()
            }
            setNegativeButton(negativeButton) { dialogInterface, _ ->
                dialogInterface.dismiss()
                closeAction?.invoke()
            }
        }.create().also { dialog ->
            dialog.show()
        }
    }
}