import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
    id("java")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("cobbleguard") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

repositories {
    mavenCentral()
    flatDir { dirs("libs") }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.nucleoid.xyz/")
    maven("https://jitpack.io")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://cursemaven.com")
    maven("https://dl.cloudsmith.io/public/tslat/sbl/maven/")
    maven("https://repo.glaremasters.me/repository/bloodshot")
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modImplementation("net.tslat.smartbrainlib:SmartBrainLib-fabric" +
            "-${project.property("minecraft_version")}" +
            ":${project.property("sbl_version")}")
    modImplementation("com.cobblemon:fabric:${project.property("cobblemon_version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.yaml:snakeyaml:2.2")

    // Claim API
    modImplementation("curse.maven:FLan-${project.property("flan_cf_id")}:${project.property("flan_cf_version")}")
    modImplementation("curse.maven:OpenParties-${project.property("opac_cf_id")}:${project.property("opac_cf_version")}")
    modImplementation("curse.maven:forgeconfigapiport-${project.property("forgeconfigapiport_cf_id")}:${project.property("forgeconfigapiport_cf_version")}")

    // GriefDefender
    //modImplementation("luckperms:luckperms")
    //modImplementation("fabric-permissions-api-v0:fabric-permissions-api")
    //modImplementation("griefdefender:griefdefender")

    // GriefDefender Apis
    implementation("com.electronwill.night-config:core:3.8.1")
    implementation("com.electronwill.night-config:toml:3.8.1")
    implementation("com.electronwill.night-config:json:3.8.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
kotlin {
    jvmToolchain(21)
}