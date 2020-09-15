# IScan
A scanner App - Made for the Indians, By the Indians  
Official website: [https://nalinstudios.herokuapp.com/IScan](https://nalinstudios.herokuapp.com/IScan)  

# Developer Notice:
NOTE: This project has migrated from OpenCV to scanLibrary, so if you have been participating, you need to switch to scanLibrary too.

### Integrating scanLibrary
1. Open up your project's root folder in the Terminal / Command Prompt and type:
    ```shell
        git clone https://github.com/NeutrinosPlatform/scanlibrary.git
    ```
2. Check your settings.gradle file and it should contain a line like this:
    ```
        include ':scanlibrary:library'
    ```
3. Check your App level build.gradle file and it should contain the following line in the dependencies block:
    ```
        implementation project(path: ':scanlibrary:library')
    ```
4. Change the content of the build.gradle file inside scanlibrary/library folder and set it's content to :

    ```
    apply plugin: 'com.android.library'
    apply plugin: 'com.github.dcendents.android-maven'

    android {
        compileSdkVersion 29
        buildToolsVersion '29.0.2'

        packagingOptions {
            doNotStrip '*/mips/*.so'
            doNotStrip '*/mips64/*.so'
        }

        defaultConfig {
            minSdkVersion 19
            targetSdkVersion 29
            versionCode 1
            versionName "1.0"
            ndk {
                moduleName "Scanner"
            }
            sourceSets.main {
                jni.srcDirs = []
                jniLibs.srcDirs 'src/main/libs'
            }
        }

        buildTypes {
            release {
                minifyEnabled false
                proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            }
        }

    }

    dependencies {
        implementation fileTree(dir: 'libs', include: ['*.jar'])
        implementation 'androidx.legacy:legacy-support-v4:1.0.0'
    }
    ```

5. Open the AndroidManifest.xml file in the scanlibrary/library/src/main folder and remove the line :
    ```
    android:allowBackup="true"
    ```

If you need the documentation for the code of this project you can get it **[here](https://nalin-2005.github.io/IScan)**
