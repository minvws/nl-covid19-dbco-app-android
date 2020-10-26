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
import nl.rijksoverheid.dbco.questionnary.QuestionnaryRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var factory: ViewModelFactory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        Timber.d("Retrieving viewmodel factory")
        if (factory != null) {
            Timber.d("Retrieving viewmodel factory as old")
            return factory as ViewModelFactory
        }
        factory = ViewModelFactory(
            TasksRepository(this),
            ContactsRepository(this),
            QuestionnaryRepository(this)
        )
        Timber.d("Retrieving viewmodel factory as new")
        return factory as ViewModelFactory

    }

}