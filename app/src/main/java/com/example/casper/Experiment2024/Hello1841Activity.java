package com.example.casper.Experiment2024;

import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.List;
import com.example.casper.Experiment2024.model.CheckInViewModel;
import com.example.casper.Experiment2024.adapter.CheckInAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
public class Hello1841Activity extends AppCompatActivity {

    private CheckInViewModel checkInViewModel;
    private RecyclerView recyclerView;
    private CheckInAdapter adapter;
    FusedLocationProviderClient fusedLocationClient;
    // 集成拍照功能的相关变量
    private Button btn_take_photo, btn_choose_photo;
    private ImageView iv_image;
    private static final int TAKE_PHOTO_REQUEST = 333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello1841);

        // 设置 TabLayout 和 ViewPager2
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);

        // 创建 Adapter 并设置给 ViewPager2
        TabPagerAdapter adapterViewPager = new TabPagerAdapter(this);
        viewPager.setAdapter(adapterViewPager);

        // 将 TabLayout 和 ViewPager2 关联起来
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

        // 初始化打卡相关的UI组件
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnAddCheckIn = findViewById(R.id.btnAddCheckIn);
        btnAddCheckIn.setOnClickListener(v -> {

        });
        recyclerView = findViewById(R.id.rvCheckIns);
        // 动态设置最大高度
        recyclerView.post(() -> {
            int maxHeight = (int) (20 * getResources().getDisplayMetrics().density); // 最大高度200dp
            if (recyclerView.getHeight() > maxHeight) {
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = maxHeight;
                recyclerView.setLayoutParams(params);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                            latitude = 22.2559; // 珠海纬度
                            longitude = 113.5415; // 珠海经度
                        }
                        // 创建一个新的 CheckIn 对象并插入
                        CheckIn checkIn = new CheckIn(description, latitude, longitude);
                        checkInViewModel.insert(checkIn);
                        Log.d("LocationCheck", "Latitude: " + latitude + ", Longitude: " + longitude);
                        etDescription.setText(""); // 清空输入框
                    } else {
                        Toast.makeText(this, "无法获取当前位置，记录失败。", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 集成拍照功能的相关代码
        btn_take_photo = findViewById(R.id.btn_take_photo);
        btn_choose_photo = findViewById(R.id.btn_choose_photo);
        iv_image = findViewById(R.id.iv_image);

        btn_take_photo.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Hello1841Activity.this, TakePhotoActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "无法启动拍照功能", Toast.LENGTH_SHORT).show();
            }
        });

        btn_choose_photo.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, TAKE_PHOTO_REQUEST);
            } catch (Exception e) {
                Toast.makeText(this, "无法启动选择照片功能", Toast.LENGTH_SHORT).show();
            }
        });
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
