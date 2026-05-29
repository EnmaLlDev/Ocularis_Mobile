package fp.practices.ocularis_mobile.ui.auth

/**
 * Utilidad para verificar permisos de acceso según los roles del usuario.
 */
object RoleAccess {
    fun canManageDoctors(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canManagePatients(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canManageAppointments(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR")

    fun canManageDetails(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR")

    fun canReadPatients(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR", "PATIENT")

    fun canReadDoctors(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canReadAppointments(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR", "PATIENT")

    fun canReadDetails(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR", "PATIENT")

    /**
     * Verifica si el usuario posee alguno de los roles esperados.
     * @param roles roles del usuario
     * @param expected roles permitidos
     * @return true si coincide algún rol
     */
    private fun hasAnyRole(roles: Set<String>, vararg expected: String): Boolean {
        if (roles.isEmpty()) return false
        return expected.any { roles.contains(it) }
    }
}
