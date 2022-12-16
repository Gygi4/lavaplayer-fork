plugins {
  `java-library`
  `maven-publish`
}

val moduleName = "lava-common"
version = "1.1.2"

dependencies {
  implementation("org.slf4j:slf4j-api:2.0.6")
  implementation("commons-io:commons-io:2.11.0")
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      artifactId = moduleName
      artifact(sourcesJar)
    }
  }
}
