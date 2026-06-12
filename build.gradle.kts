plugins {
    id("java")
    id("application")
    // Plugin JavaFX: gestisce moduli e dipendenze native della GUI.
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "it.unicam.cs.mpgc.rpg126036"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Toolchain fissata a Java 25 (LTS) come da specifica di progetto. Con il
// foojay-resolver (vedi settings.gradle.kts) Gradle scarica il JDK se assente.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Forza UTF-8 in compilazione: i testi di gioco contengono accenti italiani.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Moduli JavaFX usati dalla GUI. La versione segue la toolchain Java.
javafx {
    version = "25"
    modules = listOf("javafx.controls")
}

// Entry point dell'applicazione: abilita ./gradlew run
application {
    mainClass = "it.unicam.cs.mpgc.rpg126036.app.Main"
}

tasks.test {
    useJUnitPlatform()
}
