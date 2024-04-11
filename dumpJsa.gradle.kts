import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Jar
import java.nio.file.Files
import java.nio.file.Paths

open class DumpJsa : Exec() {
    init {
        group = "application"
        description = "Dumps the .jsa file if it does not exist or is not up-to-date."
        val bootJarTask = project.tasks.named("bootJar").get()
        val jarTask = bootJarTask as Jar
        val jarFile = jarTask.archiveFile.get().asFile
        bootJarTask.onlyIf {
            !jarFile.exists() || jarTask.inputs.files.any { it.isFile && it.lastModified() > jarFile.lastModified() }
        }
        dependsOn(bootJarTask)
    }
}

tasks.register<DumpJsa>("dumpJsa") {
    val jsaPathMain = "build/resources/main/sharedArchive.jsa"
    val jarPath = "build/libs/${project.name}-${project.version}.jar"

    val jsaFile = Paths.get(jsaPathMain)
    val jarFile = Paths.get(jarPath)
    val isJsaOutdated =
        Files.notExists(jsaFile) || Files.getLastModifiedTime(jarFile).toMillis() > Files.getLastModifiedTime(jsaFile)
            .toMillis()

    if (isJsaOutdated) {
        val commandMain = listOf("java", "-Xshare:dump", "-XX:SharedArchiveFile=$jsaPathMain", "-jar", jarPath)
        commandLine(commandMain)
    }
}
