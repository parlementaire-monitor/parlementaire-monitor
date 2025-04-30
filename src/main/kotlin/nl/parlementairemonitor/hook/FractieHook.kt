package nl.parlementairemonitor.hook

import jakarta.xml.bind.JAXBElement
import nl.parlementairemonitor.task.PostProcessor
import nl.tweedekamer.xsd.tkdata.v1_0.FractieType

class FractieHook : PostProcessor.Hook {

    override suspend fun filter(content: JAXBElement<*>): PostProcessor.Task? {
        if (content.declaredType != FractieType::class.java) {
            return null
        }
        val type = content.value as? FractieType ?: return null
        type.contentType ?: return null
        if (type.contentLength > 100000) return null
        return DownloadTask(type)
    }

}