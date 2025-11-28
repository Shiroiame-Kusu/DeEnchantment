import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile

val nmsPackage = project.extra["nmsPackage"] as String
val versionLabel = project.extra["versionLabel"] as String
val versionPrefixes = project.extra["versionPrefixes"] as String  // Comma-separated, e.g., "1.21", "1.21.1"
val factoryPriority = project.extra["factoryPriority"] as String

// Convert comma-separated prefixes to Java array literal: "1.21", "1.21.1" -> "\"1.21\", \"1.21.1\""
val versionPrefixesJava = versionPrefixes.split(",")
    .map { it.trim() }
    .joinToString(", ") { "\"$it\"" }

val generatedSources = layout.buildDirectory.dir("generated/sources/$nmsPackage/java")

val generateModernBridge by tasks.registering(Sync::class) {
    val templateDir = rootProject.layout.projectDirectory.dir("nms-templates/modern")
    from(templateDir) {
        include("*.java.template")
        eachFile {
            path = path.removeSuffix(".template")
        }
        filter<ReplaceTokens>("tokens" to mapOf(
            "NMS_PACKAGE" to nmsPackage,
            "MINECRAFT_VERSION_LABEL" to versionLabel,
            "MINECRAFT_VERSION_PREFIXES" to versionPrefixesJava,
            "FACTORY_PRIORITY" to factoryPriority
        ))
    }
    into(generatedSources)
    filteringCharset = "UTF-8"
}

extensions.configure<SourceSetContainer>("sourceSets") {
    named("main") {
        java.srcDir(generatedSources)
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(generateModernBridge)
}
