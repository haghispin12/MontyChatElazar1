plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.montychat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.montychat"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
}

dependencies {

    //noinspection GradleCompatible,GradleCompatible,GradleCompatible,GradleCompatible
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("androidx.activity:activity:1.9.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    //Rounded ImageView

    implementation("com.makeramen:roundedimageview:2.3.0")
    //Firebase

    implementation("com.google.firebase:firebase-messaging-ktx:23.2.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")

    //MultiDex
    implementation ("androidx.multidex:multidex:2.0.1")

    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("io.getstream:photoview:1.0.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0");
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0");








}