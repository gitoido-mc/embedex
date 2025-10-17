import com.github.jengelman.gradle.plugins.shadow.ShadowBasePlugin.Companion.shadow
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.net.URI

plugins {
    id("java")
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("com.gradleup.shadow") version "9.2.2"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

apply {
    plugin("com.gradleup.shadow")
    plugin("java")
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = URI("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
        content {
            includeGroup("software.bernie.geckolib")
        }
    }
    maven("https://maven.architectury.dev/")
    maven("https://maven.impactdev.net/repository/development/")
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
}

loom {
    splitEnvironmentSourceSets()
    runs {
        getByName("client") {
            programArgs(
                "--username", "AshKetchum",
                "--uuid", "93e4e551-589a-41cb-ab2d-435266c8e035"
            )
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings("net.fabricmc:yarn:${properties["yarn_mappings"]}:v2")

    implementation(
        platform("org.http4k:http4k-bom:6.19.0.0")
    )
    shadow(platform("org.http4k:http4k-bom:6.19.0.0"))
    implementation("org.http4k:http4k-core")
    shadow("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    shadow("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-ops-resilience4j")
    shadow("org.http4k:http4k-ops-resilience4j")
    implementation("org.http4k:http4k-format-gson")
    shadow("org.http4k:http4k-format-gson")

    // Mod deps
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["fabric_kotlin_version"]}")
    modImplementation("com.cobblemon:fabric:${properties["cobblemon_version"]}")
}

tasks {
    shadowJar {
        configurations.apply {
            this.add(project.configurations.shadow)
        }
        exclude("META-INF")
    }

    remapJar {
        // wait until the shadowJar is done
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)
        // Set the input jar for the task. Here use the shadow Jar that include the .class of the transitive dependency
        inputFile = shadowJar.get().archiveFile
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version
                )
            )
        }
    }

    jar {
        inputs.property("archivesName", project.base.archivesName)

        from("LICENSE")
    }

    compileJava {
        options.release = 21
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    java {
        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
        // if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}