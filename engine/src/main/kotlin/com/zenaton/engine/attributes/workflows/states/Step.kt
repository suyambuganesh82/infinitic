package com.zenaton.engine.attributes.workflows.states

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zenaton.engine.attributes.delays.DelayId
import com.zenaton.engine.attributes.events.EventId
import com.zenaton.engine.attributes.tasks.TaskId
import com.zenaton.engine.attributes.workflows.WorkflowId

data class Step(
    val hash: String,
    val criterion: StepCriterion,
    var propertiesAfterCompletion: Properties?
) {
    @JsonIgnore
    fun isCompleted() = criterion.isCompleted()

    fun completeTask(taskId: TaskId, properties: Properties): Boolean {
        return complete(ActionId(taskId), properties)
    }

    fun completeWorkflow(workflowId: WorkflowId, properties: Properties): Boolean {
        return complete(ActionId(workflowId), properties)
    }

    fun completeDelay(delayId: DelayId, properties: Properties): Boolean {
        return complete(ActionId(delayId), properties)
    }

    fun completeEvent(eventId: EventId, properties: Properties): Boolean {
        return complete(ActionId(eventId), properties)
    }

    private fun complete(actionId: ActionId, properties: Properties): Boolean {
        if (! isCompleted()) {
            criterion.complete(actionId)
            if (criterion.isCompleted()) {
                propertiesAfterCompletion = properties.copy()
                return true
            }
        }
        return false
    }
}
