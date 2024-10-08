package ru.scisolutions.scicmscore.engine.model.bpmn

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement

class BpmnStartEvent(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",
    @get:XmlElement(name = "outgoing", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var outgoings: MutableSet<String> = mutableSetOf()
)
