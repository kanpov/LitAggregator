package io.github.kanpov.litaggregator.engine.provider

import io.github.kanpov.litaggregator.engine.authorizer.MosAuthorizer
import io.github.kanpov.litaggregator.engine.feed.FeedEntry
import io.github.kanpov.litaggregator.engine.profile.Profile
import io.github.kanpov.litaggregator.engine.util.jArray
import io.github.kanpov.litaggregator.engine.util.jInt
import io.github.kanpov.litaggregator.engine.util.jString
import kotlinx.serialization.json.JsonObject

abstract class MeshProvider<E : FeedEntry>(authorizer: MosAuthorizer) : AuthorizedProvider<MosAuthorizer, E>(authorizer) {
    abstract suspend fun meshProvide(profile: Profile, studentInfo: MeshStudentInfo)

    override suspend fun provide(profile: Profile) {
        val infoObj = authorizer.getJson("https://school.mos.ru/api/family/web/v1/profile")!!
        val studentObj = infoObj.jArray<JsonObject>("children")
            .first { it.jString("class_name") == "${profile.identity.parallel}-${profile.identity.group}" }

        return meshProvide(profile, MeshStudentInfo(
            profileId = studentObj.jInt("id").toString(),
            personId = studentObj.jString("contingent_guid"),
            classUnitId = studentObj.jInt("class_unit_id").toString(),
            contractId = studentObj.jInt("contract_id").toString(),
            parallelCurriculumId = studentObj.jInt("parallel_curriculum_id").toString()
        )
        )
    }
}

data class MeshStudentInfo(
    val profileId: String,
    val personId: String,
    val classUnitId: String,
    val contractId: String,
    val parallelCurriculumId: String
)
