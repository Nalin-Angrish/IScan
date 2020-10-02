package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.Statics;
import com.nalinstudios.iscan.internal.ZoomHandler;
import com.nalinstudios.iscan.scanlibrary.Loader;
import com.nalinstudios.iscan.scanlibrary.ScanConstants;
import com.nalinstudios.iscan.scanlibrary.ScanFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * This activity is the window where the user will scan the documents.
 * @author Nalin Angrish.
 */
public class ScannerActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    /** The camera variable which will be used to capture pictures*/
    Camera camera;
    /** The FABs that will be used to handle 'picture clicks', 'submissions', and the creation of 'settings popups'*/
    FloatingActionButton clickB, nextB, settingsB;
    /** The texture to render the frames captured by the camera*/
    TextureView texture;
    /** The TextView to show the count of the pictures captured*/
    TextView countView;
    /** A variable to manage the counts*/
    int count = 0;
    /** A variable to remember that whether the flash is on or not*/
    boolean flash = true;
    /** A handler for zooming actions */
    ZoomHandler zoomHandler;
    /** A list of Uris of the pictures taken*/
    ArrayList<Uri> images = new ArrayList<>();
    /** A reference to the current fragment initialized */
    Fragment currentFragment;


    /**
     * The main method which will show our textures and will administer the window.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        texture = findViewById(R.id.camView);
        texture.setSurfaceTextureListener(this);
        int height = Resources.getSystem().getDisplayMetrics().heightPixels - 350;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        texture.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));

        clickB = findViewById(R.id.ClickB);
        clickB.setOnClickListener(this);
        nextB = findViewById(R.id.NextB);
        nextB.setOnClickListener(this);
        settingsB = findViewById(R.id.settings);
        settingsB.setOnClickListener(this);
        countView = findViewById(R.id.countView);
    }


    /**
     * The function to handle the texture rendering and to enable the camera.
     * @param surface The texture to render the camera on.
     * @param width The width of the texture.
     * @param height The height of the texture.
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            camera = Camera.open();
            camera.setPreviewTexture(surface);
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            if (flash){
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
            camera.setParameters(params);
            zoomHandler = new ZoomHandler(camera, findViewById(R.id.camView).getContext());
            texture.setOnTouchListener(zoomHandler);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("Opening Camera", e.toString());
        }
    }

    /**
     * The function to handle the clicks of all the buttons.
     * @param view the view object of the pressed button.
     */
    @Override
    public void onClick(View view){
        if (view.equals(clickB)) {
            click();
            if (nextB.getVisibility() == View.INVISIBLE) {
                nextB.show();
            }
        }else if (view.equals(nextB)){
            submit();
        }else if (view.equals(settingsB)){
            createSettingsPopup();
        }
    }

    /**
     * A function to handle the click of a picture.
     */
    protected void click(){
        String dirname = getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "");
        final File dir = new File(getFilesDir(), dirname);
        if (!dir.exists()){
            Log.println(Log.VERBOSE,"DIR", String.valueOf(dir.mkdir()));
        }
        final File[] files = dir.listFiles();
        assert files != null;

        try {
            countView.setText(String.valueOf(files.length + 1));
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                @SuppressWarnings("all")
                public void onShutter() {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(100);
                }
            }, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera cam) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    bitmap = rotateImage(bitmap);
                    File mFile = new File(dir, Statics.randString() + ".jpg");
                    try {
                        if (!mFile.exists()) {
                            Log.println(Log.VERBOSE, "IMG", String.valueOf(mFile.createNewFile()));
                        }
                        OutputStream imgStream = new FileOutputStream(mFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgStream);
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            Log.e("Couldn't wait", e.toString());
                        }
                        count = count + 1;
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Couldn't Click picture. Please Try again...", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    startScan(mFile);
                    camera.startPreview();
                }
            });
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * A function to create the popup for changing the settings.
     * (Note that only flash is supported as yet.)
     */
    protected void createSettingsPopup(){
        View popup = getLayoutInflater().inflate(R.layout.popup_window, null);
        final PopupWindow window = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(popup, Gravity.CENTER, 0, 0);
        Switch flashB = window.getContentView().findViewById(R.id.flashb_popup);
        flashB.setChecked(flash);
        flashB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flash = !flash;
                if (flash){
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    camera.setParameters(parameters);
                }else {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                }
            }
        });

        window.getContentView().findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
    }


    /**
     * A function to handle the click of the submit button.
     * This starts the EditViewActivity which can be used to make changes to the pictures.
     */
    protected void submit(){
        Intent intent = new Intent(ScannerActivity.this, EditViewActivity.class);
        Bundle b = new Bundle();
        b.putParcelableArrayList("content", images);
        intent.putExtras(b);
        startActivity(intent);
        Log.d("Submitted", getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "Null"));
    }

    /**
     * Blank function.
     * @param surface The texture to render the camera on.
     * @param width The width of the texture.
     * @param height The height of the texture.
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}


    /**
     * Blank function
     * @param surface The texture to render the camera on.
     * @return true
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    /**
     * Blank function.
     * @param surface texture that renders the camera.
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    /**
     * A function to handle back key presses.
     * It shows an alert dialog to the user to warn him that the pictures clicked till now will be deleted.
     */
    @Override
    public void onBackPressed() {
        if (currentFragment != null){
            ((ScanFragment)currentFragment).scanButton.performClick();
            Toast.makeText(this, "The Image has been cropped and saved but if needed, you can delete it later.", Toast.LENGTH_LONG).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this window? This will discard all pictures")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Statics.clearData(getApplication());
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }


    /**
     * A function to get notified when the corners are selected and the final image after cropping is available
     */
    public void onScanFinish(){
        getFragmentManager().beginTransaction().remove(currentFragment).commit();
        currentFragment = null;
    }

    /**
     * A function to create the window to crop the image
     * @param m the image file
     */
    public void startScan(File m){
        ScanFragment frag = new ScanFragment();
        Bundle b = new Bundle();
        b.putParcelable(ScanConstants.SELECTED_BITMAP, Uri.fromFile(m));
        b.putString(ScanConstants.SCAN_FILE, m.getAbsolutePath());
        frag.setArguments(b);
        getFragmentManager().beginTransaction().add(R.id.__main__, frag).commit();
        currentFragment = frag;
    }


    /**
     * A function to release the camera when activity is finished
     */
    @Override
    public void finish() {
        super.finish();
        camera.stopPreview();
        camera.release();
    }


    static {
        Loader.load();
    }

    /**
     * A function to rotate the image according to the camera orientation
     * @param bmp the bitmap to be rotated
     * @return the rotated bitmap
     */
    protected Bitmap rotateImage(Bitmap bmp){
        int deg = getRotation();
        if (deg == 0){
            //As the image does not need to be rotated.
            return bmp;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(deg);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), true);
        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
    }


    /**
     * A function to get the camera's rotation based on the manufacturer as different manufacturers assemble cameras with different orientation
     * @return the degrees to rotate the image
     */
    protected int getRotation(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        return info.orientation;
    }
}