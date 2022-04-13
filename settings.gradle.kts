rootProject.name = "caroline"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":caroline-sdk:caroline-sdk-admin",
    ":caroline-sdk:caroline-sdk-analytics",
    ":caroline-sdk:caroline-sdk-auth",
    ":caroline-sdk:caroline-sdk-config",
    ":caroline-sdk:caroline-sdk-core",
    ":caroline-sdk:caroline-sdk-crash",
    ":caroline-sdk:caroline-sdk-functions",
    ":caroline-sdk:caroline-sdk-logging",
    ":caroline-sdk:caroline-sdk-store",
    ":caroline-sdk:caroline-sdk-store-encrypted",
    ":caroline-sdk:caroline-sdk-internal",
)

include(
    ":caroline-server:caroline-server-core",
    ":caroline-server:caroline-server-crash",
    ":caroline-server:caroline-server-logging",
    ":caroline-server:caroline-server-projects",
    ":caroline-server:caroline-server-users",
    ":caroline-server:caroline-server-bundled",
    ":caroline-server:caroline-server-internal",
)
