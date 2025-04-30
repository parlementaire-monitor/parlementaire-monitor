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
import java.time.Duration
import java.time.Instant

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
                SyncFeedTask.INSTANCE.run()
                val duration = Duration.ofMillis(Instant.now().toEpochMilli() - taskExecutionTime.unixMillisLong)
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
                SyncFeedTask.INSTANCE.stop()
            }
        }

        task {
            name = "QueueProcessor"
            task = { taskExecutionTime ->
                log.info("Starting QueueProcessor task: ${taskExecutionTime.format(DateFormat.DEFAULT_FORMAT)}")
                QueueProcessorTask.INSTANCE.run()
                val duration = Duration.ofMillis(Instant.now().toEpochMilli() - taskExecutionTime.unixMillisLong)
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
                QueueProcessorTask.INSTANCE.stop()
            }
        }
    }
}
