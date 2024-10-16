plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.softylur.roohchat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.softylur.roohchat"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.activity.ktx)
    //implementation(libs.fragment.ktx)

    // Import the Firebase BoM Dependencies
    implementation(platform(libs.firebase.bom)) // Firebase BOM
    implementation(libs.google.firebase.auth) // Firebase Authentication
    implementation(libs.google.firebase.database) // Firebase Database
    implementation(libs.firebase.firestore) // Firebase FireStore
    implementation(libs.firebase.analytics) // Firebase Analytics
    implementation(libs.firebase.storage) // Firebase Storage

    implementation (libs.ccp)
    implementation (libs.lottie)

    implementation (libs.circleimageview) // Circle Image
    implementation (libs.glide) // Image from Glide
    implementation (libs.imagepicker) // Pick Gallery Image & Capture Camera Image
    implementation (libs.otpview) // OTP Viewer
    implementation(libs.stickyScrollView) // Sticky Scroll View Header and Footer

    //implementation ("com.github.bumptech.glide:glide:4.16.0")
    //implementation ("com.github.pgreze:android-reactions:1.6")
    //implementation ("com.github.3llomi:CircularStatusView:V1.0.3")
    //implementation ("com.github.OMARIHAMZA:StoryView:1.0.2-alpha")
    //implementation ("com.github.sharish:ShimmerRecyclerView:v1.3")

}