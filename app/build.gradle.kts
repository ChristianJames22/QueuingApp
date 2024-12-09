plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.appque"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appque"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Enable view binding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Material Design
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx") // Updated
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Media3 example dependency
    implementation("androidx.media3:media3-common:1.0.0") // Example version, update as needed

    // Constraint layout and activity libraries
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation ("at.favre.lib:bcrypt:0.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
