package com.example.caminos;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.caminos.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    DatabaseReference mDatabase;
    double milat;
    double milon;
    LatLng par1, par2, par3, par4, par5, par6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //CONTRUCCION DEL FRAGMENT
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        retornaMiPosicion();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<LatLng> points = null;
        ArrayList<LatLng> puntos  = new ArrayList<>();
        ArrayList<LatLng> puntosReal  = new ArrayList<>();
        PolylineOptions lineOptions = null;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation();

        //LatLng p = new LatLng(-27.684, 133.903);
        //mDatabase.child("Combis").child("Segrampo").push().setValue(p);

        cargarPuntos(puntosReal, puntos);
        double lat = puntosReal.get(1).latitude;
        double log = puntosReal.get(1).longitude;
        /*Polyline polyline1 = googleMap.addPolyline(new PolylineOptions().
                clickable(true).
                add(puntosReal.get(1),puntosReal.get(2),puntosReal.get(3), puntosReal.get(4)));
*/
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,log), 4));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        }

    private void cargarPuntos(ArrayList<LatLng> puntosReal, ArrayList<LatLng> puntos) {
        for(int i=1;i<=6;i++){
            String a = ""+i;
            mDatabase.child("Combis").child("Segrampo").child(a).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(LatLng latlng: puntosReal){
                       puntosReal.remove(latlng);
                    }
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MapsPunts mp = snapshot.getValue(MapsPunts.class);
                    double lat = mp.getLatitud();
                    double lon = mp.getLongitud();
                    LatLng point = new LatLng(lat, lon);
                    Log.e("MENSAJE","Latitud: "+lat+ " Longitud: "+lon);
                    puntos.add(point);
                    }
                    puntosReal.clear();
                    puntosReal.addAll(puntos);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }


    private void retornaMiPosicion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            HashMap<String,Object> miposi = new HashMap<>();
                            miposi.put("latitud",location.getLatitude());
                            miposi.put("longitud",location.getLongitude());
                            milat = location.getLatitude();
                            milon = location.getLongitude();

                            //miposi = new LatLng(location.getLatitude(),location.getLongitude());
                            //mDatabase.child("myUbi").push().setValue(latlang);
                        }
                    }
                });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }
}