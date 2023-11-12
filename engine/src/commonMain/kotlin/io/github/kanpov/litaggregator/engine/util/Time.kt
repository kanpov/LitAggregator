package io.github.kanpov.litaggregator.engine.util

import java.time.*
import java.time.format.DateTimeFormatter

// WARNING: this is an absolute clusterfuck
object TimeFormatters {
    val zof: ZoneOffset = ZoneOffset.ofHours(3)
    val zid: ZoneId = ZoneId.ofOffset("GMT", zof)

    val isoLocalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(zid)
    val isoGlobalDateTime: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    val zuluDateTime: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    val dottedMeshDate = newFormatter("dd.MM.uuuu") // 05.09.2023
    val slashedMeshDate = newFormatter("uuuu-MM-dd") // 2023-09-05

    val shortMeshTime = newFormatter("HH:mm") // 08:30
    val longMeshTime = newFormatter("HH:mm:ss") // 08:30:45

    val dottedShortMeshDateTime = newFormatter("uuuu.MM.dd HH:mm")
    val shortMeshDateTime = newFormatter("uuuu-MM-dd HH:mm")
    val longMeshDateTime = newFormatter("uuuu-MM-dd HH:mm:ss")

    fun parseMeshDateTime(date: String, time: String): Instant {
        val finalDate = date.removeSuffix("T-")
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.parseInstant("${finalDate}T$time")
    }

    private fun newFormatter(pattern: String): DateTimeFormatter = DateTimeFormatter
        .ofPattern(pattern)
        .withZone(zid)
}

fun DateTimeFormatter.parseInstant(literal: String): Instant {
    if (literal.length == 10) { // DISGUSTING
        return Instant.ofEpochSecond(this
            .parse(literal, LocalDate::from)
            .toEpochSecond(LocalTime.now(TimeFormatters.zid), TimeFormatters.zof))
    }

    return Instant.ofEpochSecond(this
        .parse(literal, LocalDateTime::from)
        .toEpochSecond(TimeFormatters.zof))
}

fun Instant.differenceFrom(other: Instant): Instant = Instant.ofEpochMilli(other.toEpochMilli() - this.toEpochMilli())
