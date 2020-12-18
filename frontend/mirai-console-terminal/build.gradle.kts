plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation("org.jline:jline:3.15.0")
    implementation("org.fusesource.jansi:jansi:1.18")

    compileAndTestRuntime(project(":mirai-console"))
    compileAndTestRuntime(`mirai-core-api`)
    compileAndTestRuntime(kotlin("stdlib-jdk8", Versions.kotlinStdlib)) // must specify `compileOnly` explicitly

    testApi(`mirai-core`)
    testApi(project(":mirai-console"))
}

version = Versions.consoleTerminal

description = "Console Terminal CLI frontend for mirai"

setupPublishing("mirai-console-terminal", bintrayPkgName = "mirai-console-terminal")

// endregion