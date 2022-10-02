package ru.scisolutions.scicmscore.model.bpmn

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

class BpmnProcess(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",

    @get:XmlAttribute(name = "isExecutable")
    var isExecutable: Boolean = false,

    @get:XmlElement(name = "startEvent", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var startEvent: BpmnStartEvent = BpmnStartEvent(),

    @get:XmlElement(name = "endEvent", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var endEvent: BpmnEndEvent = BpmnEndEvent(),

    @get:XmlElement(name = "task", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var tasks: MutableList<BpmnTask> = mutableListOf(),

    @get:XmlElement(name = "sequenceFlow", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var sequenceFlows: MutableList<BpmnSequenceFlow> = mutableListOf()
)