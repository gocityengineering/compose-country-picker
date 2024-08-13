plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("plugin.parcelize")
    alias(libs.plugins.dokka)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.compose.compiler)
}

object Meta {
    const val GITHUB_REPO = "github.com/gocityengineering/compose-country-picker.git"
}

description = "A lightweight, localised Country Picker for Jetpack Compose"
group = "com.gocity.countrypicker"
version = libs.versions.app.version.name.get()

centralPortal {
    name = "countrypicker"
    versionMapping {
        allVariants {
            fromResolutionOf("releaseRuntimeClasspath")
        }
    }
    pom {
        url = "https://${Meta.GITHUB_REPO}"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://github.com/gocityengineering/compose-country-picker/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = "barry-irvine"
                name = "Barry Irvine"
                organization = "Go City"
                organizationUrl ="https://www.gocity.com"
            }
        }
        scm {
            connection = "scm:git:${Meta.GITHUB_REPO}"
            developerConnection = "scm:git:ssh://${Meta.GITHUB_REPO}"
            url = "https://${Meta.GITHUB_REPO}"
        }
    }
}

android {
    namespace = "com.gocity.countrypicker"
    compileSdk = libs.versions.sdk.compile.get().toInt()
    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
