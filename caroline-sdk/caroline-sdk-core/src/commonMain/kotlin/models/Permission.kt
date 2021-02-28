package tools.caroline.core.models

import kotlinx.serialization.Serializable

@Serializable
public enum class Permission {
    Global,
    ListProjects,
    CreateProject,
    DeleteProject
}
