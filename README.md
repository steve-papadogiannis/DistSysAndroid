# Directions Map Reduce Client # 

[![Build Status](https://travis-ci.com/steve-papadogiannis/dist-sys-client-android.svg?branch=master)](https://travis-ci.com/steve-papadogiannis/dist-sys-client-android)
[![Known Vulnerabilities](https://snyk.io/test/github/steve-papadogiannis/dist-sys-client-android/badge.svg?targetFile=app/build.gradle)](https://snyk.io/test/github/steve-papadogiannis/dist-sys-client-android?targetFile=app/build.gradle)

A small project that where the user selects a starting and an ending location in a Google Maps Fragment
and queries an **Directions Map Reduce Server** at the configured ip/port.

## Versions ##

* Compile Android SDK: 30
* Android Build Tools: 30.0.3
* Play Services Maps SDK: 17.0.0
* Play Services Location SDK: 18.0.0

## Sequence Diagram ##

![Lifecycle Sequence Diagram](./images/lifecycle.svg)

## Build ##

Below command should be issued inside project's directory:

```
./gradlew build connectedCheck
```

## Run ##

Below command should be issued inside project's directory.
`adb` should be in the user's path and an `Android`
device of the targeted `sdk` version should be connected:

For debug apk:

```
./adb install app/build/outputs/apk/debug/app-debug.apk
```

For release apk:

```
./adb install app/build/outputs/apk/release/app-release.apk
```