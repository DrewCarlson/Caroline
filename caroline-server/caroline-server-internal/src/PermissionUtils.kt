package cloud.caroline.internal

import cloud.caroline.core.models.Permission
import cloud.caroline.core.models.Services
import org.drewcarlson.ktor.permissions.WithPermissionGroup

public fun WithPermissionGroup<Permission>.checkServicesPermission(
    service: Services,
    requireId: Boolean = true,
) {
    add<Permission.UseServices> {
        stub(Permission.UseServices(""))
        verify { it.canUse(service) }
        select { permissions ->
            val projectId = parameters["projectId"]
                ?: return@select if (requireId) emptySet() else permissions
            permissions.filter { it.projectId == projectId }.toSet()
        }
    }
}
