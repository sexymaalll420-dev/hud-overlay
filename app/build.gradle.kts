          plugins {
              id("com.android.application")
              id("org.jetbrains.kotlin.android")
          }

          android {
              namespace = "com.venice.hud"
              compileSdk = 34

              defaultConfig {
                  applicationId = "com.venice.hud"
                  minSdk = 24
                  targetSdk = 34
                  versionCode = 4
                  versionName = "4.0"
              }

              buildFeatures { aidl = true }

              compileOptions {
                  sourceCompatibility = JavaVersion.VERSION_17
                  targetCompatibility = JavaVersion.VERSION_17
              }
              kotlinOptions { jvmTarget = "17" }
              buildTypes { release { isMinifyEnabled = false } }
          }

          dependencies {
              implementation("dev.rikka.shizuku:api:13.1.5")
              implementation("dev.rikka.shizuku:provider:13.1.5")
          }