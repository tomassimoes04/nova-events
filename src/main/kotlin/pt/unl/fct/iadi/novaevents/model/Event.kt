package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*
import java.time.LocalDate

enum class EventType { WORKSHOP, TALK, COMPETITION, SOCIAL, MEETING, OTHER }

@Entity
@Table(name = "events")
class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clubId: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: EventType = EventType.OTHER,

    var location: String? = null,

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var owner: AppUser? = null
)
