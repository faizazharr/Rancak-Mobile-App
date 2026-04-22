import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // iOS system frameworks used by KMM code:
            // CoreBluetooth  — BLE printer (PrinterManager.ios.kt)
            // BackgroundTasks — BGTaskScheduler sync (SyncManager.ios.kt + MainViewController.kt)
            // AVFoundation   — Camera barcode scanner (BarcodeScanner.ios.kt)
            linkerOpts(
                "-framework", "CoreBluetooth",
                "-framework", "BackgroundTasks",
                "-framework", "AVFoundation"
            )
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            // WorkManager (offline sync)
            implementation(libs.androidx.work.runtime.ktx)
            // CameraX + ML Kit (barcode scanner)
            implementation(libs.cameraX.camera2)
            implementation(libs.cameraX.lifecycle)
            implementation(libs.cameraX.view)
            implementation(libs.mlkit.barcode.scanning)
            // Guava (ListenableFuture required by CameraX at compile time)
            implementation(libs.guava.android)
            // Google Sign-In via Credential Manager
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services)
            implementation(libs.googleid)
        }
        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            // Material3 — always auto-matched to compose plugin version
            implementation(compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            // Material Icons Extended — always auto-matched to compose plugin version
            implementation(compose.materialIconsExtended)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation
            implementation(libs.navigation.compose)

            // Kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Ktor (Network)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)

            // Koin (DI)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Coil (Image)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Persistent storage (multiplatform-settings)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            // Room (local SQLite — KMP)
            implementation(libs.room.runtime)

            // QR Code (QRIS)
            implementation(libs.qrose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            // SQLite bundled driver for iOS (Room KMP requires this on non-Android)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "id.rancak.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) load(f.inputStream())
    }
    val rancakApiKey: String = localProps.getProperty("RANCAK_API_KEY")
        ?: error("RANCAK_API_KEY is missing from local.properties")

    defaultConfig {
        applicationId = "id.rancak.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "RANCAK_API_KEY", "\"$rancakApiKey\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    // Room KSP annotation processor per target
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

