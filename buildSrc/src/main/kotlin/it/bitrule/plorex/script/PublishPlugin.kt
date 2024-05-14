package it.bitrule.plorex.script

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.*

class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.afterEvaluate {
        configure()
    }

    private fun Project.configure() {
        apply<JavaBasePlugin>()
        apply<MavenPublishPlugin>()

        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "plorexRepository"
                    url = uri("https://maven.pkg.github.com/Plorex-Services/mvn-repo/")
                    credentials(PasswordCredentials::class)
                }
            }

            publications {
                create<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = project.name.lowercase()
                    version = project.version.toString()
                    from(components["java"])
                }
            }
        }
    }
}