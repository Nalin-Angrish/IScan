package com.nalinstudios.iscan;

import androidx.fragment.app.FragmentActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.Statics;
import com.nalinstudios.iscan.scanlibrary.ProgressDialogFragment;
import com.nalinstudios.iscan.scanlibrary.ResultFragment;
import com.nalinstudios.iscan.scanlibrary.ScanConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle all the editing functionality.
 * @author Nalin Angrish.
 */
public class EditViewActivity extends FragmentActivity implements View.OnClickListener {
    /** A FAB to handle submit events. */
    FloatingActionButton finishB;
    /** The directory containing all the images for this session */
    File dir;
    /** A list of all the ResultFragments created...*/
    List<ResultFragment> fragList = new ArrayList<>();
    /** A progressbar to inform the user that the PDF is being created */
    ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment("Creating PDF..");
    /** A popup for letting the user enter a name for the PDF.*/
    PopupWindow window;

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

        final FragmentManager manager = getFragmentManager();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < dir.listFiles().length; i++) {
                    FragmentTransaction transaction = manager.beginTransaction();

                    File imageFile = dir.listFiles()[i];
                    Bundle args = new Bundle();
                    args.putParcelable(ScanConstants.SCANNED_RESULT, Uri.fromFile(imageFile));
                    args.putString(ScanConstants.SCAN_FILE, imageFile.getAbsolutePath());
                    ResultFragment result = new ResultFragment();
                    result.setArguments(args);


                    LinearLayout l = new LinearLayout(EditViewActivity.this);
                    l.setId(View.generateViewId());
                    transaction.add(l.getId(), result, "result-"+i);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, LinearLayout.LayoutParams.MATCH_PARENT);
                    l.setLayoutParams(param);
                    ((LinearLayout)findViewById(R.id.viewList)).addView(l,i);
                    findViewById(R.id.viewList).invalidate();
                    fragList.add(result);
                    transaction.commit();

                }
            }
        }).start();
    }


    /**
     * This function will ask the user to enter the name of the PDF to be saved.
     */
    protected void Askname(){
        View p = getLayoutInflater().inflate(R.layout.popup_enter_name, null);

        window = new PopupWindow();
        window.setContentView(p);
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(p, Gravity.CENTER, 0, 0);
        window.setFocusable(true);
        window.update();
        Statics.dimBackground(window);
        window.getContentView().findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                showProgressDialog("Converting to PDF...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean shouldClose = true;
                        try {
                            EditText tBox = window.getContentView().findViewById(R.id.pdfName);
                            if (Statics.isAvailable(tBox.getText().toString())) {
                                for (ResultFragment frag : fragList) {
                                    if (!frag.deleted){
                                        frag.finish();
                                    }
                                }
                                Statics.createPdf(getApplication(), tBox.getText().toString());
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "A PDF with this name already exists. Please try again with a different name.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                shouldClose = false;
                            }
                        }catch (Exception e){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Couldn't create PDF, Please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                            shouldClose = false;
                        }
                        dismissDialog();
                        if (shouldClose){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(EditViewActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        window.getContentView().findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });

        ((EditText)window.getContentView().findViewById(R.id.pdfName)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    window.getContentView().findViewById(R.id.end).performClick();
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * This function will handle all the button pressed and redirect to the one we need to.
     * @param v the view object of the button.
     */
    @Override
    public void onClick(View v){
        if (v.equals(finishB)){
            Askname();
        }
    }


    /**
     * A function to clear this activity and free up memory.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    /**
     * A function to remove the fragment when it's image is deleted.
     * @param fragment the fragment to be removed
     */
    public void delete(ResultFragment fragment){
        int index = fragList.indexOf(fragment);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        ((LinearLayout)findViewById(R.id.viewList)).removeViewAt(index);
        transaction.remove(fragment);
        fragList.remove(fragment);
        transaction.commit();
        if (fragList.size()==0){ //all images have been deleted
            this.onBackPressed();
        }
    }


    /**
     * A function to show the progress dialog.
     * @param message the message to show.
     */
    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }



    /**
     * A function to dismiss the progress dialog.
     */
    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }
}