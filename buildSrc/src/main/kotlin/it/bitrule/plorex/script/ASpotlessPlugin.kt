package it.bitrule.plorex.script

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ASpotlessPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.configure()
    }

    private fun Project.configure() {
        apply<SpotlessPlugin>()

        project.repositories.apply {
            mavenCentral()
        }

        extensions.configure<SpotlessExtension> {
            java {
                trimTrailingWhitespace()
                removeUnusedImports()
            }
        }
    }
}