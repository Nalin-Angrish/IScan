# IScan
The Indian Scanner App - Made for the Indians, By the Indians
Official website: [https://nalinstudios.herokuapp.com/IScan](https://nalinstudios.herokuapp.com/IScan)  

# Developer Zone:
NOTE: This project has migrated from OpenCV to a customized version of NeutrinosPlatform's scanLibrary at https://github.com/NeutrinosPlatform/scanlibrary , so if you have been participating, you need to switch too.
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

### Known features that are not present but will be added/fixed:
1) The PDF is not be created if another PDF with the same name exists.
2) Scrolling through multiple preview fragments.
3) The preview at the home page is not sorted correctly (newer should be at top but are not)
4) It shows a blank but ugly screen when there are no PDFs created

### Known bugs:
1) The PolygonView is not positioned correctly in the FrameLayout.
2) The image in the PDF card will stretch to maintain resolution but not to preserve ImageView standard ratio.
3) The app crashes when there are a lot of PDF cards created on the home page.

If you need the documentation for the code of this project you can get it **[here](https://nalin-2005.github.io/IScan)**
