package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.Statics;
import com.nalinstudios.iscan.scanlibrary.ResultFragment;
import com.nalinstudios.iscan.scanlibrary.ScanConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle all the editing functionality.
 * @author Nalin Angrish.
 */
@SuppressWarnings("ConstantConditions")
public class EditViewActivity extends AppCompatActivity implements View.OnClickListener {
    /** A FAB to handle submit events. */
    FloatingActionButton finishB;
    /** The directory containing all the images for this session */
    File dir;
    /** A list of all the ResultFragments created...*/
    List<ResultFragment> fragList = new ArrayList<>();

    /**
     * The oncreate function to load the opencv library and initialize the main function.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view);
        main();
    }


    /** The main function of this activity. */
    protected void main(){
        finishB = findViewById(R.id.finishB);
        finishB.setOnClickListener(this);
        String sessionDir = getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "hello");
        dir = new File(getFilesDir(), sessionDir);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        for (int i = 0; i < dir.listFiles().length; i++) {
            File imageFile = dir.listFiles()[i];

            Bundle args = new Bundle();
            args.putParcelable(ScanConstants.SCANNED_RESULT, Uri.fromFile(imageFile));
            args.putString(ScanConstants.SCAN_FILE, imageFile.getAbsolutePath());
            ResultFragment result = new ResultFragment();
            result.setArguments(args);
            transaction.add(R.id.viewList, result);
            fragList.add(result);
        }
        transaction.commit();
    }


    /**
     * This function will ask the user to enter the name of the PDF to be saved.
     */
    protected void Askname(){
        View p = getLayoutInflater().inflate(R.layout.popup_enter_name, null);

        final PopupWindow window = new PopupWindow(p, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(p, Gravity.CENTER, 0, 0);
        window.getContentView().findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                EditText tBox = window.getContentView().findViewById(R.id.pdfName);
                window.dismiss();
                try {
                    for (ResultFragment frag : fragList){
                        frag.finish();
                    }
                    Statics.createPdf(getApplication(), tBox.getText().toString());
                    Intent intent = new Intent(EditViewActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Couldn't create PDF, Please try again", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        window.getContentView().findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
    }


    /**
     * This function will handle all the button pressed and redirect to the one we need to.
     * @param v the view object of the button.
     */
    @Override
    public void onClick(View v){/*
        if (v.equals(delB)){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Delete?")
                    .setMessage("Are you sure you want to delete this page? This operation cannot be undone.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCurrentPic();
                        }
                    })
                    .setNegativeButton("Don't Delete", null);//.show();
        }else */if (v.equals(finishB)){
            Askname();
        }
    }

}
