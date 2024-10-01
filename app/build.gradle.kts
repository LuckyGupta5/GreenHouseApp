plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    // Add the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.ripenapps.greenhouse"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.ripenapps.greenhouse"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        dataBinding.enable = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.squareup.retrofit2:converter-gson")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-basement:18.3.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("androidx.compose.ui:ui-test-android:1.6.8")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    implementation("com.squareup.picasso:picasso:2.71828")
    //GLIDE TO SET IMAGE
    implementation("com.github.bumptech.glide:glide:4.13.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.8.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    implementation("com.androidplot:androidplot-core:1.5.10")
    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.8.7")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // Ok-Http Logs
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.67")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.supercharge:shimmerlayout:2.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0") // Use the latest version
    implementation("com.karumi:dexter:6.2.3")
    //maps
    implementation("com.google.maps.android:maps-ktx:5.0.0")

    //chart
    implementation("com.wang.avi:library:2.1.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    //socket
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }/*pinView*/
    implementation("io.github.chaosleung:pinview:1.4.4")
    implementation("com.hbb20:ccp:2.6.0")
    //payment gateway
    implementation("sa.com.clickpay:payment-sdk:6.5.0")

}