package io.github.kanpov.litaggregator.engine.response.ulyss

import io.github.kanpov.litaggregator.engine.util.FlagBoolean
import io.github.kanpov.litaggregator.engine.util.JsonInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://in.lit.msu.ru/Ulysses/$studyYear/$studyGrade/$subjectId/$teacherId/
// Returns a JSON root list of UlyssHomeworks

@Serializable
data class UlyssHomework(
    @SerialName("study_grade") val studyGrade: Int,
    val subject: String,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("teacher_fullname") val teacherFullName: String,
    val title: String,
    val body: String,
    @SerialName("body_clean") val cleanBody: String,
    @SerialName("attachments") private val separatedAttachments: String,
    @SerialName("show_body") val showBody: FlagBoolean,
    @SerialName("прикреплено") val pinned: FlagBoolean,
    @SerialName("Post date") val creationTime: JsonInstant,
    @SerialName("Updated date") val updateTime: JsonInstant,
    @SerialName("опубликовано") val published: FlagBoolean
)
