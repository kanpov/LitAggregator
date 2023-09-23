package io.github.kanpov.litaggregator.engine.data

import io.github.kanpov.litaggregator.engine.util.ComparisonFilter
import io.github.kanpov.litaggregator.engine.util.ListFilter
import io.github.kanpov.litaggregator.engine.util.RegexFilter
import kotlinx.serialization.Serializable

@Serializable
data class ProviderSettings(
    val timetable: TimetableProviderSettings? = TimetableProviderSettings(),
    val announcements: AnnouncementProviderSettings? = AnnouncementProviderSettings(),
    val ulyss: UlyssProviderSettings? = UlyssProviderSettings(),
    val dnevnikHomework: DnevnikHomeworkProviderSettings? = DnevnikHomeworkProviderSettings(),
    val dnevnikMarks: DnevnikMarkProviderSettings? = DnevnikMarkProviderSettings(),
    val dnevnikRatings: DnevnikRatingProviderSettings? = DnevnikRatingProviderSettings(),
    val dnevnikVisits: DnevnikVisitProviderSettings? = DnevnikVisitProviderSettings(),
    val dnevnikBanners: DnevnikBannerProviderSettings? = DnevnikBannerProviderSettings(),
    val portfolioDiagnostics: PortfolioDiagnosticProviderSettings? = PortfolioDiagnosticProviderSettings(),
    val portfolioEvents: PortfolioEventProviderSettings? = PortfolioEventProviderSettings(),
    val classroom: ClassroomProviderSettings? = ClassroomProviderSettings(),
    val gmail: GmailProviderSettings? = GmailProviderSettings()
)

@Serializable
data class TimetableProviderSettings(
    val includeRooms: Boolean = false,
    val replacements: List<ScheduleReplacement> = emptyList(),
    val caseSensitiveReplacements: Boolean = false
)

@Serializable
data class ScheduleReplacement(
    val from: List<String>,
    val to: String
)

@Serializable
data class AnnouncementProviderSettings(
    val categoryFilter: ListFilter = ListFilter(),
    val contentFilter: ListFilter = ListFilter()
)

@Serializable
data class UlyssProviderSettings(
    val include: UlyssInclusions = UlyssInclusions(),
    val exclude: UlyssExclusions = UlyssExclusions()
)

@Serializable
data class UlyssInclusions(
    val studyMaterials: Boolean = true,
    val hidden: Boolean = false,
    val solelyForOtherGroups: Boolean = false
)

@Serializable
data class UlyssExclusions(
    val titleFilter: RegexFilter = RegexFilter(),
    val cleanContentFilter: RegexFilter = RegexFilter(),
    val htmlContentFilter: RegexFilter = RegexFilter()
)

@Serializable
data class DnevnikHomeworkProviderSettings(
    val includeInteractiveExercises: Boolean = true
)

@Serializable
data class DnevnikMarkProviderSettings(
    val onlyIncludeExams: Boolean = false,
    val weightFilter: ComparisonFilter = ComparisonFilter(),
    val prunePreviousPeriods: Boolean = false
)

@Serializable
data class DnevnikRatingProviderSettings(
    val includeClassmateRatings: Boolean = true
)

@Serializable
data class DnevnikVisitProviderSettings(
    val includeIrregularPatterns: Boolean = true
)

@Serializable
data class DnevnikBannerProviderSettings(
    val addLinks: Boolean = true
)

@Serializable
data class PortfolioEventProviderSettings(
    val onlyVos: Boolean = false
)

@Serializable
data class PortfolioDiagnosticProviderSettings(
    val includeComparisons: Boolean = false
)

@Serializable
data class ClassroomProviderSettings(
    val courseFilter: ListFilter = ListFilter()
)

@Serializable
data class GmailProviderSettings(
    val include: GmailInclusions = GmailInclusions(),
    val aliasContactsByInitials: Boolean = true,
    val contacts: List<GmailContact> = emptyList()
)

@Serializable
data class GmailInclusions(
    val unimportant: Boolean = true,
    val notFromContacts: Boolean = false,
    val googleNotifications: Boolean = false,
    val spamOrTrash: Boolean = false
)

@Serializable
data class GmailContact(
    val identity: String,
    val address: String,
    val aliases: List<String>
)
