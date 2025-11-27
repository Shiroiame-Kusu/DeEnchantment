plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    api(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

extra["nmsPackage"] = "v1_20_R3"
extra["craftPackage"] = "org.bukkit.craftbukkit.v1_20_R3"
extra["versionLabel"] = "1.20.4"
extra["versionPrefix"] = "1.20.4"
extra["factoryPriority"] = "12004"

apply(from = rootProject.file("gradle/nms-legacy-module.gradle.kts"))
