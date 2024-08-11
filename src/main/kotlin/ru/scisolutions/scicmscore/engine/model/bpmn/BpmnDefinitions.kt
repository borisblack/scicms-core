package ru.scisolutions.scicmscore.engine.model.bpmn

import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "definitions", namespace = BpmnDefinitions.BPMN_NAMESPACE)
class BpmnDefinitions(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",
    @get:XmlElement(name = "process", namespace = BPMN_NAMESPACE, required = true)
    var bpmnProcess: BpmnProcess = BpmnProcess(),
) {
    companion object {
        const val BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL"
    }
}
