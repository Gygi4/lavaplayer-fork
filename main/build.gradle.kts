plugins {
  `java-library`
  groovy
  `maven-publish`
}

val moduleName = "lavaplayer"
version = "1.4.5"

dependencies {
  api("com.github.davidffa.lavaplayer-fork:lava-common:ebd213f")
  implementation("com.github.davidffa:lavaplayer-natives-fork:1.0.6")
  implementation("com.github.walkyst.JAADec-fork:jaadec-ext-aac:0.1.3")
  implementation("org.mozilla:rhino-engine:1.7.14")
  implementation("org.slf4j:slf4j-api:2.0.7")

  api("org.apache.httpcomponents:httpclient:4.5.14")
  implementation("commons-codec:commons-codec:1.15")
  implementation("commons-io:commons-io:2.13.0")

  api("com.fasterxml.jackson.core:jackson-core:2.15.2")
  api("com.fasterxml.jackson.core:jackson-databind:2.15.2")

  implementation("org.jsoup:jsoup:1.16.1")
  implementation("net.iharder:base64:2.3.9")
  implementation("org.json:json:20230618")

  testImplementation("org.apache.groovy:groovy:4.0.13")
  testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")
  testImplementation("ch.qos.logback:logback-classic:1.4.8")
  testImplementation("com.sedmelluq:lavaplayer-test-samples:1.3.11")
}

tasks.jar {
  exclude("natives")
}

val updateVersion by tasks.registering {
  File("$projectDir/src/main/resources/com/sedmelluq/discord/lavaplayer/tools/version.txt").let {
    it.parentFile.mkdirs()
    it.writeText(version.toString())
  }
}

tasks.classes.configure {
  finalizedBy(updateVersion)
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
