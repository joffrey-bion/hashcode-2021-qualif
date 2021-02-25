plugins {
    kotlin("jvm") version "1.4.30"
    application
    id("org.hildan.hashcode-submit") version "1.0.0"
}

group = "org.hildan.hashcode"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.hildan.hashcode:hashcode-utils-kt:1.1.1")
}

application {
    mainClassName = "org.hildan.hashcode.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
