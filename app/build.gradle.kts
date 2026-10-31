plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseVersionCode = (findProperty("releaseVersionCode") as String?)?.toInt() ?: 1
val releaseVersionName = findProperty("releaseVersionName") as String? ?: "0.0.0"

val envKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")
val envKeystorePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val envKeyAlias = System.getenv("ANDROID_KEY_ALIAS")
val envKeyPassword = System.getenv("ANDROID_KEY_PASSWORD")
val signingValues = listOf(envKeystorePath, envKeystorePassword, envKeyAlias, envKeyPassword)

android {
    namespace = "by.bnd.alconavt"
    compileSdk = 35

    defaultConfig {
        applicationId = "by.bnd.alconavt"
        minSdk = 26
        targetSdk = 35
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    when {
        signingValues.all { it.isNullOrBlank() } -> Unit
        signingValues.any { it.isNullOrBlank() } ->
            throw org.gradle.api.GradleException(
                "Для подписи release нужны ANDROID_KEYSTORE_PATH, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS и ANDROID_KEY_PASSWORD.",
            )
        else -> {
            val keystore = file(envKeystorePath!!)
            if (!keystore.isFile) {
                throw org.gradle.api.GradleException("Файл ключа не найден: $envKeystorePath")
            }
            signingConfigs {
                create("release") {
                    storeFile = keystore
                    storePassword = envKeystorePassword!!
                    keyAlias = envKeyAlias!!
                    keyPassword = envKeyPassword!!
                }
            }
            buildTypes {
                getByName("release") {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
