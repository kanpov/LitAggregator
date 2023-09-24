package io.github.kanpov.litaggregator.engine.response.ulyss

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// https://in.lit.msu.ru/Ulysses/$studyYear/
// Returns a JSON root list of UlyssSubjectDefinitions

@Serializable
data class UlyssSubject(
    @SerialName("study_year") val studyYear: String,
    @SerialName("study_grade") val studyGrade: Int,
    val subject: String,
    @SerialName("subject_id") val subjectId: Int,
    @SerialName("teacher_fullname_shorten") val shortTeacherFullName: String,
    @SerialName("teacher_id") val teacherId: Int
)
