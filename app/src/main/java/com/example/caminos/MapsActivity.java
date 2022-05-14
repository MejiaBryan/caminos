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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
    FirebaseDatabase firebaseDatabase;

    private ArrayList<Marker> tmpRealParaderos = new ArrayList<>();
    private ArrayList<Marker> RealParaderos = new ArrayList<>();
    private Double milat;
    private Double milon;

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
        Log.e("POSI","");
        p1 = puntosForRuta.get(1);
        Log.e("POSI",""+p1.toString());

        Polyline ruta = googleMap.addPolyline(new PolylineOptions()
                .clickable(true));



        mMap.setMyLocationEnabled(true);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,log), 4));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        }

    private void cargarParaderos() {
        mDatabase.child("Combis").child("Segrampo").addValueEventListener(new ValueEventListener() {
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
                    Log.e("MENSAJE3","Latitud: "+lat+"Longitud: "+lon);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(posi).title("Paradero").alpha(0.7f).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    tmpRealParaderos.add(mMap.addMarker(markerOptions));


                    //LatLng point = new LatLng(lat, lon);
                    //Log.e("MENSAJE","Latitud: "+lat+ " Longitud: "+lon);

                }
                RealParaderos.clear();
                RealParaderos.addAll(tmpRealParaderos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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