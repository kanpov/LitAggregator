package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MeshAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryMetadata
import io.github.kanpov.litaggregator.engine.feed.entry.BannerFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.authorizer.AuthorizationState
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.*
import io.github.kanpov.litaggregator.engine.util.io.jBoolean
import io.github.kanpov.litaggregator.engine.util.io.jOptionalInt
import io.github.kanpov.litaggregator.engine.util.io.jString

class MeshBannerProvider(authorizer: MeshAuthorizer) : MeshProvider<BannerFeedEntry>(authorizer) {
    override suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo) {
        val bannerMetaObj = authorizer
            .getJson("https://school.mos.ru/api/news/v2/banners/instance?location=HEADER&service_id=2")
            ?: return // banner is not always present, this is regular behavior

        val bannerId = bannerMetaObj.jOptionalInt("banner_id") ?: return
        val bannerObj = authorizer.getJson("https://school.mos.ru/api/news/v2/banners/$bannerId")!!
        val outgoingUrl = if (bannerObj.jBoolean("action_button")) bannerObj.jString("button_link") else null
        val creationTime = TimeFormatters.longMeshDateTime.parseInstant(bannerObj.jString("created_at"))

        insert(profile.feed, BannerFeedEntry(
            leftImageUrl = bannerObj.jString("image_left"),
            rightImageUrl = bannerObj.jString("image_right"),
            text = bannerObj.jString("text"),
            textColor = bannerObj.jString("banner_text_color"),
            backgroundColor = bannerObj.jString("banner_background_color"),
            outgoingUrl = if (profile.providers.meshBanners!!.addLinks) outgoingUrl else null,
            sourceFingerprint = FeedEntry.fingerprintFrom(bannerId, bannerObj.jString("author_id")),
            metadata = FeedEntryMetadata(creationTime = creationTime)
        ))
    }

    object Definition : AuthorizedProviderDefinition<MeshAuthorizer, BannerFeedEntry> {
        override val name: String = "Баннеры из МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.meshBanners != null }
        override val isAuthorized: (AuthorizationState) -> Boolean = { it.mos != null }
        override val factory: (Profile) -> AuthorizedProvider<MeshAuthorizer, BannerFeedEntry> = { MeshBannerProvider(it.authorization.mos!!) }
    }
}