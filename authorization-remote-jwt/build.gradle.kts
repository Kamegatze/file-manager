plugins {
    id("java")
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.dependency.management)
}

val currentVersion = "1.0-SNAPSHOT"

group = "com.kamegatze.authorization"
version = currentVersion

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.nimbus.jose.jwt)
    implementation(libs.jetbrains.annotations)
}

tasks {
    test {
        useJUnitPlatform()
    }
}