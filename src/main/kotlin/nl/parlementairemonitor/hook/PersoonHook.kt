package nl.parlementairemonitor.hook

import jakarta.xml.bind.JAXBElement
import nl.parlementairemonitor.task.PostProcessor
import nl.tweedekamer.xsd.tkdata.v1_0.PersoonType

class PersoonHook : PostProcessor.Hook {

    override suspend fun filter(content: JAXBElement<*>): PostProcessor.Task? {
        if (content.declaredType != PersoonType::class.java) {
            return null
        }
        val type = content.value as? PersoonType ?: return null
        type.contentType ?: return null
        if (type.contentLength > 100000) return null
        return DownloadTask(type)
    }

}

