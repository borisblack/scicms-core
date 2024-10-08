package ru.scisolutions.scicmscore.engine.model.bpmn

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement

class BpmnEndEvent(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",
    @get:XmlElement(name = "incoming", namespace = BpmnDefinitions.BPMN_NAMESPACE, required = true)
    var incomings: MutableSet<String> = mutableSetOf()
)
