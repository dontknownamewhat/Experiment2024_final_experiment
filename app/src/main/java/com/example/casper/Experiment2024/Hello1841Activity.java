package com.example.casper.Experiment2024;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.casper.Experiment2024.model.CheckIn;
import com.example.casper.Experiment2024.model.CheckInViewModel;
import com.example.casper.Experiment2024.adapter.CheckInAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.content.Intent;
import android.location.Location;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

public class Hello1841Activity extends AppCompatActivity {

    private CheckInViewModel checkInViewModel;
    private RecyclerView recyclerView;
    private CheckInAdapter adapter;
    FusedLocationProviderClient fusedLocationClient;

    // UI组件
    private Button btn_take_photo, btn_choose_photo, btnAddCheckIn;
    private ImageView iv_image;
    private static final int TAKE_PHOTO_REQUEST = 333;
    private static final int CHOOSE_PHOTO_REQUEST = 444;
    private Uri selectedImageUri;  // 存储选择或拍摄的图片 URI
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello1841);

        // 设置 TabLayout 和 ViewPager2
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        TabPagerAdapter adapterViewPager = new TabPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("图书");
                    break;
                case 1:
                    tab.setText("搜索");
                    break;
                case 2:
                    tab.setText("地图");
                    break;
            }
        }).attach();

        // 初始化UI组件
        EditText etDescription = findViewById(R.id.etDescription);
        btnAddCheckIn = findViewById(R.id.btnAddCheckIn);
        recyclerView = findViewById(R.id.rvCheckIns);
        iv_image = findViewById(R.id.iv_image);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 初始化 ViewModel
        checkInViewModel = new ViewModelProvider(this).get(CheckInViewModel.class);

        // 观察 LiveData 并更新 RecyclerView
        checkInViewModel.getAllCheckIns().observe(this, checkIns -> {
            adapter = new CheckInAdapter(checkIns);
            recyclerView.setAdapter(adapter);
        });

        // 添加打卡记录
        btnAddCheckIn.setOnClickListener(v -> {
            String description = etDescription.getText().toString();
            if (!description.isEmpty()) {
                // 获取当前位置
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // 请求权限
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // 获取图片的路径
                        String imagePath = selectedImageUri != null ? selectedImageUri.getPath() : null;

                        // 创建一个新的 CheckIn 对象并插入
                        CheckIn checkIn = new CheckIn(description, latitude, longitude, imagePath);
                        checkInViewModel.insert(checkIn);
                        Log.d("LocationCheck", "Latitude: " + latitude + ", Longitude: " + longitude);
                        etDescription.setText(""); // 清空输入框
                        iv_image.setImageURI(null);  // 清空图片预览
                    } else {
                        Toast.makeText(this, "无法获取当前位置，记录失败。", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 拍照按钮
        btn_take_photo = findViewById(R.id.button_take_photo);
        btn_take_photo.setOnClickListener(view -> {
            Uri imageUri = createImageUri(Hello1841Activity.this);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        });

        // 从相册选择图片按钮
        btn_choose_photo = findViewById(R.id.button_select_photo);
        btn_choose_photo.setOnClickListener(view -> pickImageFromAlbum());
    }

    // 从相册选择图片
    public void pickImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO_REQUEST);
    }

    // 处理拍照和选择照片结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    selectedImageUri = data.getData();
                    iv_image.setImageURI(selectedImageUri);
                }
                break;

            case TAKE_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    iv_image.setImageURI(imageUri);
                    selectedImageUri = imageUri;  // 保存拍照的图片 URI
                }
                break;

            default:
                break;
        }
    }

    // 创建图片 URI
    private static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    // 定义 TabPagerAdapter 类
    private static class TabPagerAdapter extends FragmentStateAdapter {
        public TabPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BookListFragment(); // 图书 Tab
                case 1:
                    return new WebViewFragment(); // 搜索 Tab
                case 2:
                    return new TencentMapFragment(); // 地图 Tab
                default:
                    throw new IllegalStateException("Unexpected position " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 3; // 总共有三个 Tab
        }
    }
}
