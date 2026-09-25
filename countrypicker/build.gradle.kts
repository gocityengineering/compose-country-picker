import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.compose.compiler)
}

val appVersionName: String = providers
    .fileContents(rootProject.layout.projectDirectory.file("gradle/version.properties"))
    .asText
    .map { text -> Properties().apply { load(text.reader()) }.getProperty("APP_VERSION_NAME") }
    .get()

object Meta {
    const val GITHUB_REPO = "github.com/gocityengineering/compose-country-picker.git"
}

description = "A lightweight, localised Country Picker for Jetpack Compose"
group = "com.gocity.countrypicker"
version = appVersionName

// The publisher only knows Dokka's V1 javadoc task, which Dokka 2 has removed, so give it a
// javadoc jar built from Dokka 2's output instead
val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "Creates a `-javadoc` jar from Dokka's Javadoc output"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

centralPortal {
    name = "countrypicker"
    javadocJarTask = dokkaJavadocJar
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
    implementation(libs.libphonenumber)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    testImplementation(libs.google.truth)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
