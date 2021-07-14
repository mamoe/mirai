plugins {
    `java-platform`
    `maven-publish`
}

description = "Mirai BOM"

rootProject.subprojects
    .filter { it.path != project.path }
    .forEach { project.evaluationDependsOn(it.path) }

dependencies {
    constraints {
        rootProject.subprojects
            .filter { it.path != project.path }
            .filter { it.extensions.findByName("publishing") != null }
            .forEach { subProject ->
                subProject.publishing.publications
                    .withType<MavenPublication>()
                    .forEach {
                        this@constraints.api("${it.groupId}:${it.artifactId}:${it.version}")
                    }
            }
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
