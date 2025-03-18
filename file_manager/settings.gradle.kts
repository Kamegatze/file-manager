dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
        create("tests") {
            from(files("../gradle/tests.versions.toml"))
        }
    }
}
rootProject.name = "file_manager"