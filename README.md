
# MediaPicker

This is a simple image picker library that helps one to pick/capture multiple images from the gallery or the camera.

##  Overview
This library mainly serves as a tool for implementing a customized image picker for Android. 
It uses the Android's MediaStore API to fetch all the images from the device and Camera 2 API for implementing a custom in app camera.
It also handles the necessary permissions. 

## Setup
To use this library, your project's minimum sdk version must be >= 21 and target/compile sdk version must be >= 33.  

```bash
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.ShaQib07:MediaPicker:latest_release'
}
```
## Usage
Create a MediaPicker instance.
```bash
 val mediaPicker = MediaPicker(this) // where this is an Activity instance
```
Call the `pickMedia()` function to start picking images.
```bash
 mediaPicker.pickMedia(picker = Picker.CHOOSER, fileType = Type.MEDIA, maxSelection = 3)
```
NOTE: Here, you can pass one of the three enums - `Picker.CHOOSER`, `Picker.CAMERA` or `Picker.GALLERY` as `picker`.
`Picker.CHOOSER` will open a dialog to choose from CAMERA or GALLERY.
Whereas, `Picker.CAMERA` or `Picker.GALLERY` will directly open their corresponding window.
As for `fileType`, pass `Type.IMAGE` or `Type.VIDEO` for picking image or video files respectively.
Pass `Type.MEDIA` if you want to pick both image and video files.
The  `maxSelection` parameter is optional and it's default value is 3. 
It is an integer value that'll define the possible maximum number of selection.

Use the `pickedMedia` StateFlow to observe the picked media files from CAMERA or GALLERY.
Add necessary dependencies to use StateFlow in your app.
```bash
 lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) { 
            mediaPicker.pickedMedia.collectLatest { list ->
                // use the list of media as you please
            }
        }
    }
```
You may need to import the below dependency in order to use the `repeatOnLifecycle(Lifecycle.State.STARTED)` part.
```bash
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:latest_release"
```
If you'd like to upload the Media files through REST API and want them as MultipartBody.Part then you can the extension function `asMultipart()` like below.
```bash
lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaPicker.pickedMedia.collectLatest { list ->
                    val multipartList = list.asMultipart()
                }
            }
        }
```

This is how the Image class looks like.
```bash
 data class Media(
    val uri: Uri, 
    val title: String, 
    val path: String,
    val dateTaken: Date = Date(System.currentTimeMillis())
)
```
## Tech Stack

**Language:** Kotlin

**Architecture:** MVVM with Clean Architecture

**Concurrency:** Coroutine + Flow

**Custom Gallery:** MediaStore API

**Custom Camera:** Camera 2 API


## Screenshots

![App Screenshot](https://raw.githubusercontent.com/ShaQib07/MediaPicker/master/chooser_dialog.png)

![App Screenshot](https://raw.githubusercontent.com/ShaQib07/MediaPicker/master/gallery_permission.png)

![App Screenshot](https://raw.githubusercontent.com/ShaQib07/MediaPicker/master/gallery.png)

![App Screenshot](https://raw.githubusercontent.com/ShaQib07/MediaPicker/master/camera_permission.png)

## License
```bash
Copyright (C) 2022 Golam Shakib Khan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
