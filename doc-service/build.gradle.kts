plugins {
	id ("java")
	id ("org.springframework.boot") version ("3.4.3")
	id ("io.spring.dependency-management") version ("1.1.7")
}

group = "org.kamegatze"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val springCloudVersion = "2024.0.0"
val swaggerVersion = "2.5.0"

dependencies {
	implementation ("org.springframework.cloud:spring-cloud-starter-gateway")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${swaggerVersion}")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom ("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}")
	}
}

tasks {
	test {
		useJUnitPlatform()
	}
}