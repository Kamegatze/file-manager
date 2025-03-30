dependencyResolutionManagement {
    versionCatalogs {
        create("tests") {
            from(files("./gradle/tests.versions.toml"))
        }
    }
}
rootProject.name = "authorization-microservice"