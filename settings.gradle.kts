val pluginName: String by settings
rootProject.name = pluginName

pluginManagement {
    val shadowJarVersion: String by settings
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.gradleup.shadow") version shadowJarVersion
    }
}
include("plugin")
