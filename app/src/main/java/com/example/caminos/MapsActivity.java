package com.example.caminos;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.caminos.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PatternItem;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;

    private ArrayList<Marker> tmpRealParaderos = new ArrayList<>();
    private ArrayList<Marker> RealParaderos = new ArrayList<>();
    private Double milat;
    private Double milon;

    private String combi="Segrampo";
    PolylineOptions polylineOptions = new PolylineOptions();

    //Paraderos

    public LatLng p1,p2,p3,p4,p5,p6;
    private ArrayList<LatLng> puntosForRuta = new ArrayList<>();


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
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference();
        retornaMiPosicion();


    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            return;
        }
        cargarParaderos();

        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,log), 4));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        }

    private void cargarParaderos() {
        mDatabase.child("Combis").child(combi).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(Marker marker: RealParaderos) {
                    marker.remove();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e("MENSAJE",""+snapshot.getValue());
                    MapsPunts puntos = snapshot.getValue(MapsPunts.class);
                    //snapshot.getValue(MapsPunts.class).getLatitud();
                    //Log.e("MENSAJE2",""+puntos.getLatitud());
                    double lat = puntos.getLatitude();
                    double lon = puntos.getLongitude();
                    LatLng posi = new LatLng(lat,lon);
                    puntosForRuta.add(posi);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(posi).title("Paradero").alpha(0.7f).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    tmpRealParaderos.add(mMap.addMarker(markerOptions));

                }
                RealParaderos.clear();
                RealParaderos.addAll(tmpRealParaderos);
                graficaLinea();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void graficaLinea(){
        List<PatternItem> pattern = Arrays.asList(new Dot(), new Gap(20), new Dash(30), new Gap(20));
        //PolylineOptions polylineOptions = new PolylineOptions();
        for(int i = 0;i<=5;i++){
            p1 = puntosForRuta.get(i);
            polylineOptions.add(p1).color(Color.BLUE).geodesic(true).width(7).pattern(pattern);
        }
        Polyline polyline = mMap.addPolyline(polylineOptions);
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