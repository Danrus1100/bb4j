plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")

    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

publishing {

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.danrus"
            artifactId = "bb4j"
            version = "1.3.1"
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

        maven {
            name = "ShlakoblockSnapshot"
            url = uri("https://maven.shlakoblock.com/snapshots")

            credentials {
                username = project.findProperty("shlakoblock-maven-username")?.toString()
                password = project.findProperty("shlakoblock-maven-password")?.toString()
            }
        }

        mavenLocal()
    }
}