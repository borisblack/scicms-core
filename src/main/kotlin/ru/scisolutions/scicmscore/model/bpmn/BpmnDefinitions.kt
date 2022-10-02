package ru.scisolutions.scicmscore.model.bpmn

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "definitions", namespace = BpmnDefinitions.BPMN_NAMESPACE)
class BpmnDefinitions(
    @get:XmlAttribute(name = "id", required = true)
    var id: String = "",

    @get:XmlElement(name = "process", namespace = BPMN_NAMESPACE, required = true)
    var bpmnProcess: BpmnProcess = BpmnProcess()
) {
    companion object {
        const val BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL"
    }
}