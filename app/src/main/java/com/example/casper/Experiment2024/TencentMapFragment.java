package com.example.casper.Experiment2024;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.tencent.map.sdk.a.pz;
import com.example.casper.Experiment2024.model.CheckIn;
import com.example.casper.Experiment2024.model.CheckInViewModel;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;

public class TencentMapFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_AND_LOCATION = 100;
    private MapView mapView;
    private TencentMap tencentMap;
    private CheckInViewModel viewModel;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private File photoFile;
    private ImageView ivTakenPhoto;    // 暨南大学珠海校区的经纬度
    private static final LatLng JNU_ZHUHAI = new LatLng(22.2559, 113.5415);
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;
    // 在成员变量中添加ActivityResultLauncher
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckInViewModel.class);

        // 设置ActivityResultLauncher来处理拍照结果
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == getActivity().RESULT_OK) {
                            handleTakenPhoto();
                        }
                    }
                }
        );

        // 设置ActivityResultLauncher来处理权限请求结果
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> permissions) {
                        boolean allGranted = true;
                        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                            if (!entry.getValue()) {
                                allGranted = false;
                                Log.w("TencentMap", entry.getKey() + " permission was denied.");
                                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), entry.getKey())) {
                                    // 用户选择了“不再询问”
                                    Log.w("TencentMap", entry.getKey() + " permission was permanently denied.");
                                }
                            }
                        }
                        if (allGranted) {
                            startCameraOrOtherOperation(); // 所有权限都被授予
                        } else {
                            // 至少有一个权限被拒绝，处理这种情况
                            Toast.makeText(requireContext(), "一些必要的权限被拒绝，部分功能可能无法使用。", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void startCameraOrOtherOperation() {
        try {
            photoFile = createImageFile();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.example.casper.Experiment2024.model.fileprovider", // 替换为你的包名 + ".fileprovider"
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            }
        } catch (IOException ex) {
            Log.e("TencentMap", "Error creating image file", ex);
        }
    }
    private File createImageFile() throws IOException {
        // 创建一个以当前时间命名的图像文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }
    private void handleTakenPhoto() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ivTakenPhoto == null) {
                    Log.e("TencentMap", "ivTakenPhoto is not initialized.");
                    return;
                }

                // 检查 photoFile 是否存在
                if (photoFile == null || !photoFile.exists()) {
                    Log.e("TencentMap", "Photo file does not exist or is null.");
                    return;
                }

                ivTakenPhoto.setVisibility(View.VISIBLE); // 显示ImageView
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap == null) {
                    Log.e("TencentMap", "Failed to decode the image file.");
                } else {
                    ivTakenPhoto.setImageBitmap(bitmap);

                    // 使用 Snackbar 显示拍照成功的消息，并提供查看照片的选项
                    Snackbar.make(requireView(), "拍照成功！", Snackbar.LENGTH_LONG)
                            .setAction("查看", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 当用户点击“查看”时的操作，例如滚动到图片或打开大图查看
                                    // 这里可以留空或者实现具体的查看逻辑
                                }
                            })
                            .show();
                }
            }
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tencent_map, container, false);

        // 初始化地图控件
        mapView = rootView.findViewById(R.id.mapView);

        // 获取 TencentMap 对象
        tencentMap = mapView.getMap();

        setupMap();
        // 获取按钮和ImageView
        Button btnCheckInWithPhoto = rootView.findViewById(R.id.btnCheckInWithPhoto);
        ivTakenPhoto = rootView.findViewById(R.id.ivTakenPhoto);

        // 设置按钮点击事件
        btnCheckInWithPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            // 已经有权限，可以直接调用 pz.f
            initializeMapSDK();
        }
        // 检查并请求必要的权限
        checkAndRequestPermissions();
        return rootView;
    }

    private void setupMap() {
        if (tencentMap != null) {
            // 设置地图中心点和缩放级别
            tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JNU_ZHUHAI, 15)); // 推荐缩放级别

            // 添加图标型 Marker
            tencentMap.addMarker(new MarkerOptions()
                    .position(JNU_ZHUHAI)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("暨南大学珠海校区"));

            // 显示打卡记录
            showCheckInMarkers();
        } else {
            Log.e("TencentMap", "MapView or TencentMap object is null!");
        }
    }

    private void showCheckInMarkers() {
        viewModel.getAllCheckIns().observe(getViewLifecycleOwner(), checkIns -> {
            for (CheckIn checkIn : checkIns) {
                LatLng position = new LatLng(checkIn.getLatitude(), checkIn.getLongitude());
                tencentMap.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title("打卡记录")
                        .snippet(checkIn.getDescription()));
            }
        });
    }
    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), perm) != PackageManager.PERMISSION_GRANTED) {
                // 如果用户之前拒绝过该权限，并且选择了“不再询问”
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), perm)) {
                    // 显示解释信息
                    showPermissionExplanationDialog(perm);
                } else {
                    listPermissionsNeeded.add(perm);
                }
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(listPermissionsNeeded.toArray(new String[0]));
        } else {
            // 已经有所有权限，可以启动相机或进行其他操作
            startCameraOrOtherOperation();
        }
    }
    private void showPermissionExplanationDialog(String permission) {
        // 创建并显示一个对话框，向用户解释为什么需要这个权限
        // 并提供一个按钮引导用户前往设置页面开启权限
        new AlertDialog.Builder(requireContext())
                .setTitle("权限请求")
                .setMessage("我们需要" + permission + "权限以正常使用此功能。请前往设置中开启。")
                .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    private void initializeMapSDK() {
        try {
            // 获取上下文对象
            Context context = requireContext();

            // 调用 pz.f 方法获取设备标识符
            String deviceId = pz.f(context);

            // 检查是否成功获取到设备标识符
            if (deviceId != null && !deviceId.isEmpty()) {
                Log.d("TencentMap", "Device ID: " + deviceId);
                // 使用获得的设备标识符初始化地图 SDK 或进行其他操作
            } else {
                Log.w("TencentMap", "Failed to get device identifier.");
                // 提供替代逻辑或通知用户
            }

        } catch (Exception e) {
            Log.e("TencentMap", "Failed to initialize map SDK with device identifier.", e);
            // 提供替代逻辑或通知用户
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

}