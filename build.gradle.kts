buildscript {
    extra.apply {
        set("kotlin_version", "1.9.0")
        set("appcompat_version", "1.6.1")
        set("material_version", "1.10.0")
        set("constraintlayout_version", "2.1.4")
        set("retrofit_version", "2.9.0")
        set("okhttp_version", "4.12.0")
        set("lifecycle_version", "2.6.2")
        set("coroutines_version", "1.7.3")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlin_version"]}")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
} 