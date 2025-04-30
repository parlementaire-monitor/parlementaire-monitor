package nl.parlementairemonitor.util

import nl.tweedekamer.xsd.tkdata.v1_0.DownloadEntiteitType
import org.bson.Document
import java.io.File

object FileUtil {

    fun getResourceFile(doc: Document): File {
        val id = doc["_id"] as String
        val ext = getFileExtension(doc["contentType"] as String)
        return getResourceFile(id, ext)
    }

    fun getResourceFile(type: DownloadEntiteitType): File {
        return getResourceFile(type.id, getFileExtension(type.contentType))
    }

    private fun getResourceFile(id: String, ext: String): File {
        val subDir = File(getResourceDir(), id.substring(0, 2))
        if (!subDir.exists()) {
            subDir.mkdirs()
        }
        return File(subDir, "$id.$ext")
    }

    fun getResourceDir(): File {
        return File(System.getenv("RESOURCE_DIR"))
    }

    fun getFileExtension(contentType: String): String {
        return when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "application/pdf" -> "pdf"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            else -> throw Exception("Unexpected content: ${contentType}")
        }
    }
}