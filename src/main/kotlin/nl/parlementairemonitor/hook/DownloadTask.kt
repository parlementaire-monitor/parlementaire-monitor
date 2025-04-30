package nl.parlementairemonitor.hook

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import nl.parlementairemonitor.task.PostProcessor
import nl.parlementairemonitor.util.FileUtil
import nl.parlementairemonitor.util.SyncFeedUtil
import nl.tweedekamer.xsd.tkdata.v1_0.DownloadEntiteitType
import java.io.File

open class DownloadTask(val type: DownloadEntiteitType) : PostProcessor.Task {

    companion object {
        val httpClient = HttpClient(Java)
    }

    override suspend fun process() {
        download()
        postDownload()
    }

    private suspend fun download() {
        if (file().exists()) file().delete()

        val response: HttpResponse = httpClient.get("${SyncFeedUtil.SYNC_FEED_BASE_URL}/Resources/${type.id}")

        if (response.status.value in 200..299) {
            response.bodyAsChannel().toInputStream().use { input ->
                file().outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            throw Exception("Could not download resource ${type.id}")
        }
    }

    fun file(): File {
        return FileUtil.getResourceFile(type)
    }


    open suspend fun postDownload() {
        //
    }

}