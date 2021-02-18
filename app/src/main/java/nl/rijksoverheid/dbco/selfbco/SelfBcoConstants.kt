/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco

class SelfBcoConstants {
    companion object {
        const val NOT_SELECTED = -1
        const val SYMPTOM_CHECK_FLOW = 0
        const val COVID_CHECK_FLOW = 1

        val SYMPTOMS = listOf(
            "Neusverkoudheid",
            "Schorre stem",
            "Keelpijn",
            "(licht) hoesten",
            "Kortademigheid/benauwdheid",
            "Pijn bij de ademhaling",
            "Koorts (= boven 38 graden Celsius)",
            "Koude rillingen",
            "Verlies van of verminderde reuk",
            "Verlies van of verminderde smaak",
            "Algehele malaise",
            "Vermoeidheid",
            "Hoofdpijn",
            "Spierpijn",
            "Pijn achter de ogen",
            "Algehele pijnklachten",
            "Duizeligheid",
            "Prikkelbaarheid/verwardheid",
            "Verlies van eetlust",
            "Misselijkheid",
            "Overgeven",
            "Diarree",
            "Buikpijn",
            "Rode prikkende ogen (oogontsteking)",
            "Huidafwijkingen"
        )
    }
}