/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.symptoms

import android.content.Context
import nl.rijksoverheid.dbco.R
import org.joda.time.LocalDate
import java.io.Serializable

/**
 * State for [SelfBcoDateCheckFragment]
 */
data class SelfBcoDateCheckState(
    val title: String,
    val summary: String,
    val showExplanation: Boolean = false,
    val nextAction: (LocalDate, LocalDate) -> SelfBcoDateCheckNavigation
) : Serializable {

    companion object {

        private const val TWO_WEEKS = 7 * 2

        /**
         * State when selecting EZD after choosing symptoms
         */
        fun createSymptomState(context: Context): SelfBcoDateCheckState = SelfBcoDateCheckState(
            title = context.getString(R.string.selfbco_date_symptoms_title),
            summary = context.getString(R.string.selfbco_date_symptoms_summary),
            showExplanation = true,
            nextAction = { date, now ->
                if (date.isBefore(now.minusDays(TWO_WEEKS))) {
                    SelfBcoDateCheckNavigation.ChronicSymptomCheck
                } else {
                    SelfBcoDateCheckNavigation.SymptomDateDoubleCheck
                }
            }
        )

        /**
         * State when selecting test date after specifying a lack of symptoms
         */
        fun createTestState(context: Context): SelfBcoDateCheckState = SelfBcoDateCheckState(
            title = context.getString(R.string.selfbco_date_covid_title),
            summary = context.getString(R.string.selfbco_date_covid_summary),
            nextAction = { _, _ -> SelfBcoDateCheckNavigation.SymptomDateDoubleCheck }
        )

        /**
         * State when selecting last negative COVID test date
         */
        fun createNegativeTestState(context: Context): SelfBcoDateCheckState =
            SelfBcoDateCheckState(
                title = context.getString(R.string.selfbco_date_negative_covid_title),
                summary = context.getString(R.string.selfbco_date_negative_covid_summary),
                nextAction = { _, _ -> SelfBcoDateCheckNavigation.PermissionCheck }
            )

        /**
         * State when selecting last negative COVID test date with the knowledge that the index
         * has chronic symptoms
         */
        fun createNegativeTestWithChronicSymptomsState(context: Context): SelfBcoDateCheckState =
            createNegativeTestState(context).copy(
                nextAction = { date, now ->
                    if (date.isBefore(now.minusDays(TWO_WEEKS))) {
                        SelfBcoDateCheckNavigation.SymptomsWorsenedCheck
                    } else {
                        SelfBcoDateCheckNavigation.PermissionCheck
                    }
                }
            )

        /**
         * State when selecting positive test date with the knowledge that the index
         * has chronic symptoms
         */
        fun createSymptomTestState(context: Context): SelfBcoDateCheckState = SelfBcoDateCheckState(
            title = context.getString(R.string.selfbco_date_symptoms_positive_test_title),
            summary = context.getString(R.string.selfbco_date_symptoms_positive_test_summary),
            nextAction = { _, _ -> SelfBcoDateCheckNavigation.PermissionCheck }
        )

        /**
         * State when selecting the date symptoms started increasing with the knowledge that the index
         * has chronic symptoms
         */
        fun createSymptomIncreasedState(context: Context): SelfBcoDateCheckState =
            SelfBcoDateCheckState(
                title = context.getString(R.string.selfbco_date_symptoms_increased_title),
                summary = context.getString(R.string.selfbco_date_symptoms_increased_summary),
                nextAction = { _, _ -> SelfBcoDateCheckNavigation.PermissionCheck }
            )
    }
}