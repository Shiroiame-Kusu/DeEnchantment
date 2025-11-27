plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    api(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}

extra["nmsPackage"] = "v1_20_R1"
extra["craftPackage"] = "org.bukkit.craftbukkit.v1_20_R1"
extra["versionLabel"] = "1.20.1"
extra["versionPrefix"] = "1.20.1"
extra["factoryPriority"] = "12001"

apply(from = rootProject.file("gradle/nms-legacy-module.gradle.kts"))
