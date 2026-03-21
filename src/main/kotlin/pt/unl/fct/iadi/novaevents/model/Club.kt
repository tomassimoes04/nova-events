package pt.unl.fct.iadi.novaevents.model

// Domain objects represent the internal data state
data class Club(
    val id: Long,
    val name: String,
    val description: String,
    val category: ClubCategory
)