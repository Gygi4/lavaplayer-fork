import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    `java-library`
    alias(libs.plugins.maven.publish.base)
}

java {
    withSourcesJar()
    withJavadocJar()
}

base {
    archivesName = "lavaplayer-natives"
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc()))
}
