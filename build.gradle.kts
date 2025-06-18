import org.slf4j.event.Level as LogLevel

plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.89"
    idea
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.0"
    id("at.petra-k.pkpcpbp.PKJson5Plugin") version "0.2.0-pre-103"
}

val modGroup = property("mod_group_id") as String
val modId = property("mod_id") as String
val modVersion = property("mod_version") as String

version = modVersion
group = modGroup

repositories {
    mavenLocal()
}

base {
    archivesName.set(modId)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = project.property("neo_version")!! as String

    parchment {
        mappingsVersion.set(project.property("parchment_mappings_version")!! as String)
        minecraftVersion.set(project.property("parchment_minecraft_version")!! as String)
    }

    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }

    runs {
        create("client") {
            client()
            gameDirectory.set(project.file("runs/client"))

            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("server") {
            server()
            gameDirectory.set(project.file("runs/server"))

            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        create("gameTestServer") {
            type = "gameTestServer"
            gameDirectory.set(project.file("runs/gameTestServer"))

            systemProperty("neoforge.enabledGameTestNamespaces", modId)
        }

        create("data") {
            data()
            gameDirectory.set(project.file("runs/data"))

            programArguments.addAll(
                "--mod", modId, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath//, "--existing-mod", modId
            )
        }

        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = LogLevel.DEBUG
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources")
    }
}

// Sets up a dependency configuration called "localRuntime".
// This configuration should be used instead of "runtimeOnly" to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
val ConfigurationContainer.localRuntime by configurations.registering

configurations.runtimeClasspath {
    extendsFrom(configurations.localRuntime.get())
}

repositories {
    mavenLocal()
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    //maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    //maven("https://mvn.devos.one/snapshots") // Registrate
    //maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // ForgeConfigAPIPort
    //maven(url = "https://maven.wispforest.io/releases/")
    maven {
        name = "Sinytra"
        setUrl("https://maven.su5ed.dev/releases")
    }

    maven {
        // location of the maven that hosts JEI files since January 2023
        name = "Jared's maven"
        setUrl("https://maven.blamejared.com/")
    }
    maven {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
        setUrl("https://modmaven.dev")
    }
    maven(url = "https://maven.shedaniel.me/")
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top/")
    }
    maven {
        name = "C4's Maven"
        setUrl("https://maven.theillusivec4.top/")
    }
    maven {
        name = "Architectury"
        setUrl("https://maven.architectury.dev/")
    }
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:5.8.0")

    compileOnly("mezz.jei:jei-1.21.1-neoforge-api:19.21.1.310")
    runtimeOnly("mezz.jei:jei-1.21.1-neoforge:19.21.1.310")

    implementation("vazkii.patchouli:Patchouli:1.21-88-NEOFORGE")
    implementation("com.samsthenerd.inline:inline-neoforge:1.21.1-1.2.2-74")

    implementation("dev.architectury:architectury-neoforge:13.0.8")
    //implementation("at.petra-k:paucal:0.7.1-pre-23+1.21.1-neoforge")
    implementation("at.petra-k:paucal:0.7.2+1.21.1-neoforge") // TODO port: mavenLocal

    compileOnly("top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1:api")
    runtimeOnly("top.theillusivec4.curios:curios-neoforge:9.5.1+1.21.1")

    compileOnly("com.illusivesoulworks.caelus:caelus-neoforge:7.0.1+1.21.1:api")
    runtimeOnly("com.illusivesoulworks.caelus:caelus-neoforge:7.0.1+1.21.1")

    implementation("com.github.Virtuoel:Pehkui:3.8.3-1.21-neoforge")
}

val generateModMetadata by tasks.registering(ProcessResources::class) {
    val replaceProperties = mapOf(
        "minecraft_version" to project.property("minecraft_version") as String,
        "minecraft_version_range" to project.property("minecraft_version_range") as String,
        "neo_version" to project.property("neo_version") as String,
        "neo_version_range" to project.property("neo_version_range") as String,
        "loader_version_range" to project.property("loader_version_range") as String,
        "mod_id" to project.property("mod_id") as String,
        "mod_name" to project.property("mod_name") as String,
        "mod_license" to project.property("mod_license") as String,
        "mod_version" to project.property("mod_version") as String,
        "mod_authors" to project.property("mod_authors") as String,
        "mod_description" to project.property("mod_description") as String
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main {
    resources {
        srcDir(generateModMetadata)
    }
}
neoForge.ideSyncTask(generateModMetadata)

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // Increase error limit for easier porting
    options.compilerArgs.add("-Xmaxerrs")
    options.compilerArgs.add("10000")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
