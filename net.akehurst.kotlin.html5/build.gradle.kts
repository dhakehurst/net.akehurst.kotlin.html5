/**
 * Copyright (C) 2020 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.buildconfig) apply false
    alias(libs.plugins.credentials)
    alias(libs.plugins.exportPublic) apply false
}
val kotlin_languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
val kotlin_apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
val jvmTargetVersion = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8

allprojects {

    group = rootProject.name
    version = rootProject.libs.versions.project.get()

    project.layout.buildDirectory = File(rootProject.projectDir, ".gradle-build/${project.name}")

}

fun getProjectProperty(s: String) = project.findProperty(s) as String?

subprojects {

    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.github.gmazzo.buildconfig")
    apply(plugin = "net.akehurst.kotlin.gradle.plugin.exportPublic")

    repositories {
        mavenLocal {
            content{
                includeGroupByRegex("net\\.akehurst.+")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }

    configure<BuildConfigExtension> {
        val now = java.time.Instant.now()
        fun fBbuildStamp(): String = java.time.format.DateTimeFormatter.ISO_DATE_TIME.withZone(java.time.ZoneId.of("UTC")).format(now)
        fun fBuildDate(): String = java.time.format.DateTimeFormatter.ofPattern("yyyy-MMM-dd").withZone(java.time.ZoneId.of("UTC")).format(now)
        fun fBuildTime(): String= java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss z").withZone(java.time.ZoneId.of("UTC")).format(now)

        packageName("${project.group}")
        buildConfigField("String", "version", "\"${project.version}\"")
        buildConfigField("String", "buildStamp", "\"${fBbuildStamp()}\"")
        buildConfigField("String", "buildDate", "\"${fBuildDate()}\"")
        buildConfigField("String", "buildTime", "\"${fBuildTime()}\"")
    }

    configure<KotlinMultiplatformExtension> {
        jvm("jvm8") {
            compilations {
                val main by getting {
                    compilerOptions.configure {
                        languageVersion.set(kotlin_languageVersion)
                        apiVersion.set(kotlin_apiVersion)
                        jvmTarget.set(jvmTargetVersion)
                    }
                }
                val test by getting {
                    compilerOptions.configure {
                        languageVersion.set(kotlin_languageVersion)
                        apiVersion.set(kotlin_apiVersion)
                        jvmTarget.set(jvmTargetVersion)
                    }
                }
            }
        }
        js("js",IR) {
            binaries.library()
            generateTypeScriptDefinitions()
            compilerOptions {
                target.set("es2015")
            }
            nodejs()
            browser {
                webpackTask {
                    mainOutputFileName = "${project.group}-${project.name}.js"
                }
            }
        }
        //macosX64("macosX64") {
        // uncomment stuff below too
        //}
        sourceSets {
            val commonMain by getting {
                kotlin.srcDir("${project.layout.buildDirectory.get()}/generated/kotlin")
            }
        }
    }

    val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
        //dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        //from(dokkaHtml.outputDirectory)
    }
    tasks.named("publish").get().dependsOn("javadocJar")

    dependencies {
        "commonTestImplementation"(kotlin("test"))
        "commonTestImplementation"(kotlin("test-annotations-common"))
    }

    configure<SigningExtension> {
        setRequired( {  gradle.taskGraph.hasTask("uploadArchives") })
        useGpgCmd()
        val publishing = project.properties["publishing"] as PublishingExtension
        sign(publishing.publications)
    }

    val creds = project.properties["credentials"] as nu.studer.gradle.credentials.domain.CredentialsContainer
    val sonatype_pwd = creds.forKey("SONATYPE_PASSWORD")
        ?: getProjectProperty("SONATYPE_PASSWORD")
        ?: error("Must set project property with Sonatype Password (-P SONATYPE_PASSWORD=<...> or set in ~/.gradle/gradle.properties)")
    project.ext.set("signing.password", sonatype_pwd)

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "sonatype"
                setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = getProjectProperty("SONATYPE_USERNAME")
                        ?: error("Must set project property with Sonatype Username (-P SONATYPE_USERNAME=<...> or set in ~/.gradle/gradle.properties)")
                    password = sonatype_pwd
                }
            }
            maven {
                name = "Other"
                setUrl(getProjectProperty("PUB_URL")?: "<use -P PUB_URL=<...> to set>")
                credentials {
                    username = getProjectProperty("PUB_USERNAME")
                        ?: error("Must set project property with Username (-P PUB_USERNAME=<...> or set in ~/.gradle/gradle.properties)")
                    password = getProjectProperty("PUB_PASSWORD")?: creds.forKey(getProjectProperty("PUB_USERNAME"))
                }
            }
        }
        publications.withType<MavenPublication> {
            artifact(javadocJar.get())

            pom {
                name.set("HTML4")
                description.set("Builder for html 5")
                url.set("https://github.com/dhakehurst/net.akehurst.kotlin.html5")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Dr. David H. Akehurst")
                        email.set("dr.david.h@akehurst.net")
                    }
                }
                scm {
                    url.set("https://github.com/dhakehurst/net.akehurst.kotlin.html5")
                }
            }
        }
    }

    val signTasks = arrayOf(
        "signKotlinMultiplatformPublication",
        "signJvm8Publication",
        "signJsPublication",
//        "signWasmJsPublication",
//         "signMacosArm64Publication"
    )
    tasks.forEach {
        when {
            it.name.matches(Regex("publish(.)+")) -> {
                println("${it.name}.mustRunAfter(${signTasks.toList()})")
                it.mustRunAfter(*signTasks)
            }
        }
    }
}