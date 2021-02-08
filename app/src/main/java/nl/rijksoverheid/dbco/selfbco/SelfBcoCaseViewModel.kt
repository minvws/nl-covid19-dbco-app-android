/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.Resource
import org.joda.time.DateTime
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class SelfBcoCaseViewModel(private val tasksRepository: ITaskRepository) : ViewModel() {

    private var dateOfSymptomOnset : DateTime? = null
    private val indexSymptoms = ArrayList<String>()

    fun generateSelfBcoCase(dateOfSymptomOnset: DateTime) {
        this.dateOfSymptomOnset = dateOfSymptomOnset
        tasksRepository.generateSelfBcoCase(dateOfSymptomOnset.toString(DateFormats.dateInputData))
    }

    fun addSymptom(symptom : String){
        if(!indexSymptoms.contains(symptom)){
            indexSymptoms.add(symptom)
            Timber.d("Added symptom $symptom")
        }
    }

    fun removeSymptom(symptom: String){
        if(indexSymptoms.contains(symptom)){
            indexSymptoms.remove(symptom)
            Timber.d("Removed symptom $symptom")
        }
    }

    fun addRoommate(name : String){
        val roommateTask = Task(taskType = "contact", source = Source.App,category = Category.LIVED_TOGETHER, label = name, uuid = UUID.randomUUID().toString())
        tasksRepository.saveChangesToTask(roommateTask)
    }

    fun getOnsetAsFormattedString() : String{
        return dateOfSymptomOnset?.toString(DateFormats.selfBcoDateCheck) ?: ""
    }

    fun getDateOfSymptomOnset(): DateTime? {
        return dateOfSymptomOnset
    }

    fun updateDateOfSymptomOnset(newDateTime: DateTime){
        dateOfSymptomOnset = newDateTime
    }






}