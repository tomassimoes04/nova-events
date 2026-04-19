package pt.unl.fct.iadi.novaevents.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.AppRole
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository

@Service
class AppUserDetailsManager(private val userRepository: AppUserRepository) : UserDetailsManager {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User '$username' not found")
        val authorities = user.roles.map { SimpleGrantedAuthority(it.name) }
        return User(user.username, user.password, authorities)
    }

    override fun createUser(userDetails: UserDetails) {
        val appUser = AppUser(username = userDetails.username, password = userDetails.password)
        userDetails.authorities.forEach { authority ->
            appUser.roles.add(AppRole(name = authority.authority, user = appUser))
        }
        userRepository.save(appUser)
    }

    override fun updateUser(userDetails: UserDetails) {
        val appUser = userRepository.findByUsername(userDetails.username)
            ?: throw UsernameNotFoundException("User '${userDetails.username}' not found")
        appUser.password = userDetails.password
        userRepository.save(appUser)
    }

    override fun deleteUser(username: String) {
        val appUser = userRepository.findByUsername(username) ?: return
        userRepository.delete(appUser)
    }

    override fun changePassword(oldPassword: String, newPassword: String) {
        throw UnsupportedOperationException("changePassword is not supported")
    }

    override fun userExists(username: String): Boolean = userRepository.existsByUsername(username)

    fun findAppUser(username: String): AppUser =
        userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User '$username' not found")
}
