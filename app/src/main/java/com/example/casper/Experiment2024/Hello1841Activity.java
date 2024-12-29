package com.example.casper.Experiment2024;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.example.casper.Experiment2024.model.CheckInViewModel;
import com.example.casper.Experiment2024.adapter.CheckInAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
public class Hello1841Activity extends AppCompatActivity {

    private CheckInViewModel checkInViewModel;
    private RecyclerView recyclerView;
    private CheckInAdapter adapter;
    FusedLocationProviderClient fusedLocationClient;
    private static final int TAKE_PHOTO_REQUEST_TWO = 444;
    private static final int TAKE_PHOTO_REQUEST_ONE = 333;
    private static final int TAKE_PHOTO_REQUEST_THREE = 555;
    // 集成拍照功能的相关变量
    private Button btn_take_photo, btn_choose_photo, btnAddCheckIn;
    private ImageView iv_image;
    private static final int TAKE_PHOTO_REQUEST = 333;
    private static final int CHOOSE_PHOTO_REQUEST = 444;
    private Uri selectedImageUri;  // 存储选择或拍摄的图片 URI
    private File photoFile;

    private Uri imageUri;
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
        btnAddCheckIn = findViewById(R.id.btnAddCheckIn);
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
                        String imagePath = photoFile.getAbsolutePath();
                        // 创建一个新的 CheckIn 对象并插入
                        CheckIn checkIn = new CheckIn(description, latitude, longitude,imagePath);
                        if (selectedImageUri != null) {
                            checkIn.setImagePath(selectedImageUri.toString());  // 保存照片路径
                        }
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

        // 集成拍照功能的相关代码
        btn_take_photo = findViewById(R.id.button_take_photo);
        btn_choose_photo = findViewById(R.id.button_select_photo);
        iv_image = findViewById(R.id.iv_image);

        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageUri = createImageUri(Hello1841Activity.this);
                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 如果不设置EXTRA_OUTPUT getData() 获取的是bitmap数据  是压缩后的
                startActivityForResult(intent, TAKE_PHOTO_REQUEST_ONE);
                try {
                    imageUri = TakePhotoUtils.takePhoto(Hello1841Activity.this, TAKE_PHOTO_REQUEST_THREE);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        btn_choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromAlbum();
            }
        });
    }
    public void pickImageFromAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 111);

    }
    // 处理拍照和选择照片结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
            case 222:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(Hello1841Activity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    Uri imageUri = data.getData();
                    Log.e("TAG", imageUri.toString());
                    iv_image.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO_REQUEST_ONE:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(Hello1841Activity.this, imageUri);
                    Toast.makeText(Hello1841Activity.this, "点击取消  拍照", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    //如果拍照图片过大会无法显示
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_image.setImageBitmap(bitmap1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TAKE_PHOTO_REQUEST_TWO:
                if (resultCode == RESULT_CANCELED) {
                    delteImageUri(Hello1841Activity.this, imageUri);
                    return;
                }
                Bitmap photo = data.getParcelableExtra("data");
                iv_image.setImageBitmap(photo);
                break;
            case TAKE_PHOTO_REQUEST_THREE:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(Hello1841Activity.this, "点击取消从相册选择", Toast.LENGTH_LONG).show();
                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), getOptions(imageUri.getPath()));
                iv_image.setImageBitmap(bitmap);
                break;
            default:
                break;
        }
    }


    private static Uri createImageUri(Context context) {
        String name = "takePhoto" + System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, name);
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".jpeg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        return uri;
    }


    public static void delteImageUri(Context context, Uri uri) {
        context.getContentResolver().delete(uri, null, null);

    }


    /**
     * 获取压缩图片的options
     *
     * @return
     */
    public static BitmapFactory.Options getOptions(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 4;      //此项参数可以根据需求进行计算
        options.inJustDecodeBounds = false;

        return options;
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
