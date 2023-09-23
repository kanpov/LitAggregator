package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.Authorizer
import io.github.kanpov.litaggregator.engine.data.FeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile

abstract class Provider<A : Authorizer, E : FeedEntry>(protected val authorizer: A) {
    abstract suspend fun provide(profile: Profile, entries: MutableList<E>)
}
