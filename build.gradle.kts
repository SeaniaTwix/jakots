import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// ...
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.useIR = true

plugins {
    java
    kotlin("jvm") version "1.4.30"
}

group = "gd.now"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(group = "com.github.skjolber", name = "indent", version = "1.0.0")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-alpha2")
}
