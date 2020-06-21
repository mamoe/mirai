pluginManagement {
    repositories {
        mavenLocal()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            val version = requested.version
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${version}")
                "org.jetbrains.kotlin.plugin.serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${version}")
                "com.jfrog.bintray" -> useModule("com.jfrog.bintray.gradle:gradle-bintray-plugin:$version")
            }
        }
    }
}

rootProject.name = "mirai-console"

val disableOldFrontEnds = true

fun includeProject(projectPath: String, path: String? = null) {
    include(projectPath)
    if (path != null) project(projectPath).projectDir = file(path)
}

includeProject(":mirai-console", "backend/mirai-console")
includeProject(":mirai-console.codegen", "backend/codegen")
includeProject(":mirai-console-pure", "frontend/mirai-console-pure")

@Suppress("ConstantConditionIf")
if (!disableOldFrontEnds) {
    includeProject(":mirai-console-terminal", "frontend/mirai-console-terminal")

    val jdkVersion = kotlin.runCatching {
        System.getProperty("java.version").let { v ->
            v.toIntOrNull() ?: v.removePrefix("1.").substringBefore("-").toIntOrNull()
        }
    }.getOrNull() ?: -1

    println("JDK version: $jdkVersion")

    if (jdkVersion >= 9) {
        includeProject(":mirai-console-graphical", "frontend/mirai-console-graphical")
    } else {
        println("当前使用的 JDK 版本为 ${System.getProperty("java.version")},  请使用 JDK 9 以上版本引入模块 `:mirai-console-graphical`\n")
    }
}

enableFeaturePreview("GRADLE_METADATA")