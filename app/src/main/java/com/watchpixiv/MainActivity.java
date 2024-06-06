package com.watchpixiv;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String baseUrl = "https://pixiv.re/";
    private String nowIdx = "";
    private int nowPage = 1;
    private boolean loadedImage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ImageView imageView = findViewById(R.id.image);
        Button buttonGet = findViewById(R.id.get);
        Button buttonLast = findViewById(R.id.last);
        Button buttonNext = findViewById(R.id.next);
        TextView msgText = findViewById(R.id.msgText);
        EditText idx = findViewById(R.id.idx);

        buttonGet.setOnClickListener((view) -> {
            nowIdx = idx.getText().toString();
            nowPage = 1;
            _load_image(msgText,imageView);
        });
        buttonLast.setOnClickListener((view) -> {
            if (nowPage > 1) {
                nowPage -= 1;
            }
            _load_image(msgText,imageView);
        });
        buttonNext.setOnClickListener((view) -> {
            nowPage += 1;
            _load_image(msgText,imageView);
        });
        imageView.setOnClickListener((view)-> {
            if(loadedImage) {
                bigImageLoader();
            }
        });

    }

    private void _load_image(@NonNull TextView msgText, ImageView imageView){
        loadedImage = false;
        msgText.setText("loading...");
        String url = baseUrl + nowIdx + ( nowPage==1 ? "" : "-" + nowPage ) + ".jpg";
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>(){
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        msgText.setText("Failed: "+url);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        msgText.setText("loaded: "+url);
                        loadedImage = true;
                        return false;
                    }
                })
                .into(imageView);
    }
    private void bigImageLoader(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.big_image_dialog_layout);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.color.transparent);
        dialog.getWindow().setDimAmount(1f);

        ImageView image = dialog.findViewById(R.id.bigImage);

        // 动态调整图像大小
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
        layoutParams.width = point.x;
        layoutParams.height = point.y;
        image.setLayoutParams(layoutParams);

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // TODO 处理手势
                return false;
            }
        });

        Glide.with(this)
                .load(baseUrl + nowIdx + ( nowPage==1 ? "" : "-" + nowPage ) + ".jpg")
                .into(image);

        dialog.show();

        image.setOnClickListener((view)->{
            Log.d("BigImage","Clicked");
            dialog.dismiss();
        });
    }
}