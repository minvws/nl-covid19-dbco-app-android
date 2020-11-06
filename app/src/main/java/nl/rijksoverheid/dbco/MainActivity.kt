/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.play.core.install.model.AppUpdateType
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
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.user.UserRepository

private const val RC_UPDATE_APP = 1

class MainActivity : AppCompatActivity() {

    private var factory: ViewModelFactory? = null
    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
    }

    override fun onResume() {
        super.onResume()
        appLifecycleViewModel.checkForForcedAppUpdate()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        if (factory != null) {
            return factory as ViewModelFactory
        }
        if (!BuildConfig.USER_TEST) {
            factory = ViewModelFactory(
                baseContext,
                TasksRepository(this),
                ContactsRepository(this),
                QuestionnareRepository(this),
                UserRepository(this),
                AppConfigRepository(this)
            )
        } else {
            factory = ViewModelFactory(
                baseContext,
                UsertestTaskRepository(this),
                ContactsRepository(this),
                UsertestQuestionnaireRepository(this),
                UsertestUserRepository(this),
                AppConfigRepository(this)
            )
        }
        return factory as ViewModelFactory

    }

}