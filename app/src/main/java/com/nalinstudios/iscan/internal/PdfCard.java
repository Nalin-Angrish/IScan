package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.nalinstudios.iscan.R;

import java.io.File;

public class PdfCard{
    private View content;
    private ImageView thumbnail;
    private TextView title;
    private ImageButton view, share;
    private Context context;

    public PdfCard(Context con, File file, LayoutInflater inflater){
        context = con;
        content = inflater.inflate(R.layout.pdf_card, null);
        thumbnail = content.findViewById(R.id.pdfThumbnail);
        title = content.findViewById(R.id.pdfName);
        view = content.findViewById(R.id.view);
        share = content.findViewById(R.id.share);
        init(file);
    }

    public View getCard(){
        CardView card = new CardView(context);
        card.addView(content);
        int color = Color.rgb(255,255,255);
        card.setCardBackgroundColor(color);
        card.setRadius(10);
        return card;
    }

    private void init(final File file){
        int HEIGHT = getHeight();
        int WIDTH = getWidth();
        thumbnail.getLayoutParams().height = HEIGHT;
        thumbnail.getLayoutParams().width = WIDTH;
        thumbnail.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
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
                intent.setDataAndType(Uri.fromFile(viewAble), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File shareAble = getFile(file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareAble));
                intent.setType("application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });


    }

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

    private int getWidth(){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int width = getPercent(screenWidth, 30);
        Log.println(Log.ASSERT, "width", screenWidth+"-"+width);
        return width;
    }
    private int getHeight(){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;
        int height = getPercent(screenHeight, 20);
        Log.println(Log.ASSERT, "height", screenHeight+"-"+height);
        return height;
    }

    private int getPercent(int of, int percent){
        Log.println(Log.ASSERT, "", ""+of);
        Log.println(Log.ASSERT, "", ""+percent);
        return (of * percent)/100;
    }
}
