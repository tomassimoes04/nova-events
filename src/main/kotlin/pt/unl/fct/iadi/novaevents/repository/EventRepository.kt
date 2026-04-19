package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import java.time.LocalDate

interface EventRepository : JpaRepository<Event, Long> {
    fun findByClubId(clubId: Long): List<Event>
    fun existsByNameIgnoreCase(name: String): Boolean
    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    @Query("""
        SELECT DISTINCT e FROM Event e
        LEFT JOIN FETCH e.owner
        WHERE (:type IS NULL OR e.type = :type)
        AND (:clubId IS NULL OR e.clubId = :clubId)
        AND (:fromDate IS NULL OR e.date >= :fromDate)
        AND (:toDate IS NULL OR e.date <= :toDate)
    """)
    fun findFiltered(
        @Param("type") type: EventType?,
        @Param("clubId") clubId: Long?,
        @Param("fromDate") fromDate: LocalDate?,
        @Param("toDate") toDate: LocalDate?
    ): List<Event>
}
