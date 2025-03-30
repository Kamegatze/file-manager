plugins {
	id ("java")
	alias(libs.plugins.org.springframework.boot)
	alias(libs.plugins.dependency.management)
}

group = "org.kamegatze"
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
	implementation(libs.spring.cloud.starter.netflix.eureka.server)
	testImplementation(tests.spring.boot.starter.test)
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
