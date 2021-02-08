package com.nalinstudios.iscan.edit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;
import com.nalinstudios.iscan.MainActivity;
import com.nalinstudios.iscan.R;
import com.nalinstudios.iscan.internal.Statics;
import com.nalinstudios.iscan.scanlibrary.Loader;
import com.nalinstudios.iscan.scanlibrary.ProgressDialogFragment;
import com.nalinstudios.iscan.scanlibrary.ScanConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle all the editing functionality after the PDF has been created.
 * @author Nalin Angrish.
 */
@SuppressWarnings("ConstantConditions")
public class PDFEditActivity extends FragmentActivity implements View.OnClickListener {
    /** A FAB to handle submit events. */
    FloatingActionButton finishB;
    /** The directory containing all the images for this session */
    File dir;
    /** A list of all the ResultFragments created...*/
    List<ResultFragment> fragList = new ArrayList<>();
    /** A fragment to show to the user while the pdf is decoded*/
    static ProgressDialogFragment progressDialogFragment;
    /** A reference to current ScanFragment*/
    ScanFragment currentFragment;
    /** A reference to the current ResultFragment*/
    ResultFragment currentResult;
    /** The popup for entering name*/
    PopupWindow window;



    /**
     * The oncreate function to load the opencv library and initialize the main function.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_edit);
        showProgressDialog("Decoding PDF");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                load();
            }
        });

    }


    /**
     * A function to create a session, load the PDF and decode it.
     */
    public void load() {
        Uri pdf = getIntent().getData();
        File file = new File(pdf.getPath());
        String s = Statics.randString();
        getApplicationContext().getSharedPreferences("IScan", MODE_PRIVATE).edit().putString("sessionName", s).apply();
        new File(getFilesDir(), s).mkdir();

        try{
            PdfReader reader = new PdfReader(file.getAbsolutePath());

            for (int i = 0; i < reader.getXrefSize(); i++) {
                PdfObject pdfobj= reader.getPdfObject(i);
                if (pdfobj == null || !pdfobj.isStream()) {
                    continue;
                }

                PdfStream stream = (PdfStream) pdfobj;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);

                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    byte[] img = PdfReader.getStreamBytesRaw((PRStream) stream);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    out.write(img);
                    File f = new File(new File(getFilesDir(), s), Statics.randString()+".jpg");
                    f.createNewFile();
                    out.writeTo(new FileOutputStream(f));
                }
            }
        } catch (Exception e) {e.printStackTrace(); }

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


                    LinearLayout l = new LinearLayout(PDFEditActivity.this);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        });
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
                                Toast.makeText(getApplicationContext(), "A PDF with this name already exists. Please try again with a different name.", Toast.LENGTH_LONG).show();
                                shouldClose = false;
                            }
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Couldn't create PDF, Please try again", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            shouldClose = false;
                        }
                        dismissDialog();
                        if (shouldClose){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(PDFEditActivity.this, MainActivity.class);
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


    /**
     * A function to get notified when the corners are selected and the final image after cropping is available
     */
    public void onScanFinish(){
        getFragmentManager().beginTransaction().remove(currentFragment).commit();
        finishB.setVisibility(View.VISIBLE);
        currentResult.getBitmap();
        currentResult = null;
        currentFragment = null;
    }

    /**
     * A function to create the window to crop the image
     * @param rfrag the resultFragment showing the image
     */
    public void startScan(ResultFragment rfrag){
        ScanFragment frag = new ScanFragment();
        Bundle b = new Bundle();
        b.putParcelable(ScanConstants.SELECTED_BITMAP, rfrag.getUri());
        b.putString(ScanConstants.SCAN_FILE, rfrag.getUri().getPath());
        frag.setArguments(b);
        getFragmentManager().beginTransaction().add(R.id.pe_act_main, frag).commit();
        finishB.setVisibility(View.GONE);
        currentFragment = frag;
        currentResult = rfrag;
    }

    static {
        Loader.load();
    }
}
