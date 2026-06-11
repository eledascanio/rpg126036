plugins {
    id("java")
    id("application")
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

// Toolchain fissata a Java 21 (LTS) per una compilazione riproducibile.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Forza UTF-8 in compilazione: i testi di gioco contengono accenti italiani.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Entry point dell'applicazione: abilita ./gradlew run
application {
    mainClass = "it.unicam.cs.mpgc.rpg126036.app.Main"
}

tasks.test {
    useJUnitPlatform()
}
