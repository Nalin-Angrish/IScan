package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
    private ImageButton view, share;
    private Context context;
    private AppCompatActivity activity;

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
        init(file);
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
     * @param file The image file of the thumbnail.
     */
    private void init(final File file){
        int HEIGHT = getHeight();
        int WIDTH = getWidth();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        thumbnail.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeFile(file.getAbsolutePath(),options),WIDTH, HEIGHT, true));
        try {
            title.setText(file.getName().replace(".jpg", ""));
        }catch (Exception e){
            title.setText(file.getName());
        }
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
}
