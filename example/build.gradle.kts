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

application {
    mainClass = "mvvm.example.App"
    applicationDefaultJvmArgs +=
        "--enable-native-access=javafx.graphics"
}

javafx {
    version = "25"
    modules("javafx.controls")
}
