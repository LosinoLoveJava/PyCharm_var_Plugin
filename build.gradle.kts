plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "dev.pycharmvar"
version = providers.gradleProperty("pluginVersion").orElse("0.1.0").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        pycharmCommunity("2023.3.7")
        bundledPlugin("PythonCore")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        pluginVerifier("1.410")
    }
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    register<Copy>("prepareRelease") {
        val pluginArchive = tasks.named<Zip>("buildPlugin")
        dependsOn(pluginArchive)
        from(pluginArchive.flatMap { it.archiveFile })
        into(layout.projectDirectory.dir("releases"))
    }

    jar {
        from("LICENSE") {
            into("META-INF")
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "233"
            untilBuild = providers.gradleProperty("pluginUntilBuild").orElse("253.*")
        }

        changeNotes = "Initial implementation of Python .var postfix completion."
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    pluginVerification {
        ides {
            local(intellijPlatform.platformPath.toFile())
            val localIde = providers.gradleProperty("verificationIdePath")
            if (localIde.isPresent) {
                local(localIde)
            }
        }
    }
}
