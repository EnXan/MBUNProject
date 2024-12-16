import org.apache.tools.ant.util.JavaEnvUtils.VERSION_11

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "2.1.0" // Konsistenz mit der Version im Klassenpfad
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlinx-serialization")
}



android {
    namespace = "com.example.projektmbun"
    compileSdk = 35

    defaultConfig {

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        applicationId = "com.example.projektmbun"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        viewBinding = true
        android.buildFeatures.buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.androidx.room.ktx)
    implementation(libs.gson)
    implementation(libs.wasabeef.glide.transformations)
    implementation(libs.wheelpicker)
    implementation (libs.glide.v4151)
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.robolectric)
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    implementation(libs.retrofit2.converter.gson)
    ksp(libs.androidx.room.compiler)
    androidTestImplementation (libs.androidx.room.testing)
    testImplementation(libs.kotlin.test.junit)
    implementation (libs.androidx.emoji2)
    implementation (libs.androidx.emoji2.bundled)
    implementation (libs.material)
    implementation (libs.commons.text)
    implementation(libs.squareup.retrofit)
    implementation(libs.retrofit.v290)
    implementation (libs.flexbox)
    implementation(libs.materialnumberpicker)
    testImplementation(libs.turbine)
    implementation(libs.aws.android.sdk.s3)
    implementation(libs.github.glide)
    implementation (libs.fab)
    implementation (libs.androidx.activity.ktx)
    implementation (libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation (libs.androidx.work.runtime.ktx)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.androidx.camera.video)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation (libs.material3)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
testImplementation (libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.mockito.android)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

