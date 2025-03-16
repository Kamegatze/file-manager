plugins {
    id("java")
    id("org.springframework.boot") version ("3.4.3")
    id("io.spring.dependency-management") version ("1.1.7")
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

val springCloudVersion = "2024.0.0"
val swaggerVersion = "2.8.5"
val nimbusJwt = "9.47"
val modelMapperVersion = "3.2.0"
val jetbrainsAnnotation = "24.1.0"
val thymeleaf = "3.1.1.RELEASE"

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    implementation("com.nimbusds:nimbus-jose-jwt:${nimbusJwt}")
    implementation("org.modelmapper:modelmapper:${modelMapperVersion}")
    implementation("org.liquibase:liquibase-core")
    implementation("org.jetbrains:annotations:${jetbrainsAnnotation}")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${swaggerVersion}")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("com.kamegatze:general-dto")
    implementation("com.kamegatze.authorization:authorization-remote-jwt")

    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:postgresql")

    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}