package nl.parlementairemonitor

import io.github.flaxoos.ktor.server.plugins.taskscheduling.TaskScheduling
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.mongoDb
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.application.log
import korlibs.time.DateFormat
import nl.parlementairemonitor.task.QueueProcessorTask
import nl.parlementairemonitor.task.SyncFeedTask
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

fun Application.configureAdministration() {
    install(TaskScheduling) {
        mongoDb {
            databaseName = "taskmanager"
            client = MongoDb.client
        }

        task {
            name = "SyncFeed"
            task = { taskExecutionTime ->
                log.info("Starting SyncFeed task: ${taskExecutionTime.format(DateFormat.DEFAULT_FORMAT)}")
                SyncFeedTask.run()
                val duration = (Instant.now().toEpochMilli() - taskExecutionTime.unixMillisLong).milliseconds
                log.info("Completed SyncFeed task in $duration")
            }
            kronSchedule = {
                minutes {
                    from(0).every(10)
                }
                seconds { at(0) }
            }
            concurrency = 1
            monitor.subscribe(ApplicationStopped) {
                SyncFeedTask.stop()
            }
        }

        task {
            name = "QueueProcessor"
            task = { taskExecutionTime ->
                log.info("Starting QueueProcessor task: ${taskExecutionTime.format(DateFormat.DEFAULT_FORMAT)}")
                QueueProcessorTask.run()
                val duration = (Instant.now().toEpochMilli() - taskExecutionTime.unixMillisLong).milliseconds
                log.info("Completed QueueProcessor task in $duration")
            }
            kronSchedule = {
                minutes {
                    from(5).every(10)
                }
                seconds { at(0) }
            }
            concurrency = 1
            monitor.subscribe(ApplicationStopped) {
                QueueProcessorTask.stop()
            }
        }
    }
}
