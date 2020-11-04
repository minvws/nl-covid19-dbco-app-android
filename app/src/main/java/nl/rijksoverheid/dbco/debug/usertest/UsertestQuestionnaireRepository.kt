/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.debug.usertest

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.questionnaire.QuestionnaireInterface

class UsertestQuestionnaireRepository(context: Context) : QuestionnaireInterface {

    private var questionnaireToUse: ContactDetailsResponse? = null

    override suspend fun retrieveQuestionnaires(): ContactDetailsResponse {
        return if (questionnaireToUse == null) {
            val response: ContactDetailsResponse = Json.decodeFromString(
                MOCK_QUESTIONNAIRE
            )
            questionnaireToUse = response
            response
        } else {
            questionnaireToUse!!
        }
    }

    companion object {
        const val MOCK_QUESTIONNAIRE = "\n" +
                "{\n" +
                "    \"questionnaires\": [\n" +
                "        {\n" +
                "            \"uuid\": \"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\n" +
                "            \"taskType\": \"contact\",\n" +
                "            \"questions\": [\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368387\",\n" +
                "                    \"group\": \"classification\",\n" +
                "                    \"questionType\": \"classificationdetails\",\n" +
                "                    \"label\": \"Vragen over jullie ontmoeting\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [\n" +
                "                        {\n" +
                "                            \"category\": \"1\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2a\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2b\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"3\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9770-725467368388\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"contactdetails\",\n" +
                "                    \"label\": \"Contactgegevens\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [\n" +
                "                        {\n" +
                "                            \"category\": \"1\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2a\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2b\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"3\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368389\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"open\",\n" +
                "                    \"label\": \"Beroep\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [\n" +
                "                        {\n" +
                "                            \"category\": \"1\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368390\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"multiplechoice\",\n" +
                "                    \"label\": \"Waar ken je deze persoon van?\",\n" +
                "                    \"description\": null,\n" +
                "                    \"relevantForCategories\": [\n" +
                "                        {\n" +
                "                            \"category\": \"2a\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2b\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"answerOptions\": [\n" +
                "                        {\n" +
                "                            \"label\": \"Ouder\",\n" +
                "                            \"value\": \"Ouder\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Kind\",\n" +
                "                            \"value\": \"Kind\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Broer of zus\",\n" +
                "                            \"value\": \"Broer of zus\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Partner\",\n" +
                "                            \"value\": \"Partner\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Familielid (overig)\",\n" +
                "                            \"value\": \"Familielid (overig)\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Huisgenoot\",\n" +
                "                            \"value\": \"Huisgenoot\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Vriend of kennis\",\n" +
                "                            \"value\": \"Vriend of kennis\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Medestudent of leerling\",\n" +
                "                            \"value\": \"Medestudent of leerling\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Collega\",\n" +
                "                            \"value\": \"Collega\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Gezondheidszorg medewerker\",\n" +
                "                            \"value\": \"Gezondheidszorg medewerker\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Ex-partner\",\n" +
                "                            \"value\": \"Ex-partner\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Overig\",\n" +
                "                            \"value\": \"Overig\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"uuid\": \"37d818ed-9499-4b9a-9771-725467368391\",\n" +
                "                    \"group\": \"contactdetails\",\n" +
                "                    \"questionType\": \"multiplechoice\",\n" +
                "                    \"label\": \"Geldt een of meer van deze dingen voor deze persoon?\",\n" +
                "                    \"description\": \"<ul><li>Student</li><li>70 jaar of ouder</li><li>Gezondheidsklachten of extra gezondheidsrisico's</li><li>Woont in een zorginstelling of asielzoekerscentrum (bijvoorbeeld bejaardentehuis)</li><li>Spreekt slecht of geen Nederlands</li><li>Werkt in de zorg, onderwijs of een contactberoep (bijvoorbeeld kapper)</li></ul>\",\n" +
                "                    \"relevantForCategories\": [\n" +
                "                        {\n" +
                "                            \"category\": \"1\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2a\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"category\": \"2b\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"answerOptions\": [\n" +
                "                        {\n" +
                "                            \"label\": \"Ja, \\u00e9\\u00e9n of meerdere dingen\",\n" +
                "                            \"value\": \"Ja\",\n" +
                "                            \"trigger\": \"communication_staff\"\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"label\": \"Nee, ik denk het niet\",\n" +
                "                            \"value\": \"Nee\",\n" +
                "                            \"trigger\": \"communication_index\"\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    }
}