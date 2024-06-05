package com.watchpixiv;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;

import org.w3c.dom.Text;

import java.lang.annotation.Target;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String baseUrl = "https://pixiv.re/";
    private String nowIdx = "";
    private int nowPage = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

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
            if (Objects.equals(nowIdx, "")){
                // TODO
            }
            if (nowPage > 1) {
                nowPage -= 1;
            }
            _load_image(msgText,imageView);
        });
        buttonNext.setOnClickListener((view) -> {
            if (Objects.equals(nowIdx, "")){
                // TODO
            }
            nowPage += 1;
            _load_image(msgText,imageView);
        });

    }

    private void _load_image(@NonNull TextView msgText, ImageView imageView){
        msgText.setText("loading...");
        if(nowPage == 1){
            Glide.with(this)
                    .load(baseUrl + nowIdx + ".jpg")
                    .listener(new RequestListener<Drawable>(){
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            msgText.setText("Failed: "+baseUrl + nowIdx + ".jpg");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            msgText.setText("loaded: "+baseUrl + nowIdx + ".jpg");
                            return false;
                        }
                    })
                    .into(imageView);

        }else{
            Glide.with(this)
                    .load(baseUrl + nowIdx + "-" + nowPage + ".jpg")
                    .listener(new RequestListener<Drawable>(){
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                            msgText.setText("Failed: "+baseUrl + nowIdx + "-" + nowPage + ".jpg");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            msgText.setText("loaded: "+baseUrl + nowIdx + "-" + nowPage + ".jpg");
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}