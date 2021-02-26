/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.install.model.AppUpdateType
import com.scottyab.rootbeer.RootBeer
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleManager
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.dbco.applifecycle.AppUpdateRequiredFragmentDirections
import nl.rijksoverheid.dbco.applifecycle.EndOfLifeFragmentDirections
import nl.rijksoverheid.dbco.applifecycle.config.AppConfigRepository
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestQuestionnaireRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestTaskRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestUserRepository
import nl.rijksoverheid.dbco.lifecycle.EventObserver
import nl.rijksoverheid.dbco.questionnaire.QuestionnareRepository
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.user.UserRepository
import nl.rijksoverheid.dbco.util.hideKeyboard


private const val RC_UPDATE_APP = 1

class MainActivity : AppCompatActivity() {

    private var factory: ViewModelFactory? = null
    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()
    private var userPrefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set FLAG_SECURE to hide content on non-debug builds
        if(!BuildConfig.DEBUG) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        userPrefs = LocalStorageRepository.getInstance(this).getSharedPreferences()

        appLifecycleViewModel.updateEvent.observe(
            this,
            EventObserver {
                when (it) {
                    is AppLifecycleViewModel.AppLifecycleStatus.Update -> {
                        if (it.update is AppLifecycleManager.UpdateState.InAppUpdate) {
                            it.update.appUpdateManager.startUpdateFlowForResult(
                                it.update.appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                RC_UPDATE_APP
                            )
                        } else {
                            val installerPackageName =
                                (it.update as AppLifecycleManager.UpdateState.UpdateRequired).installerPackageName
                            findNavController(R.id.nav_host_fragment).navigate(
                                AppUpdateRequiredFragmentDirections.actionAppUpdateRequired(
                                    installerPackageName
                                )
                            )
                        }
                    }
                    AppLifecycleViewModel.AppLifecycleStatus.EndOfLife -> {
                        findNavController(R.id.nav_host_fragment).navigate(
                            EndOfLifeFragmentDirections.actionEndOfLife()
                        )
                    }
                }
            }
        )

        checkIfRooted()
    }

    private fun checkIfRooted() {
        if (userPrefs?.getBoolean(Constants.USER_SAW_ROOTED_WARNING_KEY, false) == true) {
            return
        }

        if (RootBeer(this).isRooted) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setCancelable(true)
            builder.setMessage(getString(R.string.rooted_device_warning))
            builder.setPositiveButton(
                R.string.close
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
                userPrefs?.edit()?.putBoolean(Constants.USER_SAW_ROOTED_WARNING_KEY, true)?.apply()
            }
            val alert: AlertDialog = builder.create()
            alert.show()
        }
    }

    override fun onResume() {
        super.onResume()
        appLifecycleViewModel.checkForForcedAppUpdate()
    }

    override fun onPause() {
        super.onPause()
        currentFocus?.hideKeyboard()
        currentFocus?.clearFocus()
    }


    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        if (factory != null) {
            return factory as ViewModelFactory
        }
        if (BuildConfig.USER_TEST) {
            val userRepository = UsertestUserRepository(this)
            factory = ViewModelFactory(
                baseContext,
                UsertestTaskRepository(this, userRepository),
                ContactsRepository(this),
                UsertestQuestionnaireRepository(this),
                userRepository,
                AppConfigRepository(this)
            )
        } else {
            val userRepository = UserRepository(this)
            factory = ViewModelFactory(
                baseContext,
                TasksRepository(this, userRepository),
                ContactsRepository(this),
                QuestionnareRepository(this),
                userRepository,
                AppConfigRepository(this)
            )
        }
        return factory as ViewModelFactory

    }

}