// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register<Copy>("copyApk") {
  from(file("app/build/outputs/apk/debug/app-debug.apk"))
  into(file(".build-outputs"))
}

tasks.register<Copy>("copyApkToDownload") {
  from(file("app/build/outputs/apk/debug/app-debug.apk"))
  into(file("APK_DOWNLOAD"))
}
