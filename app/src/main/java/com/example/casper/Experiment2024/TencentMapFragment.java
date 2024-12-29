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
import android.icu.text.DecimalFormat;
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
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import java.util.Random;
public class TencentMapFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA_AND_LOCATION = 100;
    private MapView mapView;
    private TencentMap tencentMap;
    private CheckInViewModel viewModel;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private File photoFile;
    private ImageView ivTakenPhoto;

    // 暨南大学珠海校区的经纬度
    private static final LatLng JNU_ZHUHAI = new LatLng(22.2488, 113.5344);

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CheckInViewModel.class);
        Context context = getContext();

        if (context != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        } else {
            Log.e("TencentMap", "Context is null during FusedLocationProviderClient initialization.");
        }

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
                                    Log.w("TencentMap", entry.getKey() + " permission was permanently denied.");
                                }
                            }
                        }
                        if (allGranted) {
                            startCameraOrOtherOperation();
                        } else {
                            Toast.makeText(requireContext(), "一些必要的权限被拒绝，部分功能可能无法使用。", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TencentMapFragment", "Fragment loaded successfully!");
        // 初始化 fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (fusedLocationClient != null) {
            Log.d("TencentMap", "fusedLocationClient initialized successfully.");
        } else {
            Log.e("TencentMap", "fusedLocationClient initialization failed.");
        }

        mapView = view.findViewById(R.id.mapView);
        tencentMap = mapView.getMap();
        setupMap();
        ivTakenPhoto = view.findViewById(R.id.ivTakenPhoto);
        Button btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        Button btnZoomOut = view.findViewById(R.id.btn_zoom_out);

        // 设置放大按钮的点击事件
        btnZoomIn.setOnClickListener(v -> {
            if (tencentMap != null) {
                float currentZoom = tencentMap.getCameraPosition().zoom;
                tencentMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom + 1));
            }
        });

        // 设置缩小按钮的点击事件
        btnZoomOut.setOnClickListener(v -> {
            if (tencentMap != null) {
                float currentZoom = tencentMap.getCameraPosition().zoom;
                tencentMap.moveCamera(CameraUpdateFactory.zoomTo(currentZoom - 1));
            }
        });
        checkAndRequestPermissions(); // 在此检查并请求权限
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startCameraOrOtherOperation() {
        try {
            photoFile = createImageFile();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    "com.example.casper.Experiment2024.model.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            }
        } catch (IOException ex) {
            Log.e("TencentMap", "Error creating image file", ex);
        }
    }
    public void handleCheckInButtonClicked() {
        Log.d("TencentMapFragment", "Check-in button event received from MainActivity.");
        handleTakenPhoto();
    }
    private File createImageFile() throws IOException {
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
        requireActivity().runOnUiThread(() -> {
            if (ivTakenPhoto == null || photoFile == null || !photoFile.exists()) {
                Log.e("TencentMap", "Photo capture failed or file does not exist.");
                return;
            }

            ivTakenPhoto.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            if (bitmap != null) {
                ivTakenPhoto.setImageBitmap(bitmap);

                // 获取当前位置
                Context context = requireContext(); // 获取Fragment的上下文
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling ActivityCompat#requestPermissions here to request the missing permissions
                    return;
                }

                // 确保 fusedLocationClient 已经初始化
                if (fusedLocationClient != null) {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // 保存到数据库
                            String description = "拍照打卡记录"; // 可根据需求动态设置描述
                            viewModel.insertWithLocation(description, latitude, longitude);

                            // 在地图上标记
                            LatLng position = new LatLng(latitude, longitude);
                            tencentMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    .title("新打卡记录")
                                    .snippet(description));

                            Snackbar.make(requireView(), "打卡成功！位置已记录。", Snackbar.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(requireContext(), "无法获取当前位置，记录失败。", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("TencentMap", "fusedLocationClient is null.");
                }
            } else {
                Log.e("TencentMap", "Failed to decode the image file.");
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tencent_map, container, false);

        mapView = rootView.findViewById(R.id.mapView);
        tencentMap = mapView.getMap();
        setupMap();
        ivTakenPhoto = rootView.findViewById(R.id.ivTakenPhoto);

        checkAndRequestPermissions(); // 在此检查并请求权限
        return rootView;
    }

    private void setupMap() {
        if (tencentMap != null) {
            // 设置初始位置和缩放级别
            tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JNU_ZHUHAI, 15));

            // 设置缩放级别范围（根据需要调整）
            tencentMap.setMaxZoomLevel(20); // 最大放大级别
            tencentMap.setMinZoomLevel(3);  // 最小缩小级别，确保可以看到世界地图
            BitmapDescriptor customIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

            // 使用新的 MarkerOptions 构造函数，直接传递位置参数
            Marker jnuMarker = tencentMap.addMarker(new MarkerOptions(JNU_ZHUHAI) // 显式指定位置
                    .anchor(0.5f, 0.5f)
                    .icon(customIcon) // 设置为自定义图标
                    .title("暨南大学珠海校区"));

            // 显示信息窗口
            jnuMarker.showInfoWindow();

            // 调用方法以显示打卡标记
            showCheckInMarkers();

        } else {
            Log.e("TencentMap", "MapView or TencentMap object is null!");
        }
    }

    private void showCheckInMarkers() {
        // 从 ViewModel 获取所有打卡记录
        viewModel.getAllCheckIns().observe(getViewLifecycleOwner(), checkIns -> {
            if (tencentMap != null) {
                for (CheckIn checkIn : checkIns) {
                    double latitude=checkIn.getLatitude();
                    double longitude=checkIn.getLongitude();
                    //默认的打卡位置
                    if (latitude < 0 || latitude > 150 || longitude < 0 || longitude > 150) {
                        Random random = new Random();
                        DecimalFormat df = new DecimalFormat("#.####");

                        double rawLatitude = JNU_ZHUHAI.latitude + (random.nextDouble() * 0.002 - 0.001);
                        latitude = Double.parseDouble(df.format(rawLatitude));
                        double rawLongitude = JNU_ZHUHAI.longitude + (random.nextDouble() * 0.02 - 0.01);
                        longitude = Double.parseDouble(df.format(rawLongitude));
                    }
                    LatLng position = new LatLng(latitude, longitude);
                    String description = checkIn.getDescription();
                    String timestamp = checkIn.getTimestamp();
                    // 使用新的构造函数设置位置
                    Marker marker = tencentMap.addMarker(new MarkerOptions(position) // 显式指定位置
                            .title("打卡记录")
                            .snippet("描述: " + description + "\n时间: " + timestamp)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .anchor(0.5f, 0.5f)
                            .draggable(false)); // 禁止拖拽标记
                    marker.showInfoWindow(); // 显示信息窗口
                }
            } else {
                Log.e("TencentMap", "TencentMap is null while showing check-in markers.");
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
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), perm)) {
                    showPermissionExplanationDialog(perm);
                } else {
                    listPermissionsNeeded.add(perm);
                }
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissionLauncher.launch(listPermissionsNeeded.toArray(new String[0]));
        } else {
            startCameraOrOtherOperation();
        }
    }

    private void showPermissionExplanationDialog(String permission) {
        new AlertDialog.Builder(requireContext())
                .setTitle("权限请求")
                .setMessage("我们需要" + permission + "权限以正常使用此功能。请前往设置中开启。")
                .setPositiveButton("前往设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
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

