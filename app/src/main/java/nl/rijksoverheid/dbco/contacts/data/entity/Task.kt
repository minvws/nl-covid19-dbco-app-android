package nl.rijksoverheid.dbco.contacts.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val taskType: String? = null,
    val taskContext: String? = null,
    val source: String? = null,
    val label: String? = null,
    val category: String? = null,
    val communication: String? = null,
    val uuid: String? = null
)

