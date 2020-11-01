/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestQuestionnaireRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestTaskRepository
import nl.rijksoverheid.dbco.debug.usertest.UsertestUserRepository
import nl.rijksoverheid.dbco.questionnaire.QuestionnareRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.user.UserRepository

class MainActivity : AppCompatActivity() {

    private var factory: ViewModelFactory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                UserRepository(this)
            )
        } else {
            factory = ViewModelFactory(
                baseContext,
                UsertestTaskRepository(this),
                ContactsRepository(this),
                UsertestQuestionnaireRepository(this),
                UsertestUserRepository(this)
            )
        }
        return factory as ViewModelFactory

    }

}