plugins {
    kotlin("jvm") version "1.4.30"
    application
}

group = "org.hildan.hashcode"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.hildan.hashcode:hashcode-utils-kt:1.1.0")
}

application {
    mainClassName = "org.hildan.hashcode.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
