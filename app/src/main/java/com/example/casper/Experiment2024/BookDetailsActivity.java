package com.example.casper.Experiment2024;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BookDetailsActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextPrice;
    private Button buttonSave;
    private int position = -1; // 用于修改时记录位置
    private static final String PREFS_NAME = "ItemPrefs"; // SharedPreferences 文件名
    private static final String KEY_ITEM = "item"; // 存储 Item 对象的键
    private static final String TAG = "AddEditActivity"; // 用于标记日志

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

        // 读取 SharedPreferences 中保存的商品数据（如果有）
        loadItemData();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTextTitle.getText().toString();
                double price = Double.parseDouble(editTextPrice.getText().toString());
                Item newItem = new Item(title, price, R.drawable.book_1); // 设定默认图片

                // 保存数据到 SharedPreferences
                saveItemData(newItem);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("item", newItem);
                resultIntent.putExtra("position", position);
                setResult(RESULT_OK, resultIntent);
                finish(); // 关闭当前 Activity
            }
        });
    }

    // 从 SharedPreferences 中加载商品数据
    private void loadItemData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String itemString = sharedPreferences.getString(KEY_ITEM, null);
        if (itemString != null) {
            try {
                // 反序列化
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(itemString.getBytes());
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Item savedItem = (Item) objectInputStream.readObject();
                objectInputStream.close();

                if (savedItem != null) {
                    editTextTitle.setText(savedItem.getTitle());
                    editTextPrice.setText(String.valueOf(savedItem.getPrice()));
                }
            } catch (Exception e) {
                // 使用 Log 来记录异常信息
                Log.e(TAG, "Error loading item data", e);  // 记录错误日志，打印完整的异常堆栈
            }
        }
    }

    // 将商品对象保存到 SharedPreferences
    private void saveItemData(Item item) {
        try {
            // 序列化
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(item);
            objectOutputStream.close();

            String itemString = byteArrayOutputStream.toString();

            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_ITEM, itemString);
            editor.apply(); // 提交保存
        } catch (Exception e) {
            // 使用 Log 来记录异常信息
            Log.e(TAG, "Error saving item data", e);  // 记录错误日志，打印完整的异常堆栈
        }
    }
}
