plugins {
    java
}

allprojects {
    group = "dev.sato.worldprotect"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    dependencies {
        // JUnit 5 for unit tests
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    }

    tasks.test {
        useJUnitPlatform()
        systemProperty("file.encoding", "UTF-8")
        jvmArgs("-Dfile.encoding=UTF-8")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.register("printClasspath") {
        doLast {
            println("Test Runtime Classpath for ${project.name}:")
            sourceSets.test.get().runtimeClasspath.forEach { println(it) }
        }
    }
}

// Module-specific dependencies configuration
project(":worldprotect-minecraft") {
    dependencies {
        implementation(project(":worldprotect-core"))
    }
}

project(":worldprotect-protection") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
    }
}

project(":worldprotect-audit") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
    }
}

project(":worldprotect-worldedit-bridge") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
    }
}

project(":worldprotect-compat-api") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
    }
}

project(":worldprotect-neoforge") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
        implementation(project(":worldprotect-protection"))
        implementation(project(":worldprotect-audit"))
        implementation(project(":worldprotect-compat-api"))
    }
}

project(":worldprotect-fabric") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
        implementation(project(":worldprotect-protection"))
        implementation(project(":worldprotect-audit"))
        implementation(project(":worldprotect-compat-api"))
    }
}

project(":verification") {
    dependencies {
        implementation(project(":worldprotect-core"))
        implementation(project(":worldprotect-minecraft"))
        implementation(project(":worldprotect-protection"))
        implementation(project(":worldprotect-audit"))
        implementation(project(":worldprotect-compat-api"))
        implementation(project(":worldprotect-neoforge"))
        implementation(project(":worldprotect-fabric"))
        implementation(project(":worldprotect-worldedit-bridge"))
    }
}
