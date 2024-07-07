package ru.scisolutions.scicmscore.engine.model.bpmn

import jakarta.xml.bind.annotation.XmlAttribute

class BpmnSequenceFlow(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",

    @get:XmlAttribute(name = "name")
    var name: String? = null,

    @get:XmlAttribute(name = "sourceRef", required = true)
    var sourceRef: String = "",

    @get:XmlAttribute(name = "targetRef", required = true)
    var targetRef: String = ""
)