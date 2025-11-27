val pluginName: String by settings
rootProject.name = pluginName

include("core")
include("nms-api")
include("nms-v1_20_R1")
include("nms-v1_21_4")

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
