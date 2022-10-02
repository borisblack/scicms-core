package ru.scisolutions.scicmscore.util

import java.io.StringReader
import java.io.StringWriter
import javax.xml.bind.JAXBContext
import javax.xml.transform.stream.StreamSource

object Jaxb {
    inline fun <reified T> readXml(xml: String): T {
        val jaxbContext = JAXBContext.newInstance(T::class.java)
        val unmarshaller = jaxbContext.createUnmarshaller()
        val streamSource = StreamSource(StringReader(xml))
        val jaxbElement = unmarshaller.unmarshal(streamSource, T::class.java)

        return jaxbElement.value
    }

    fun writeXml(obj: Any): String {
        val jaxbContext = JAXBContext.newInstance(obj::class.java)
        val marshaller = jaxbContext.createMarshaller()
        val stringWriter = StringWriter()
        marshaller.marshal(obj, stringWriter)

        return stringWriter.toString()
    }
}
