plugins {
    id("com.gradleup.shadow") apply false
}

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
