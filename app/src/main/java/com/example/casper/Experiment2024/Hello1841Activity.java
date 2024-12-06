package com.example.casper.Experiment2024;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class Hello1841Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hello1841);

        // 设置 TabLayout 和 ViewPager2
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);

        // 创建 Adapter 并设置给 ViewPager2
        TabPagerAdapter adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);

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