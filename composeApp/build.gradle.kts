import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "1.8.20"
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.desktop.windows_x64)
            implementation(compose.desktop.linux_arm64)
            implementation(compose.desktop.linux_x64)
            implementation(compose.desktop.macos_arm64)
            implementation(compose.desktop.macos_x64)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation("org.jetbrains.exposed:exposed-core:0.60.0")
            implementation("org.jetbrains.exposed:exposed-jdbc:0.60.0")
            implementation("org.jetbrains.exposed:exposed-java-time:0.60.0")
            implementation("mysql:mysql-connector-java:8.0.32")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
            val voyagerVersion = "1.1.0-beta02"
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            implementation("com.h2database:h2:2.1.214")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

group = "ch.js.rm2025"
version = "1"

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "ch.js.rm2025.MainKt"
    }

    val desktopMainSourceSet = kotlin.sourceSets["desktopMain"]

    from(desktopMainSourceSet.resources.srcDirs)

    from({
        configurations["desktopRuntimeClasspath"].filter { it.exists() }.map { zipTree(it) }
    })
}

compose.desktop {
    application {
        mainClass = "ch.js.rm2025.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ch.js.rm2025"
            packageVersion = "1.0.0"
        }
    }
}
