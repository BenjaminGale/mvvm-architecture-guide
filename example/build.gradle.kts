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

configurations {
    create("mockitoAgent")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    "mockitoAgent"("org.mockito:mockito-core:5.18.0") { isTransitive = false }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    jvmArgs("-javaagent:${configurations["mockitoAgent"].asPath}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
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
