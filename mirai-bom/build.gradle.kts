plugins {
    `java-platform`
    `maven-publish`
}

val projectName = project.name
description = "Mirai BOM"

dependencies {
    constraints {
        api(project(":mirai-core"))
        api(project(":mirai-core-api"))
        api(project(":mirai-core-utils"))
        api(project(":mirai-console"))
    }
}

publishing {
    publications {
        create<MavenPublication>("myPlatform") {
            groupId = rootProject.group.toString()
            artifactId = "mirai-bom"
            version = Versions.project
            from(components["javaPlatform"])
            setupPom(project)
            configGpgSign(project)
        }
    }
}
