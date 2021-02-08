# Developer Notes:

This project is based upon a customized version of NeutrinosPlatform's scanLibrary at https://github.com/NeutrinosPlatform/scanlibrary .
### Setting up native libraries:

Uncompress the jniLibs.7z from IScan/app/src/main/ using 7-Zip to IScan/app/src/main/jniLibs. Your directory structure should look something like this:
~~~shell
main\
    +- java
    |
    +- res
    |
    +- jniLibs\
    |         +- arm64-v8a\
    |         |           +-libopencv_java3.so
    |         |           +-libScanner.so
    |         +- armeabi\
    |         |         +-libopencv_java3.so
    |         |         +-libScanner.so
    |         +- ....
    |
    +- AndroidManifest.xml
~~~

Currently the native code is the default as the one present in the master branch of the ScanLibrary but when the code will be edited for improvisation, it would be available on the GitHub repo.

If you need the documentation for the code of this project you can get it **[here](http://docs.nalinangrish.me/IScan)**
