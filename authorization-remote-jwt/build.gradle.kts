plugins {
    id("java")
    id ("maven-publish")
    id("org.springframework.boot") version("3.4.3")
    id("io.spring.dependency-management") version ("1.1.7")
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

val nimbusJwt = "9.47"
val jetbrainsAnnotation = "24.1.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.nimbusds:nimbus-jose-jwt:${nimbusJwt}")
    implementation("org.jetbrains:annotations:${jetbrainsAnnotation}")
}

tasks {
    test {
        useJUnitPlatform()
    }
}