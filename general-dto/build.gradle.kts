plugins {
    id ("java")
    id ("maven-publish")
    id("org.springframework.boot") version("3.4.3")
    id("io.spring.dependency-management") version ("1.1.7")
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

val swaggerAnnotationsVersion = "2.2.22"

dependencies {
    implementation ("io.swagger.core.v3:swagger-annotations:${swaggerAnnotationsVersion}")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}