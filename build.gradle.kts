buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    java
    application
}
repositories {
    jcenter()
    maven("https://artifacts.anaplan-np.net/artifactory/anaplan-all")
    // FIXME temporary until we can push first version to Anaplan artifactory
    mavenLocal()
}

dependencies {
    // This dependency is used by the application.
    implementation("com.faunadb:faunadb-java:2.12.0")

    implementation("com.anaplan.logging.analog:analog-springboot-starter:1.2.58")
    implementation("com.anaplan.logging:analog-log4j2:1.0.32")
    implementation("com.anaplan.dd:dd-dsl:0.1.117")

    implementation("com.google.guava:guava:28.1-jre")

    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

application {
    // Define the main class for the application.
    mainClassName = "fauna.test.App"
}

tasks.test {
    useJUnitPlatform()
}
