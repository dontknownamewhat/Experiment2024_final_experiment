package com.example.casper.Experiment2024;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
public class Hello1841Activity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello1841);
        // 定义成员变量保存点击的数字

        // 获取数字按钮并设置点击事件
        for (int i = 0; i <= 9; i++) {
            String buttonID = "buttonview_number_" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            Button numberButton = findViewById(resID);
            numberButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickedNumber += ((Button) v).getText().toString();
                }
            });
        }

        // 清除按钮的点击事件
        Button cleanButton = findViewById(R.id.buttonview_clean);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出AlertDialog确认清除操作
                new AlertDialog.Builder(Hello1841Activity.this)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure to clean the number?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            clickedNumber = "";
                            Toast.makeText(Hello1841Activity.this, "Number cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        // 输入按钮的点击事件
        Button inputButton = findViewById(R.id.buttonview_input);
        inputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast消息显示输入的数字
                Toast.makeText(Hello1841Activity.this, "You have input number: " + clickedNumber, Toast.LENGTH_SHORT).show();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textView = findViewById(R.id.textview_helloworld);
        textView.setText(R.string.hello_jnu);  // 动态设置为 hello_jnu
    }
    private String clickedNumber = "";
}