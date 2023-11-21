package io.github.kanpov.litaggregator.engine.settings

import io.github.kanpov.litaggregator.engine.util.ComparisonFilter
import io.github.kanpov.litaggregator.engine.util.ListFilter
import io.github.kanpov.litaggregator.engine.util.RegexFilter
import kotlinx.serialization.Serializable

@Serializable
data class ProviderSettings(
    var announcements: AnnouncementProviderSettings? = null,
    var ulysses: UlyssesProviderSettings? = null,
    var meshHomework: MeshHomeworkProviderSettings? = null,
    var meshMarks: MeshMarkProviderSettings? = null,
    var meshRatings: MeshRatingProviderSettings? = null,
    var meshVisits: MeshVisitProviderSettings? = null,
    var meshBanners: MeshBannerProviderSettings? = null,
    var portfolioDiagnostics: PortfolioDiagnosticProviderSettings? = null,
    var portfolioEvents: PortfolioEventProviderSettings? = null,
    var classroom: ClassroomProviderSettings? = null
)

@Serializable
data class AnnouncementProviderSettings(
    var categoryFilter: ListFilter = ListFilter(),
    var htmlFilter: ListFilter = ListFilter()
)

@Serializable
data class UlyssesProviderSettings(
    val inclusions: UlyssesInclusions = UlyssesInclusions(),
    val filters: UlyssesFilters = UlyssesFilters(),
)

@Serializable
data class UlyssesInclusions(
    var studyMaterials: Boolean = true,
    var hidden: Boolean = false,
    var solelyForOtherGroups: Boolean = false
)

@Serializable
data class UlyssesFilters(
    var titleFilter: RegexFilter = RegexFilter(),
    var cleanContentFilter: RegexFilter = RegexFilter(),
    var htmlContentFilter: RegexFilter = RegexFilter()
)

@Serializable
data class MeshHomeworkProviderSettings(
    var titleFormatter: String = "!{subject} на !{assigned_time}",
    var onlyIncludeOO: Boolean = false
)

@Serializable
data class MeshMarkProviderSettings(
    var onlyIncludeExams: Boolean = false,
    var weightFilter: ComparisonFilter = ComparisonFilter()
)

@Serializable
data class MeshRatingProviderSettings(
    var includeClassmateRatings: Boolean = true
)

@Serializable
data class MeshVisitProviderSettings(
    var includeIrregularPatterns: Boolean = true
)

@Serializable
data class MeshBannerProviderSettings(
    var addLinks: Boolean = true
)

@Serializable
data class PortfolioEventProviderSettings(
    var onlyVos: Boolean = false
)

@Serializable
data class PortfolioDiagnosticProviderSettings(
    var includeComparisons: Boolean = true
)

@Serializable
data class ClassroomProviderSettings(
    var courseFilter: ListFilter = ListFilter()
)
