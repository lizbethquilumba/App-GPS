// build.gradle.kts (Module :app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Asegúrate de que esta línea esté si estás usando Compose para la UI.
    // Si no estás usando Compose, puedes comentarla o eliminarla.
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ec.edu.utn.example.app_monitoreogps" // Verifica que este sea tu namespace real
    compileSdk = 35 // Mantén tu SDK de compilación, si es 35, está bien.

    defaultConfig {
        applicationId = "ec.edu.utn.example.app_monitoreogps" // Verifica que este sea tu ID de aplicación real
        minSdk = 26 // ¡CORREGIDO! Asegúrate de que sea 26 o superior para los adaptive-icons
        targetSdk = 35 // Mantén tu SDK objetivo, si es 35, está bien.
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // Asegúrate de que esta línea esté si estás usando Compose para la UI.
        // Si no estás usando Compose, puedes comentarla o eliminarla.
        compose = true
    }
    // Añade esto si quieres ver la vista previa de Compose en el IDE
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1" // O la versión de tu extensión de compilador Kotlin compatible
    }
}

dependencies {

    // Dependencias de AndroidX Core y Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Dependencias de Compose (si estás usando Compose para la UI)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Dependencias de AppCompat y Material Design (si estás usando la UI tradicional basada en Views)
    // Estas son las que tenías previamente, se mantienen por si las necesitas.
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout) // Si usas ConstraintLayout

    // Dependencias para el monitoreo GPS (Google Play Services Location)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Dependencia para el servidor HTTP integrado (NanoHttpd)
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // Dependencias para las peticiones HTTP (OkHttp3)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")


    // Dependencias para pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}