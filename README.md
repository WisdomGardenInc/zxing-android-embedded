# WisdomGarden ZXing Android Embedded
## The repository has been migrated to [Gitlab](https://gitlab.tronclass.com.cn/lms/zxing-android-embedded) on 2023-8-17

Fork from [zxing-android-embedded][2] of V4.3.0

By default, Android SDK 24+ is required because of `zxing:core` 3.4.x.
SDK 19+ is supported with additional configuration, see [Older SDK versions](#older-sdk-versions).

## Adding aar dependency with Gradle

Add the following to your `build.gradle` file:

```groovy
// Config for SDK 24+

repositories {
    maven {
        url "http://nexus.tronclass.com.cn:8081/repository/wg"
        credentials {
            username NEXUS_NAME
            password NEXUS_PASSWORD
        }
    }
}

dependencies {
    implementation 'com.wisdomgarden:zxing-android-embedded:1.0.0'
}
```

## Older SDK versions

By default, only SDK 24+ will work, even though the library specifies 19 as the minimum version.

For SDK versions 19+, one of the changes below are required.
Some older SDK versions below 19 may work, but this is not tested or supported.

### Option 1. Downgrade zxing:core to 3.3.0

```groovy
repositories {
    maven {
        url "http://nexus.tronclass.com.cn:8081/repository/wg"
        credentials {
            username NEXUS_NAME
            password NEXUS_PASSWORD
        }
    }
}

dependencies {
    implementation('com.wisdomgarden:zxing-android-embedded:1.0.0') { transitive = false }
    implementation 'com.google.zxing:core:3.3.0'
}
```

### Option 2: Desugaring (Advanced)

This option does not require changing library versions, but may complicate the build process.

This requires Android Gradle Plugin version 4.0.0 or later.

See [Java 8+ API desugaring support](https://developer.android.com/studio/write/java8-support#library-desugaring).

Example for SDK 21+:

```groovy
android {
    defaultConfig {
        minSdkVersion 21
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        coreLibraryDesugaringEnabled true
        // Sets Java compatibility to Java 8
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.wisdomgarden:zxing-android-embedded:1.0.0'

    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.5'
}
```

SDK 19+ additionally requires multiDex. In addition to these gradle config changes, the Application
class must also be changed. See for details: [Configure your app for multidex](https://developer.android.com/studio/build/multidex#mdex-gradle).

```groovy
android {
    defaultConfig {
        multiDexEnabled true
        minSdkVersion 19
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        coreLibraryDesugaringEnabled true
        // Sets Java compatibility to Java 8
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'com.wisdomgarden:zxing-android-embedded:1.0.0'

    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.5'
    implementation "androidx.multidex:multidex:2.0.1"
}
```

## Hardware Acceleration

Hardware acceleration is required since TextureView is used.

Make sure it is enabled in your manifest file:

```xml
    <application android:hardwareAccelerated="true" ... >
```

## Usage with ScanContract

Note: `startActivityForResult` is deprecated, so this example uses `registerForActivityResult` instead.
See for details: https://developer.android.com/training/basics/intents/result

`startActivityForResult` can still be used via `IntentIntegrator`, but that is not recommended anymore.

```java
// Register the launcher and result handler
private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
        result -> {
            if(result.getContents() == null) {
                Toast.makeText(MyActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MyActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        });

// Launch
public void onButtonClick(View view) {
    barcodeLauncher.launch(new ScanOptions());
}
```

Customize options:
```java
ScanOptions options = new ScanOptions();
options.setDesiredBarcodeFormats(ScanOptions.ONE_D_CODE_TYPES);
options.setPrompt("Scan a barcode");
options.setCameraId(0);  // Use a specific camera of the device
options.setBeepEnabled(false);
options.setBarcodeImageEnabled(true);
barcodeLauncher.launch(options);
```

See [BarcodeOptions][5] for more options.

### Generate Barcode example

While this is not the primary purpose of this library, it does include basic support for
generating some barcode types:

```java
try {
  BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
  Bitmap bitmap = barcodeEncoder.encodeBitmap("content", BarcodeFormat.QR_CODE, 400, 400);
  ImageView imageViewQrCode = (ImageView) findViewById(R.id.qrCode);
  imageViewQrCode.setImageBitmap(bitmap);
} catch(Exception e) {

}
```

To customize the generated barcode image, use the `setBackgroundColor` and `setForegroundColor` functions of the
`BarcodeEncoder` class with a [`@ColorInt`](https://developer.android.com/reference/androidx/annotation/ColorInt)
value to update the background and foreground colors of the barcode respectively. By default, the barcode has a
white background and black foreground.


### Changing the orientation

To change the orientation, specify the orientation in your `AndroidManifest.xml` and let the `ManifestMerger` to update the Activity's definition.

Sample:

```xml
<activity
		android:name="com.wisdomgarden.barcodescanner.CaptureActivity"
		android:screenOrientation="fullSensor"
		tools:replace="screenOrientation" />
```

```java
ScanOptions options = new ScanOptions();
options.setOrientationLocked(false);
barcodeLauncher.launch(options);
```

### Customization and advanced options

See [EMBEDDING](EMBEDDING.md).

For more advanced options, look at the [Sample Application](https://github.com/wisdomgarden/zxing-android-embedded/blob/master/sample/src/main/java/example/zxing/MainActivity.java),
and browse the source code of the library.

This is considered advanced usage, and is not well-documented or supported.

## Android Permissions

The camera permission is required for barcode scanning to function. It is automatically included as
part of the library. On Android 6 it is requested at runtime when the barcode scanner is first opened.

When using BarcodeView directly (instead of via IntentIntegrator / CaptureActivity), you have to
request the permission manually before calling `BarcodeView#resume()`, otherwise the camera will
fail to open.

## Building locally

    ./gradlew assemble

To deploy the artifacts the your local Maven repository:

    ./gradlew publishToMavenLocal

You can then use your local version by specifying in your `build.gradle` file:

    repositories {
        mavenLocal()
    }

## Sponsored by

[wisdomgarden][1]


## License

Licensed under the [Apache License 2.0][3]

	Copyright (C) 2012-2022 ZXing authors, Journey Mobile

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.



[1]: https://www.wisdomgarden.com/
[2]: https://github.com/journeyapps/zxing-android-embedded
[3]: http://www.apache.org/licenses/LICENSE-2.0
