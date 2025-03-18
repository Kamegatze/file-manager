plugins {
    id ("java")
    alias(libs.plugins.org.springframework.boot)
    alias(libs.plugins.dependency.management)
}

val currentVersion = "1.0"

group = "com.kamegatze"
version = currentVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation (libs.swagger.annotations)
    implementation(libs.spring.boot.starter.web)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}