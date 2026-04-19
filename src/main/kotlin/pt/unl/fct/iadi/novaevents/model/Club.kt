package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "clubs")
class Club(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, length = 1024)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: ClubCategory = ClubCategory.SOCIAL
) {
    // Unidirectional — club_id FK is owned by Event.clubId; Club reads it read-only
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", insertable = false, updatable = false)
    val events: MutableList<Event> = mutableListOf()
}
