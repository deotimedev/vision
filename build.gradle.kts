plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "com.deotime"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.20-Beta")
}

publishing {
    repositories { mavenLocal() }
    publications {
        create<MavenPublication>("vision") {
            artifactId = "vision"
            from(components["java"])
        }
    }
}