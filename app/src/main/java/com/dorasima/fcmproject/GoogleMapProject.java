package com.dorasima.fcmproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.security.KeyStore;

public class GoogleMapProject extends AppCompatActivity {

    // 위치 권한
    String [] permission_list = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    LocationManager locationManager;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map_project);

        // 버전 체크
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permission_list,0);
        }else{
            init();
        }
    }

    @Override
    // 권한 체크
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int result : grantResults){
            if(result== PackageManager.PERMISSION_DENIED){
                return;
            }
        }
        init();
    }
    public void init(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment supportMapFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);

        MapReadyCallback mapReadyCallback = new MapReadyCallback();
        supportMapFragment.getMapAsync(mapReadyCallback);
    }
    
    // 구글 지도 사용준비가 완료되면 동작하는 콜백
    class MapReadyCallback implements OnMapReadyCallback{
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            // Log.d("test123", "구글 지도 사용 준비 완료");
            getMyLocation();
        }
    }
    // 현재 위치를 측정하는 메서드
    public void getMyLocation(){
        // LocationManager 개체 만들기
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        // 권한 확인 작업
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
                    // 어떤 코드
                    return;
                }
        }
        // 이전에 측정했던 값을 가져온다.
        // GPS와 Network 두 가지 방법으로 위치를 가져온다.
        Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location location2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // 위치를 세팅한다.
        if(location1!=null){
            setMyLocation(location1);
        }else{
            if(location2!=null){
                setMyLocation(location2);
            }
        }
        // 새롭게 측정한다.
        // 언제 측정될지 알 수 없기 때문에 리스너를 이용한다.
        GetMyLocationListener getMyLocationListener = new GetMyLocationListener();
        // 만약 GPS가 사용 가능한 상태라면
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            // GPS로 1초마다, 10m 이동하면 갱신,
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 10f,getMyLocationListener);
        }else{
            // 다른 코드
        }
        // GPS가 단말기 상에서는 사용가능하지만 위성과 연결이 안되어 있을 수 있다.
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)==true){
            // 그럴때는 네트워크로 위치를 파악한다.
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,1000,10f,getMyLocationListener);
        }
    }
    public void setMyLocation(Location location){
        Log.d("test123","위도: "+location.getLatitude());
        Log.d("test123","경도: "+location.getLongitude());

        // 위도와 경도값을 관리하는 개체
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        // CameraUpdate 개체를 이용한다.
        // 측정된 곳으로 카메라를 이동 시켜준다.
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(position);
        map.moveCamera(cameraUpdate);

        // 카메라를 15배 확대 시켜준다.
        CameraUpdate cameraUpdate2 = CameraUpdateFactory.zoomTo(15f);
        map.animateCamera(cameraUpdate2);
        
        // 현재 위치 표시
        // (권한 체크 코드를 위에 넣어주어야 한다.)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED){
                // 어떤 코드
                return;
            }
        }
        map.setMyLocationEnabled(true);

    }
    // 현재 위치 측정이 성공하면 반응하는 리스너
    class GetMyLocationListener implements LocationListener{
        @Override
        // 위치 측정을 하기위한 요소들이 사용이 가능하면 호출
        public void onProviderEnabled(@NonNull String provider) {}
        @Override
        // 위치 측정을 하기위한 요소들이 사용이 불가능하면 호출
        public void onProviderDisabled(@NonNull String provider) {}

        @Override
        // 현재 위치가 변경되면 호출
        public void onLocationChanged(@NonNull Location location) {
            setMyLocation(location);
            // 위치 측정을 중단한다.
            locationManager.removeUpdates(this);
        }
    }
}