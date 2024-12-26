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
    private ImageView ivTakenPhoto;

    // 暨南大学珠海校区的经纬度
    private static final LatLng JNU_ZHUHAI = new LatLng(22.2559, 113.5415);

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
            if (ivTakenPhoto == null) {
                Log.e("TencentMap", "ivTakenPhoto is not initialized.");
                return;
            }

            if (photoFile == null || !photoFile.exists()) {
                Log.e("TencentMap", "Photo file does not exist or is null.");
                return;
            }

            ivTakenPhoto.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e("TencentMap", "Failed to decode the image file.");
            } else {
                ivTakenPhoto.setImageBitmap(bitmap);

                Snackbar.make(requireView(), "拍照成功！", Snackbar.LENGTH_LONG)
                        .setAction("查看", v -> {
                            // 当用户点击“查看”时的操作
                        })
                        .show();
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
        checkAndRequestPermissions();
        return rootView;
    }

    private void setupMap() {
        if (tencentMap != null) {
            tencentMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JNU_ZHUHAI, 15));

            tencentMap.addMarker(new MarkerOptions()
                    .position(JNU_ZHUHAI)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("暨南大学珠海校区"));

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
