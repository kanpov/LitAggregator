package io.github.kanpov.litaggregator.engine.settings

import io.github.kanpov.litaggregator.engine.util.ComparisonFilter
import io.github.kanpov.litaggregator.engine.util.ListFilter
import io.github.kanpov.litaggregator.engine.util.RegexFilter
import kotlinx.serialization.Serializable

@Serializable
data class ProviderSettings(
    val announcements: AnnouncementProviderSettings? = AnnouncementProviderSettings(),
    val ulyss: UlyssProviderSettings? = UlyssProviderSettings(),
    val meshHomework: MeshHomeworkProviderSettings? = MeshHomeworkProviderSettings(),
    val meshMarks: MeshMarkProviderSettings? = MeshMarkProviderSettings(),
    val meshRatings: MeshRatingProviderSettings? = MeshRatingProviderSettings(),
    val meshVisits: MeshVisitProviderSettings? = MeshVisitProviderSettings(),
    val meshBanners: MeshBannerProviderSettings? = MeshBannerProviderSettings(),
    val portfolioDiagnostics: PortfolioDiagnosticProviderSettings? = PortfolioDiagnosticProviderSettings(),
    val portfolioEvents: PortfolioEventProviderSettings? = PortfolioEventProviderSettings(),
    val classroom: ClassroomProviderSettings? = ClassroomProviderSettings(),
    val gmail: GmailProviderSettings? = GmailProviderSettings()
)

@Serializable
data class AnnouncementProviderSettings(
    val categoryFilter: ListFilter = ListFilter(),
    val contentFilter: ListFilter = ListFilter()
)

@Serializable
data class UlyssProviderSettings(
    val include: UlyssInclusions = UlyssInclusions(),
    val exclude: UlyssExclusions = UlyssExclusions(),
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
data class MeshHomeworkProviderSettings(
    val titleFormatter: String = "!{subject} на !{assigned_time}",
    val onlyIncludeOO: Boolean = false
)

@Serializable
data class MeshMarkProviderSettings(
    val onlyIncludeExams: Boolean = false,
    val weightFilter: ComparisonFilter = ComparisonFilter()
)

@Serializable
data class MeshRatingProviderSettings(
    val includeClassmateRatings: Boolean = true
)

@Serializable
data class MeshVisitProviderSettings(
    val includeIrregularPatterns: Boolean = true
)

@Serializable
data class MeshBannerProviderSettings(
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
