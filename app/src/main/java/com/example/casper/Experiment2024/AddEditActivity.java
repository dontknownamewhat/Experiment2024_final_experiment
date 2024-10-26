package com.example.casper.Experiment2024;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextPrice;
    private Button buttonSave;
    private int position = -1; // 用于修改时记录位置

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextPrice = findViewById(R.id.editTextPrice);
        buttonSave = findViewById(R.id.buttonSave);

        // 检查是否是修改操作
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("item")) {
            Item item = (Item) intent.getSerializableExtra("item");
            if (item != null) {
                editTextTitle.setText(item.getTitle());
                editTextPrice.setText(String.valueOf(item.getPrice()));
                position = intent.getIntExtra("position", -1);
            }
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString();
                double price = Double.parseDouble(editTextPrice.getText().toString());
                Item newItem = new Item(title, price, R.drawable.book_1); // 设定默认图片

                Intent resultIntent = new Intent();
                resultIntent.putExtra("item", newItem);
                resultIntent.putExtra("position", position);
                setResult(RESULT_OK, resultIntent);
                finish(); // 关闭当前 Activity
            }
        });
    }
}
