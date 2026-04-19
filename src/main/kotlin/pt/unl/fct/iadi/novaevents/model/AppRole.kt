package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "app_roles")
class AppRole(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: AppUser = AppUser()
)
