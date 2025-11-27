import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.compile.JavaCompile
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    `java-library`
    id("com.gradleup.shadow")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

val pluginName: String by rootProject
val author: String by rootProject
val outputDir = rootProject.layout.projectDirectory.dir("output")
val gitSha: String = project.gitSha()
val buildTimestamp: String = project.buildTimestamp()

dependencies {
    implementation(project(":nms-api"))
    runtimeOnly(project(":nms-v1_20_R1"))
    runtimeOnly(project(":nms-v1_20_R2"))
    runtimeOnly(project(":nms-v1_20_R3"))
    runtimeOnly(project(":nms-v1_21"))
    runtimeOnly(project(":nms-v1_21_3"))
    runtimeOnly(project(":nms-v1_21_4"))
    runtimeOnly(project(":nms-v1_21_5"))
    runtimeOnly(project(":nms-v1_21_6"))
    runtimeOnly(project(":nms-v1_21_7"))
    runtimeOnly(project(":nms-v1_21_8"))
    runtimeOnly(project(":nms-v1_21_10"))

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

@Suppress("UnstableApiUsage")
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

    val cleanOutput by registering(Delete::class) {
        delete(outputDir)
    }

    val shadowJarTask = named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        mergeServiceFiles()
        archiveFileName.set("${pluginName}-${project.version}-${buildTimestamp}-${gitSha}.jar")
        relocate("org.bstats", "icu.nyat.kusunoki.deenchantment.lib.bstats")
    }

    val copyShadowJar by registering(Copy::class) {
        dependsOn(cleanOutput, shadowJarTask)
        from(shadowJarTask.flatMap { it.archiveFile })
        into(outputDir)
    }

    build {
        dependsOn(shadowJarTask, copyShadowJar)
    }
}

fun Project.gitSha(): String {
    return try {
        val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
            .directory(rootDir)
            .redirectErrorStream(true)
            .start()
        process.waitFor()
        process.inputStream.bufferedReader().use { reader ->
            reader.readText().trim().ifEmpty { "unknown" }
        }
    } catch (_: Exception) {
        "unknown"
    }
}

fun Project.buildTimestamp(): String {
    return DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        .withZone(ZoneOffset.UTC)
        .format(Instant.now())
}
