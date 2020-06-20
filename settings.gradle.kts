pluginManagement {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            val version = requested.version
            when (requested.id.id) {
                "org.jetbrains.kotlin.jvm" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${version}")
            }
        }
    }
}

rootProject.name = "mirai-console"

val onlyBackEnd = true

include(":mirai-console")
project(":mirai-console").projectDir = file("backend/mirai-console")

include(":codegen")
project(":codegen").projectDir = file("backend/codegen")

@Suppress("ConstantConditionIf")
if (!onlyBackEnd) {

    include(":mirai-console-pure")
    project(":mirai-console-pure").projectDir = file("frontend/mirai-console-pure")

    include(":mirai-console-terminal")
    project(":mirai-console-terminal").projectDir = file("frontend/mirai-console-terminal")

    try {
        val javaVersion = System.getProperty("java.version")
        var versionPos = javaVersion.indexOf(".")
        var javaVersionNum = javaVersion.substring(0, 1).toInt()

        if (javaVersion.startsWith("1.")) {
            javaVersionNum = javaVersion.substring(2, 3).toInt()
        } else {
            if (versionPos == -1) versionPos = javaVersion.indexOf("-")
            if (versionPos == -1) {
                println("jdk version unknown")
            } else {
                javaVersionNum = javaVersion.substring(0, versionPos).toInt()
            }
        }
        if (javaVersionNum >= 9) {
            include(":mirai-console-graphical")
            project(":mirai-console-graphical").projectDir = file("frontend/mirai-console-graphical")
        } else {
            println("JDK 版本为 $javaVersionNum")
            println("当前使用的 JDK 版本为 ${System.getProperty("java.version")},  请使用JDK 9以上版本引入模块 `:mirai-console-graphical`\n")
        }

    } catch (ignored: Exception) {
        println("无法确定 JDK 版本, 将不会引入 `:mirai-console-graphical`")
    }
}


enableFeaturePreview("GRADLE_METADATA")