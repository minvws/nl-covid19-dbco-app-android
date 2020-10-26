/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data.entity

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.entity.Case

@Serializable
data class TasksResponse(val case: Case? = null)