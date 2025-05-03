package nl.parlementairemonitor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommissieZetel(
    @SerialName("_id") val id: String,
    val vast: List<CommissiePersoon> = emptyList(),
    val vervanger: List<CommissiePersoon> = emptyList(),
)