val pluginName: String by settings
rootProject.name = pluginName

include("core")
include("nms-api")

val nmsModules = listOf(
    "nms-v1_20_R3",
    "nms-v1_21",
    "nms-v1_21_3",
    "nms-v1_21_4",
    "nms-v1_21_5",
    "nms-v1_21_6",
    "nms-v1_21_7",
    "nms-v1_21_8",
    "nms-v1_21_10"
)

nmsModules.forEach { include(it) }

project(":nms-api").projectDir = file("nms/nms-api")
nmsModules.forEach { project(":$it").projectDir = file("nms/$it") }

pluginManagement {
    val shadowJarVersion: String by settings
    val paperweightVersion: String by settings
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("com.gradleup.shadow") version shadowJarVersion
        id("io.papermc.paperweight.userdev") version paperweightVersion
    }
}
