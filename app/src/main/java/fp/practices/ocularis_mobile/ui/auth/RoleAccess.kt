package fp.practices.ocularis_mobile.ui.auth

object RoleAccess {
    fun canManageDoctors(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canManagePatients(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canManageAppointments(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR")

    fun canManageDetails(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR")

    fun canReadPatients(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR")

    fun canReadDoctors(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN")

    fun canReadAppointments(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR", "PATIENT")

    fun canReadDetails(roles: Set<String>): Boolean = hasAnyRole(roles, "ADMIN", "DOCTOR", "PATIENT")

    private fun hasAnyRole(roles: Set<String>, vararg expected: String): Boolean {
        if (roles.isEmpty()) return false
        return expected.any { roles.contains(it) }
    }
}

