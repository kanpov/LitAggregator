package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorization.GoogleAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntryAttachment
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.HomeworkFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorization.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.TimeFormatters
import io.github.kanpov.litaggregator.engine.util.io.*
import io.github.kanpov.litaggregator.engine.util.padLeft
import io.github.kanpov.litaggregator.engine.util.parseInstant
import java.time.Instant

class ClassroomProvider(authorizer: GoogleAuthorizer) : AuthorizedProvider<GoogleAuthorizer, HomeworkFeedEntry>(authorizer) {
    override suspend fun provide(profile: Profile) {
        val courses = authorizer.getJsonArrayFromPayload(
            "https://classroom.googleapis.com/v1/courses",
            payloadName = "courses"
        )!!
        val (earliestTime, _) = getRelevantPastDays(profile).entries.last()

        for (courseObj in courses) {
            val courseId = courseObj.jString("id")
            val courseName = courseObj.jString("name")

            if (!profile.providers.classroom!!.courseFilter.match(courseName)) continue

            val courseWorks = authorizer.getJsonArrayFromPayload(
                "https://classroom.googleapis.com/v1/courses/$courseId/courseWork",
                payloadName = "courseWork"
            ) ?: continue

            for (courseWorkObj in courseWorks) {
                val creationTime = TimeFormatters.zuluDateTime.parse(courseWorkObj.jString("creationTime"), Instant::from)
                if (creationTime.isBefore(earliestTime)) continue

                val courseWorkId = courseWorkObj.jString("id")
                val attachments = buildList {
                    if (!courseWorkObj.containsKey("materials")) return@buildList
                    for (materialObj in courseWorkObj.jArray("materials")) {
                        if (!materialObj.containsKey("driveFile")) continue

                        val driveFileObj = materialObj.jObject("driveFile").jObject("driveFile")

                        this += FeedEntryAttachment(
                            downloadUrl = driveFileObj.jString("alternateLink"),
                            title = driveFileObj.jString("title"),
                            thumbnailUrl = driveFileObj.jOptionalString("thumbnailUrl")
                        )
                    }
                }

                var dueTime: Instant? = null
                if (courseWorkObj.containsKey("dueDate")) {
                    val dueDateObj = courseWorkObj.jObject("dueDate")
                    val dueYear = dueDateObj.jInt("year")
                    val dueMonth = dueDateObj.jInt("month").toString().padLeft(until = 2, with = '0')
                    val dueDay = dueDateObj.jInt("day").toString().padLeft(until = 2, with = '0')
                    dueTime = TimeFormatters.dottedMeshDate.parseInstant("$dueDay.$dueMonth.$dueYear")
                }

                insert(profile.feed, HomeworkFeedEntry(
                    title = courseWorkObj.jString("title"),
                    plain = courseWorkObj.jString("description"),
                    html = null,
                    subject = courseName,
                    teacher = null,
                    assignedTime = dueTime,
                    attachments = attachments,
                    allowsSubmissions = true,
                    submissionUrl = courseWorkObj.jString("alternateLink"),
                    sourceFingerprint = courseWorkId,
                    metadata = FeedEntryMetadata(creationTime = creationTime)
                ))
            }
        }
    }

    object Definition : AuthorizedProviderDefinition<GoogleAuthorizer, HomeworkFeedEntry> {
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.googleSession != null }
        override val name: String = "Домашние задания из Google Classroom"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.classroom != null }
        override val factory: (Profile) -> SimpleProvider<HomeworkFeedEntry> = { ClassroomProvider(it.authorization.google) }
    }
}
