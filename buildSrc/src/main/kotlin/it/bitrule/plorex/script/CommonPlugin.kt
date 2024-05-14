package it.bitrule.plorex.script

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.credentials
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType

class CommonPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.configure()

    private fun Project.configure() {
        project.plugins.apply("java")

        project.tasks.apply {
            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
                options.compilerArgs.add("-parameters")
            }

            configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }
        }

        project.repositories {
            mavenCentral()
            maven {
                name = "plorexRepository"
                url = uri("https://maven.pkg.github.com/Plorex-Services/mvn-repo/")
                credentials(PasswordCredentials::class)
            }
        }
    }
}