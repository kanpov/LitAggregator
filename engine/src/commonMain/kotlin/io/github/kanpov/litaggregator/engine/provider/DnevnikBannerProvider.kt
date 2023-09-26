package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryInserter
import io.github.kanpov.litaggregator.engine.feed.entry.BannerFeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.settings.Authorization
import io.github.kanpov.litaggregator.engine.settings.ProviderSettings
import io.github.kanpov.litaggregator.engine.util.jBoolean
import io.github.kanpov.litaggregator.engine.util.jInt
import io.github.kanpov.litaggregator.engine.util.jString

class DnevnikBannerProvider(authorizer: MosAuthorizer) : AbstractDnevnikProvider<BannerFeedEntry>(authorizer, exitOnHit = true) {
    override suspend fun dnevnikProvide(inserter: FeedEntryInserter, profile: Profile, studentInfo: DnevnikStudentInfo) {
        val bannerMetaObj = authorizer
            .getJson("https://school.mos.ru/api/news/v2/banners/instance?location=HEADER&service_id=2")
            ?: return // banner is not always present, this is regular behavior

        val bannerId = bannerMetaObj.jInt("banner_id")
        val bannerObj = authorizer.getJson("https://school.mos.ru/api/news/v2/banners/$bannerId")!!
        val outgoingUrl = if (bannerObj.jBoolean("action_button")) bannerObj.jString("button_link") else null

        inserter.insert(BannerFeedEntry(
            leftImageUrl = bannerObj.jString("image_left"),
            rightImageUrl = bannerObj.jString("image_right"),
            text = bannerObj.jString("text"),
            textColor = bannerObj.jString("banner_text_color"),
            backgroundColor = bannerObj.jString("banner_background_color"),
            outgoingUrl = outgoingUrl,
            sourceFingerprint = FeedEntry.fingerprintFrom(bannerId, bannerObj.jString("author_id"))
        ))
    }

    object Definition : AuthorizedProviderDefinition<MosAuthorizer, BannerFeedEntry> {
        override val name: String = "Баннеры МЭШ"
        override val isEnabled: (ProviderSettings) -> Boolean = { it.dnevnikBanners != null }
        override val isAuthorized: (Authorization) -> Boolean = { it.mos != null }
        override val factory: (Authorization) -> AuthorizedProvider<MosAuthorizer, BannerFeedEntry> = { DnevnikBannerProvider(it.mos!!) }
    }
}