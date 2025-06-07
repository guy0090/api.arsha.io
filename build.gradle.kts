plugins {
	java
	alias(libs.plugins.versions)
	alias(libs.plugins.version.catalog.update)
	alias(libs.plugins.researchgate.release)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependencies)
}

group = "io.arsha"

release {
	failOnCommitNeeded = true
	failOnPublishNeeded = false
	failOnSnapshotDependencies = false
	failOnUnversionedFiles = true
	failOnUpdateNeeded = true
	revertOnFail = true
	pushReleaseVersionBranch = "master"
	snapshotSuffix = "-SNAPSHOT"
	versionPropertyFile = "gradle.properties"
	preTagCommitMessage = "Released:"
	newVersionCommitMessage = "New Development Version:"

	git {
		requireBranch.set("develop")
		pushToRemote.set("origin")
	}
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation(libs.jakarta.inject)
	implementation(libs.apache.lang3)
	implementation(libs.apache.http5)
	implementation(libs.jsoup)
	implementation(libs.struct) {
		exclude("commons-io", "commons-io")
	}
	implementation(libs.commons.io)
	implementation(libs.redisson)

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")

	testCompileOnly(libs.lombok)
	testAnnotationProcessor(libs.lombok)
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}
