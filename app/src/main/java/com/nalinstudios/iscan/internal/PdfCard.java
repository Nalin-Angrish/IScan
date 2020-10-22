package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.nalinstudios.iscan.R;

import java.io.File;

/**
 * A class to create CardView Layouts for each PDF thumbnail found in the internal data directory.
 * @author Nalin Angrish.
 */
public class PdfCard{
    private View content;
    private ImageView thumbnail;
    private TextView title;
    private ImageButton view, share, options;
    private Context context;
    private AppCompatActivity activity;
    private File file;

    /**
     * The constructor of the class to get all the meta-data of the PDF.
     * @param ctx The context of the application.
     * @param file The thumbnail file of hte PDF.
     * @param inflater The LayoutInflater of the activity.
     */
    public PdfCard(Context ctx, File file, LayoutInflater inflater, AppCompatActivity activity){
        context = ctx;
        this.activity = activity;
        content = inflater.inflate(R.layout.pdf_card, null);
        thumbnail = content.findViewById(R.id.pdfThumbnail);
        title = content.findViewById(R.id.pdfName);
        view = content.findViewById(R.id.view);
        share = content.findViewById(R.id.share);
        options = content.findViewById(R.id.options);

        this.file = file;
        init();
    }

    /**
     * A function to get the CardView of the PDF.
     * @return The cardView of the PDF with all the buttons.
     */
    public View getCard(){
        CardView card = new CardView(activity.getApplicationContext());
        card.addView(content);
        int color = Color.rgb(255,255,255);
        card.setCardBackgroundColor(color);
        card.setRadius(10);
        card.setUseCompatPadding(true);
        return card;
    }


    /**
     * The main initialization function of the PdfCard.
     */
    private void init(){
        int HEIGHT = getHeight();
        int WIDTH = getWidth();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 3;
        thumbnail.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(file.getAbsolutePath(),opts),WIDTH, HEIGHT, true));
        setTitle();
        LinearLayout l = content.findViewById(R.id.data);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPercent(context.getResources().getDisplayMetrics().widthPixels,50), ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = 20;
        params.bottomMargin = 20;
        l.setLayoutParams(params);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File viewAble = getFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(FileProvider.getUriForFile(activity.getApplicationContext(), activity.getPackageName()+ ".provider", viewAble), "application/pdf");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(intent, "Open Using"));
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File shareAble = getFile(file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity.getApplicationContext(), activity.getPackageName()+ ".provider", shareAble));
                intent.setType("application/pdf");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(intent, "Share Using"));
            }
        });
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(activity, options);
                menu.getMenuInflater().inflate(R.menu.card_menu, menu.getMenu());

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().toLowerCase().contains("delete")){
                            delete();
                        }else if (item.getTitle().toString().toLowerCase().contains("edit")){
                            edit();
                        }
                        return true;
                    }
                });

                menu.show(); //showing popup menu
            }
        });


    }

    /**
     * A function to get the original PDF file from the name of the thumbnail.
     * @param file The image file.
     * @return The file object of the PDF.
     */
    private File getFile(File file){
        File folder = new File(Environment.getExternalStorageDirectory(), "IScan");
        String pdfName;
        try {
            pdfName = file.getName().replace(".jpg", ".pdf");
        }catch (Exception e){
            pdfName = file.getName() + ".pdf";
        }
        return new File(folder, pdfName);
    }

    /**
     * A function to get the optimal width of the thumbnail.
     * @return The optimal width of the thumbnail.
     */
    private int getWidth(){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        return getPercent(screenWidth, 30);
    }

    /**
     * A function to get the optimal height of the thumbnail.
     * @return The optimal height of the thumbnail.
     */
    private int getHeight(){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        return getPercent(screenHeight, 20);
    }

    /**
     * A function to get the 'percent' percent of 'of'
     * @param of The percentage to get of.
     * @param percent The percentage to get.
     * @return The calculated percentage.
     */
    private int getPercent(int of, int percent){
        return (of * percent)/100;
    }



    /**
     * A function to set the title. This function is kept separate so that
     * if the name is too long, it can be shortened and for that manipulation
     * a separate block of code is made.
     */
    private void setTitle(){
        File file = this.file;
        String name;
        try {
            name = file.getName().replace(".jpg","");
        }catch (Exception e){
            name = file.getName();
        }
        if (name.length() > 15){
            StringBuilder sb = new StringBuilder();
            for (int i=0;i<15;i++){
                sb.append(name.charAt(i));
            }
            name = sb.toString() + "...";
        }
        title.setText(name);
    }


    /**
     * A function to delete the file when the user asks to delete it.
     * This also asks the user whether he/she just wants to delete it from the menu or completely delete it.
     */
    private void delete(){
        new AlertDialog.Builder(activity)
                .setMessage("Are you sure you want to delete this file? If you wish, you can just delete the file from the menu and keep the file saved or delete it permanently.")
                .setPositiveButton("Delete Permanently", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()){
                            File actual = getFile(file);
                            if (actual.delete()) {
                                Toast.makeText(activity, "File permanently deleted", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        Toast.makeText(activity, "File couldn't be deleted", Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("Delete from menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.delete()){
                            Toast.makeText(activity, "File deleted from menu", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(activity, "File couldn't be deleted", Toast.LENGTH_LONG).show();

                    }
                })
                .setNeutralButton("Don't delete", null)
                .show();
    }


    /**
     * A function to edit the file when the user wants to. (Currently not supported)
     */
    private void edit(){
        Toast.makeText(activity, "Editing functionality will be soon added", Toast.LENGTH_LONG).show();
    }
}
