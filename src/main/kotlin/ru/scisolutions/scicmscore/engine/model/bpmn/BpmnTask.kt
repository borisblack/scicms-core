package ru.scisolutions.scicmscore.engine.model.bpmn

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement

class BpmnTask(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",

    @get:XmlAttribute(name = "name", required = true)
    var name: String = "",

    @get:XmlElement(name = "incoming", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var incomings: MutableSet<String> = mutableSetOf(),

    @get:XmlElement(name = "outgoing", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var outgoings: MutableSet<String> = mutableSetOf()
)