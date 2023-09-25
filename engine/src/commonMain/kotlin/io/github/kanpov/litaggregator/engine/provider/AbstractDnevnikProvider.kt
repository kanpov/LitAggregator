package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.feed.FeedEntryInserter
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.util.jArray
import io.github.kanpov.litaggregator.engine.util.jInt
import io.github.kanpov.litaggregator.engine.util.jString
import kotlinx.serialization.json.JsonObject

abstract class AbstractDnevnikProvider<E : FeedEntry>(authorizer: MosAuthorizer, exitOnHit: Boolean)
    : AuthorizedProvider<MosAuthorizer, E>(authorizer, exitOnHit) {
    abstract suspend fun dnevnikProvide(inserter: FeedEntryInserter, profile: Profile, studentInfo: DnevnikStudentInfo)

    override suspend fun provide(inserter: FeedEntryInserter, profile: Profile) {
        println("THIS IS BEING CALLED")

        val infoObj = authorizer.getJson("https://school.mos.ru/api/family/web/v1/profile?nocache=false")!!
        val studentObj = infoObj.jArray<JsonObject>("children")
            .first { it.jString("class_name") == "${profile.identity.parallel}-${profile.identity.group}" }

        return dnevnikProvide(inserter, profile, DnevnikStudentInfo(
            profileId = studentObj.jInt("id").toString(),
            personId = studentObj.jString("contingent_guid"),
            classUnitId = studentObj.jInt("class_unit_id").toString(),
            contractId = studentObj.jInt("contract_id").toString(),
            parallelCurriculumId = studentObj.jInt("parallel_curriculum_id").toString()
        ))
    }
}

data class DnevnikStudentInfo(
    val profileId: String,
    val personId: String,
    val classUnitId: String,
    val contractId: String,
    val parallelCurriculumId: String
)
