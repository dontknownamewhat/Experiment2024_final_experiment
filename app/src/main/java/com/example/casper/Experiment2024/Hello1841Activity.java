package com.example.casper.Experiment2024;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.List;
import com.example.casper.Experiment2024.model.CheckInViewModel;
import com.example.casper.Experiment2024.adapter.CheckInAdapter;

public class Hello1841Activity extends AppCompatActivity {

    private CheckInViewModel checkInViewModel;
    private RecyclerView recyclerView;
    private CheckInAdapter adapter;

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
        recyclerView = findViewById(R.id.rvCheckIns);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                checkInViewModel.insert(new CheckIn(description));
                etDescription.setText(""); // 清空输入框
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
