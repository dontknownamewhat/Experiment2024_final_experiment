package com.example.casper.Experiment2024;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImageViewActivity extends AppCompatActivity {

    private Button buttonPrevious, buttonNext;
    private ImageView imageViewFunny;
    private int[] imageIDArray = {
            R.drawable.funny_1, R.drawable.funny_2,
            R.drawable.funny_3, R.drawable.funny_4,
            R.drawable.funny_5, R.drawable.funny_6
    };
    private int imageIDArrayCurrentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        // 初始化 Button 和 ImageView
        buttonPrevious = findViewById(R.id.button_previous);
        buttonNext = findViewById(R.id.button_next);
        imageViewFunny = findViewById(R.id.image_view_funny);

        // 设置初始图片
        imageViewFunny.setImageResource(imageIDArray[imageIDArrayCurrentIndex]);

        // 设置按钮点击事件
        buttonPrevious.setOnClickListener(new MyButtonClickListener());
        buttonNext.setOnClickListener(new MyButtonClickListener());
    }

    private class MyButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.button_next) {
                // 点击“下一个”
                if (imageIDArrayCurrentIndex < imageIDArray.length - 1) {
                    imageIDArrayCurrentIndex++;
                } else {
                    imageIDArrayCurrentIndex = 0; // 循环到第一张图片
                }
            } else if (view.getId() == R.id.button_previous) {
                // 点击“上一个”
                if (imageIDArrayCurrentIndex > 0) {
                    imageIDArrayCurrentIndex--;
                } else {
                    imageIDArrayCurrentIndex = imageIDArray.length - 1; // 循环到最后一张图片
                }
            }
            // 更新 ImageView 显示的图片
            imageViewFunny.setImageResource(imageIDArray[imageIDArrayCurrentIndex]);
        }
    }
}
