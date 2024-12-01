plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.butter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.butter"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures{
        viewBinding = true
    }

    tasks.withType<Test>{
        useJUnitPlatform()
    }
}

dependencies {
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.google.zxing:core:3.3.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-messaging")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("org.osmdroid:osmdroid-android:6.1.14")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.espresso.intents)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.espresso.contrib){
        exclude(group = "com.google.protobuf")
    }
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.15.0")  // Add Glide dependency
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")  // Add annotation processor (for Glide)
    implementation("com.google.firebase:firebase-storage:20.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.1")
}