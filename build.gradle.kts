plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.danrus"
            artifactId = "bb4j"
            version = "1.1-SNAPSHOT"
        }
    }

    repositories {
        maven {
            name = "Shlakoblock"
            url = uri("https://maven.shlakoblock.com/releases")

            credentials {
                username = project.findProperty("shlakoblock-maven-username")?.toString()
                password = project.findProperty("shlakoblock-maven-password")?.toString()
            }
        }

        mavenLocal()
    }
}