import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.compile.JavaCompile

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

configurations.compileClasspath {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
}

dependencies {
    api(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
}

extra["nmsPackage"] = "v1_21_10"
extra["versionLabel"] = "1.21.10"
extra["versionPrefix"] = "1.21.10"
extra["factoryPriority"] = "12110"

apply(from = rootProject.file("gradle/nms-modern-module.gradle.kts"))
