import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.compile.JavaCompile

val nmsPackage = project.extra["nmsPackage"] as String
val versionLabel = project.extra["versionLabel"] as String
val versionPrefix = project.extra["versionPrefix"] as String
val factoryPriority = project.extra["factoryPriority"] as String

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
            "MINECRAFT_VERSION_PREFIX" to versionPrefix,
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
