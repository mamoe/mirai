import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

@Suppress("unused")
fun DependencyHandlerScope.kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

@Suppress("unused")
fun DependencyHandlerScope.ktor(id: String, version: String = Versions.ktor) = "io.ktor:ktor-$id:$version"

@Suppress("unused")
fun DependencyHandler.compileAndRuntime(any: Any) {
    add("compileOnly", any)
    add("runtimeOnly", any)
}
