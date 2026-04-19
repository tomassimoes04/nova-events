package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "app_users")
class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String = "",

    @Column(nullable = false)
    var password: String = "",

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val roles: MutableList<AppRole> = mutableListOf()
)
