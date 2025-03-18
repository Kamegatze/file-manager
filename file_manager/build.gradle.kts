plugins {
    id("java")
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.dependency.management)
}

group = "com.kamegatze"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}


repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {

    implementation(libs.bundles.core.library)

    implementation(libs.spring.kafka)

    implementation(libs.bundles.spring.starter)

    implementation(libs.bundles.com.kamegatze)

    testImplementation(tests.bundles.tests)

    runtimeOnly(libs.postgresql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}