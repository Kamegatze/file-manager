dependencyResolutionManagement {
    versionCatalogs {
        create("tests") {
            from(files("./gradle/tests.versions.toml"))
        }
    }
}
rootProject.name = "gateway-service"