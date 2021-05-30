package nl.rijksoverheid.dbco.network.request

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.bcocase.data.entity.TaskType

@Serializable
data class TaskRequest(
    val taskType: TaskType? = null,
    var taskContext: String? = null,
    val source: Source? = null,
    var label: String? = null,
    var category: Category? = null,
    var communication: CommunicationType? = null,
    var uuid: String? = null,
    var dateOfLastExposure: String? = null,
    var questionnaireResult: QuestionnaireResult? = null,
    var informedByIndexAt: String? = null,
) {

    companion object {

        fun fromTask(task: Task): TaskRequest = TaskRequest(
            taskType = task.taskType,
            taskContext = task.taskContext,
            source = task.source,
            label = task.label,
            category = task.category,
            communication = task.communication,
            uuid = task.uuid,
            dateOfLastExposure = task.dateOfLastExposure,
            questionnaireResult = task.questionnaireResult,
            informedByIndexAt = task.informedByIndexAt
        )
    }
}