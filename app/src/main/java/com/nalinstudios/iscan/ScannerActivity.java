package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class ScannerActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    Camera camera;
    FloatingActionButton clickB, nextB, settingsB;
    TextureView texture;
    TextView countView;
    int count = 0;
    boolean flash = true;

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


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            camera = Camera.open();
            camera.setPreviewTexture(surface);
            camera.setDisplayOrientation(90);
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            params.setRotation(90);
            if (flash){
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            }
            camera.setParameters(params);
            camera.startPreview();
        } catch (Exception e) {
            Log.e("Opening Camera", e.toString());
        }
    }

    @Override
    public void onClick(View view){
        if (view.equals(clickB)) {
            if (nextB.getVisibility() == View.INVISIBLE) {
                nextB.show();
            }
            click();
        }else if (view.equals(nextB)){
            submit();
        }else if (view.equals(settingsB)){
            createSettingsPopup();
        }


    }

    protected void click(){
        String dirname = getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "");
        final File dir = new File(getFilesDir(), dirname);
        if (!dir.exists()){
            Log.println(Log.VERBOSE,"DIR", String.valueOf(dir.mkdir()));
        }
        final File[] files = dir.listFiles();
        assert files != null;

        countView.setText(String.valueOf(files.length+1));
        camera.takePicture(new Camera.ShutterCallback() {
            @Override @SuppressWarnings("all")
            public void onShutter() {
                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
            }
        },null,  new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera cam){
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                File mFile = new File(dir, "img"+count+".jpg");
                try {
                    if (!mFile.exists()) {
                        Log.println(Log.VERBOSE, "IMG", String.valueOf(mFile.createNewFile()));
                    }
                    OutputStream imgStream = new FileOutputStream(mFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgStream);
                    try {Thread.sleep(700);}catch (InterruptedException e){Log.e("Couldn't wait", e.toString());}
                    count = count+1;
                }catch (IOException e){
                    Toast.makeText(getApplicationContext(), "Couldn't Click picture. Please Try again...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                camera.startPreview();
            }
        });
    }

    protected void createSettingsPopup(){
        View view = getLayoutInflater().inflate(R.layout.popup_window, null);
        final PopupWindow window = new PopupWindow(view, 400, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
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


    protected void submit(){
        Intent intent = new Intent(ScannerActivity.this, EditViewActivity.class);
        startActivity(intent);
        Log.d("Submitted", getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "Null"));
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        camera.stopPreview();
        camera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onBackPressed() {
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

}