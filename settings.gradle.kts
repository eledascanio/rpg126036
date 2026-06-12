// Risolutore di toolchain Foojay: consente a Gradle di scaricare automaticamente
// il JDK richiesto (Java 25) se non presente, rendendo il progetto compilabile su
// qualsiasi computer senza installazione manuale del JDK.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "rpg126036"