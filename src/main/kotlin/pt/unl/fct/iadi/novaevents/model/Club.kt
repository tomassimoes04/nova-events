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
    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    val events: MutableList<Event> = mutableListOf()
}
