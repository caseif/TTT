import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// Apply Gradle plugins
plugins {
    java
    eclipse
    idea
    checkstyle

    id("org.cadixdev.licenser") version "0.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

defaultTasks("clean", "updateLicenses", "build", "shadowJar")

// Project information
group = "net.caseif.ttt"
version = "0.11.5-SNAPSHOT"

// Extended project information
val description: String by extra { "A Bukkit minigame based off the Garry\"s Mod gamemode Trouble In Terrorist Town." }
val inceptionYear: String by extra { "2013" }
val packaging: String by extra { "jar" }

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

// Project repositories
repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.caseif.net/")
    maven("https://repo.spongepowered.org/maven")
}

// Project dependencies
dependencies {
    shadow("org.spigotmc:spigot-api:1.14-pre5-SNAPSHOT")
    shadow("net.caseif.flint:flint:1.3")
    implementation("net.caseif.rosetta:rosetta:1.1.4")
    implementation("net.caseif.crosstitles:crosstitles:0.1.3")
    implementation("net.gravitydevelopment.updater:updater:2.4")
    implementation("org.bstats:bstats-bukkit:1.2")
    implementation("org.jnbt:jnbt:1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    checkstyle("org.spongepowered:checkstyle:6.1.1-sponge1")
}

// Read source files using UTF-8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
}

tasks.named<Copy>("processResources") {
    from("LICENSE")

    filesMatching("LICENSE") {
        expand("name" to project.name)
        expand("version" to project.version)
    }
}

tasks.withType<Jar> {
    classifier = "base"
}

tasks.withType<ShadowJar> {
    classifier = ""

    relocate("net.caseif.rosetta", "net.caseif.ttt.lib.net.caseif.rosetta")
    relocate("net.caseif.crosstitles", "net.caseif.ttt.lib.net.caseif.crosstitles")
    relocate("net.gravitydevelopment.updater", "net.caseif.ttt.lib.net.gravitydevelopment.updater")
    relocate("org.bstats", "net.caseif.ttt.lib.org.bstats")
    relocate("org.jnbt", "net.caseif.ttt.lib.org.jnbt")
}

tasks.create<Jar>("sourceJar") {
    from(sourceSets["main"].java)
    from(sourceSets["main"].resources)
    classifier = "sources"
}

artifacts {
    archives(tasks["shadowJar"])
    archives(tasks["sourceJar"])
}

// License header formatting
license {
    include("**/*.java")
    ignoreFailures(false)
}

// check code style
tasks.withType<Checkstyle> {
    configDirectory.set(file("etc"))
    configFile = file("etc/checkstyle.xml")

    exclude("**/*.properties")
    exclude("**/*.yml")
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.1"
}
