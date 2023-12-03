package ru.scisolutions.scicmscore.persistence.entity

import ru.scisolutions.scicmscore.model.LifecycleSpec
import ru.scisolutions.scicmscore.model.State
import ru.scisolutions.scicmscore.model.bpmn.BpmnDefinitions
import ru.scisolutions.scicmscore.model.bpmn.BpmnSequenceFlow
import ru.scisolutions.scicmscore.model.bpmn.BpmnTask
import ru.scisolutions.scicmscore.util.Jaxb
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Transient

@Entity
@Table(name = "core_lifecycles")
class Lifecycle(
    @Column(nullable = false)
    var name: String,

    @Column(name = "display_name")
    var displayName: String? = name,

    var description: String? = null,
    var icon: String? = null,
    var implementation: String? = null,
    var spec: String,
    var checksum: String? = null,
    var hash: String? = null
) : AbstractEntity() {
    @Transient
    private var parsedSpec: LifecycleSpec? = null

    fun parseSpec(): LifecycleSpec {
        if (parsedSpec == null) {
            val bpmnDefinitions: BpmnDefinitions = Jaxb.readXml(spec)
            val process = bpmnDefinitions.bpmnProcess
            val taskMap = process.tasks.associateBy { it.id }
            val sequenceFlowMap = process.sequenceFlows.associateBy { it.id }

            val startTransitions = process.startEvent.outgoings.asSequence()
                .map { sequenceFlowId -> sequenceFlowMap[sequenceFlowId] as BpmnSequenceFlow }
                .filter { sequenceFlow -> sequenceFlow.targetRef != process.endEvent.id }
                .map { sequenceFlow ->
                    val targetTask = taskMap[sequenceFlow.targetRef] as BpmnTask
                    targetTask.name
                }
                .toSet()

            val states = process.tasks.associate { task ->
                task.name to State(
                    task.outgoings.asSequence()
                        .map { sequenceFlowId -> sequenceFlowMap[sequenceFlowId] as BpmnSequenceFlow }
                        .filter { sequenceFlow -> sequenceFlow.targetRef != process.endEvent.id }
                        .map { sequenceFlow ->
                            val targetTask = taskMap[sequenceFlow.targetRef] as BpmnTask
                            targetTask.name
                        }
                        .toSet()
                )
            }

            parsedSpec = LifecycleSpec(
                startEvent = State(startTransitions),
                states = states
            )
        }

        return parsedSpec as LifecycleSpec
    }

    override fun toString(): String = "Lifecycle(name=$name)"

    companion object {
        const val DEFAULT_LIFECYCLE_ID: String = "ad051120-65cf-440a-8fc3-7a24ac8301d3"
    }
}
