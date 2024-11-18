package com.example.test3;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    ConstraintLayout constraintLayout1;
    LinearLayout linearLayout1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient; // 현재 위치 제공
    private NaverMap naverMap;
    private DatabaseReference database;
    private TextView textView;

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        constraintLayout1 = findViewById(R.id.constraintlayout);
        linearLayout1 = findViewById(R.id.linearlayout);

        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        // TextView 참조
        textView = findViewById(R.id.TextView);

        // Firebase Database 인스턴스 참조
        database = FirebaseDatabase.getInstance("https://my-bus2-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

        // Firebase에서 데이터 가져오기
        database.child("example_data").child("message")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String message = snapshot.getValue(String.class);
                            textView.setText(message);
                        } else {
                            textView.setText("데이터가 없습니다.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        textView.setText("데이터 로드 실패: " + error.getMessage());
                    }
                });

        // FusedLocationSource 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this); // FusedLocationProviderClient 초기화

        // MapFragment 가져오기
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.map_fragment, mapFragment)
                    .commit();
        }

        // 지도 비동기 초기화
        mapFragment.getMapAsync(this);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 지도에 LocationSource 설정
        naverMap.setLocationSource(locationSource);

        // 권한 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 현재 위치 가져오기
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // 현재 위치 위도, 경도
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentPosition = new LatLng(latitude, longitude);

                    // 지도 카메라 이동
                    naverMap.moveCamera(CameraUpdate.scrollTo(currentPosition));

                    // LocationOverlay에 현재 위치 표시
                    LocationOverlay locationOverlay = naverMap.getLocationOverlay();
                    locationOverlay.setVisible(true);
                    locationOverlay.setPosition(currentPosition);
                } else {
                    Toast.makeText(MainActivity.this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 위치 추적 모드 활성화
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
    }

    public void onButtonClicked_map(View v) {
        constraintLayout1.setVisibility(View.INVISIBLE);
        linearLayout1.setVisibility(View.VISIBLE);
    }

    public void onButtonClicked_back(View v) {
        constraintLayout1.setVisibility(View.VISIBLE);
        linearLayout1.setVisibility(View.INVISIBLE);
    }
}
