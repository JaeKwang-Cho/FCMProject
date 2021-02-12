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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleMapProject extends AppCompatActivity {

    // 위치 권한
    String [] permission_list = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    LocationManager locationManager;
    GoogleMap map;

    // JSON 데이터를 담을 리스트
    ArrayList<Double> lat_list = new ArrayList<Double>();
    ArrayList<Double> lng_list = new ArrayList<Double>();
    ArrayList<String> name_list = new ArrayList<String>();
    ArrayList<String> vicinity_list = new ArrayList<String>();

    // 구글맵의 Marker 개체를 담을 리스트
    ArrayList<Marker> marker_list = new ArrayList<Marker>();

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

        // 주변 정보 가져오는 스레드
        PlaceNetworkThread placeNetworkThread = new PlaceNetworkThread(location.getLatitude(),location.getLongitude());
        placeNetworkThread.start();

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
    // 구글 서버에서 주변 정보를 받아오기 위한 스레드
    class PlaceNetworkThread extends Thread{
        private double lat, lng;
        // 생성자로 위도 경도를 받는다.
        PlaceNetworkThread(double lat, double lng){
            this.lat = lat;
            this.lng= lng;
        }
        @Override
        public void run() {
            super.run();

            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder();
            // 사이트를 지정한다.
            String site = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                    +"?key=AIzaSyCcQpmHbJ3fvcvbCR-i3wlqXvg96O9jFZM"
                    +"&location="+lat+","+lng
                    +"&radius=1000"
                    +"&language=ko"
                    +"&type=restaurant";
            Log.d("test123",site);

            builder = builder.url(site);
            Request request = builder.build();

            PlaceCallback placeCallback = new PlaceCallback();
            Call call = client.newCall(request);
            call.enqueue(placeCallback);
        }
    }
    class PlaceCallback implements Callback{
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            // 실패 했을 때 코드
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            try{
                String result = response.body().string();
                // JSON 양식으로 읽어온다.
                JSONObject jsonObject = new JSONObject(result);
                String status = jsonObject.getString("status");
                if (status.equals("OK")) {
                    JSONArray results = jsonObject.getJSONArray("results");

                    lat_list.clear();
                    lng_list.clear();
                    name_list.clear();
                    vicinity_list.clear();
                    for(int i = 0;i<results.length();i++){
                        JSONObject object = results.getJSONObject(i);

                        // 위도 경도를 뽑는다.
                        JSONObject geometry = object.getJSONObject("geometry");
                        JSONObject location = geometry.getJSONObject("location");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");

                         // 이름을 뽑는다.
                        String name = object.getString("name");

                        // 인근 정보를 뽑는다.
                        String vicinity = object.getString("vicinity");

                        lat_list.add(lat);
                        lng_list.add(lng);
                        name_list.add(name);
                        vicinity_list.add(vicinity);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 지도에 표시되어 있는 마커를 제거한다.
                            for(Marker marker:marker_list){
                                marker.remove();
                            }
                            marker_list.clear();

                            for(int i = 0;i<lat_list.size();i++){
                                double lat = lat_list.get(i);
                                double lng = lng_list.get(i);
                                String name = name_list.get(i);
                                String vicinity = vicinity_list.get(i);

                                LatLng position = new LatLng(lat,lng);
                                // 마커 세팅을 도와주는 MarkerOptions 개체
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(position);

                                // 말풍선을 세팅할 수도 있다.
                                markerOptions.title(name);
                                markerOptions.snippet(vicinity);

                                // 마커 이미지를 설정할 수 도 있다.
                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(
                                        android.R.drawable.ic_menu_mylocation);
                                markerOptions.icon(bitmapDescriptor);

                                // 마커를 맵에 세팅한다.
                                Marker marker = map.addMarker(markerOptions);
                                marker_list.add(marker);

                            }
                        }
                    });
                }
                Log.d("test123",result);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}