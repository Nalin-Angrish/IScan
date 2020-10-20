# IScan
The Indian Scanner App - Made for the Indians, By the Indians<br>
Official website: [https://nalinstudios.herokuapp.com/IScan](https://nalinstudios.herokuapp.com/IScan) <br><br>


IScan - The Indian Scanner App, Made for the Indians, By the Indians.<br>
Scan High-Quality PDFs quickly and efficiently on the go using IScan, a Lightweight, Fast, and Efficient Document
Scanner. This app turns your android device into a fast, lightweight and portable document scanner. This
application allows you to easily scan documents and share them using your preferred mode of file sharing. The true
power of this application is that it is open source and would readily accept any Modifications, Upgrades,Feature
Requests and Bug Fixes.<br><br><br>

For more Information, Visit: [https://nalinstudios.herokuapp.com/IScan](https://nalinstudios.herokuapp.com/IScan)

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

If you need the documentation for the code of this project you can get it **[here](https://nalin-2005.github.io/IScan)**
