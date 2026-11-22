plugins {
    java
    application
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.buga"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"
val testfxVersion = "4.0.18"
val assertjVersion = "3.26.3"
val monocleVersion = "21.0.2"


tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("com.buga.mmgenerator")
    mainClass.set("com.buga.mmgenerator.HelloApplication")
}
kotlin {
    jvmToolchain(21)
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.swing")
}

dependencies {
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("eu.hansolo:tilesfx:21.0.9") {
        exclude(group = "org.openjfx")
    }
    implementation("org.xerial:sqlite-jdbc:3.47.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.testfx:testfx-core:${testfxVersion}")
    testImplementation("org.testfx:testfx-junit5:${testfxVersion}")
    testImplementation("org.assertj:assertj-core:${assertjVersion}")
    testImplementation("org.testfx:openjfx-monocle:${monocleVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Configure headless execution when -Pheadless is passed
    if (project.hasProperty("headless")) {
        systemProperty("testfx.robot", "glass")
        systemProperty("testfx.headless", "true")
        systemProperty("glass.platform", "Monocle")
        systemProperty("monocle.platform", "Headless")
        systemProperty("prism.order", "sw")
        systemProperty("prism.text", "t2k")
        systemProperty("java.awt.headless", "true")
    }
    // Add module reads/exports for TestFX and JUnit access
    jvmArgs(
        "--add-reads", "com.buga.mmgenerator=org.testfx.junit5",
        "--add-reads", "com.buga.mmgenerator=org.testfx",
        "--add-reads", "com.buga.mmgenerator=org.testfx.monocle",
        "--add-reads", "com.buga.mmgenerator=org.junit.jupiter.api",
        "--add-reads", "com.buga.mmgenerator=org.junit.jupiter.engine",
        "--add-reads", "com.buga.mmgenerator=org.junit.platform.commons",
        "--add-reads", "com.buga.mmgenerator=org.junit.platform.engine",
        "--add-reads", "com.buga.mmgenerator=org.junit.platform.launcher",
        "--add-reads", "com.buga.mmgenerator=org.assertj.core",
        "--add-reads", "com.buga.mmgenerator=org.hamcrest",
        "--add-reads", "com.buga.mmgenerator=javafx.graphics",
        "--add-reads", "com.buga.mmgenerator=javafx.base",
        "--add-exports", "javafx.base/com.sun.javafx.logging=org.testfx.monocle",
        "--add-opens", "javafx.base/com.sun.javafx.logging=org.testfx.monocle",
        "--add-exports", "javafx.base/com.sun.javafx.logging=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.logging=ALL-UNNAMED",
        "--add-exports", "javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
        "--add-exports", "javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        "--add-exports", "org.testfx.junit5/org.testfx.framework.junit5=com.buga.mmgenerator",
        "--add-exports", "org.testfx/org.testfx.api=com.buga.mmgenerator",
        "--add-exports", "org.testfx/org.testfx.util=com.buga.mmgenerator",
        "--add-opens", "org.testfx/org.testfx.util=com.buga.mmgenerator",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.glass.ui=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.delegate=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.glass.ui.delegate=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.delegate=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui.delegate=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.monocle=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.glass.ui.monocle=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.javafx.util=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.javafx.util=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.javafx.tk=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.javafx.tk=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.javafx.application=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.javafx.application=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.prism=org.testfx.monocle",
        "--add-opens", "javafx.graphics/com.sun.prism=org.testfx.monocle",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.monocle=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui.monocle=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=com.buga.mmgenerator",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=org.testfx",
        "--add-opens", "javafx.graphics/com.sun.glass.ui=org.testfx",
        "--add-exports", "javafx.graphics/com.sun.javafx.application=org.testfx",
        "--add-opens", "javafx.graphics/com.sun.javafx.application=org.testfx",
        "--add-exports", "javafx.graphics/com.sun.javafx.tk=com.buga.mmgenerator",
        "--add-exports", "javafx.graphics/com.sun.prism=com.buga.mmgenerator",
        "--add-opens", "com.buga.mmgenerator/com.buga.mmgenerator.ui=org.junit.platform.commons",
        "--add-opens", "com.buga.mmgenerator/com.buga.mmgenerator=org.junit.platform.commons",
        "--add-opens", "com.buga.mmgenerator/com.buga.mmgenerator.ui=org.testfx.junit5",
        "--add-opens", "com.buga.mmgenerator/com.buga.mmgenerator=org.testfx.junit5"
    )
}

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app"
    }
}
