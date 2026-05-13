plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "mvvm.architecture.guide"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "mvvm.example.App"
    applicationDefaultJvmArgs +=
        "--enable-native-access=javafx.graphics"
}

javafx {
    version = "25"
    modules("javafx.controls")
}
