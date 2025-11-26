allprojects {
    group = property("group").toString()
    version = property("version").toString()

    repositories {
        mavenCentral()
        maven("papermc") { url = uri("https://repo.papermc.io/repository/maven-public/") }
        maven("spigot") { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
        maven("jitpack") { url = uri("https://jitpack.io") }
        maven("codemc") { url = uri("https://repo.codemc.org/repository/maven-public") }
        maven("auxilor") { url = uri("https://repo.auxilor.io/repository/maven-public/") }
        maven("placeholderapi") { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
        maven("helpch") { url = uri("https://repo.helpch.at/releases/") }
        mavenLocal()
    }
}

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

sourceSets {
    val main by getting {
        java.setSrcDirs(listOf(projectDir.toPath().resolve("../src/main/java").toFile()))
        resources.setSrcDirs(listOf(projectDir.toPath().resolve("../src/main/resources").toFile()))
    }
}

val pluginName: String by rootProject
val author: String by rootProject

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("com.willfp:libreforge:4.79.0")
    compileOnly("com.willfp:eco:6.77.2")
    compileOnly("com.willfp:EcoEnchants:12.26.1")
    compileOnly("io.github.baked-libs:dough-api:1.2.0")
    compileOnly("com.github.Slimefun:Slimefun4:RC-35")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(
                "main" to "icu.nyat.kusunoki.deenchantment.DeEnchantmentPlugin",
                "name" to pluginName,
                "version" to project.version,
                "author" to author
            )
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}
