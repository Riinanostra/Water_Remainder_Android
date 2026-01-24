import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    id("org.owasp.dependencycheck") version "9.2.0" apply false
}

subprojects {
    apply(plugin = "org.owasp.dependencycheck")

    extensions.configure<DependencyCheckExtension>("dependencyCheck") {
        failBuildOnCVSS = 7.0F
        suppressionFiles = listOf("${rootProject.projectDir}/dependency-check-suppressions.xml")
        analyzers.apply {
            assemblyEnabled = false
            nodeEnabled = false
        }
    }
}
