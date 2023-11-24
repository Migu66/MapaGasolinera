package com.example.mapagasolineras;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mapagasolineras.interfaces.GasolineraAPI;
import com.example.mapagasolineras.models.Gasolinera;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    EditText txtLatitud, txtLongitud;
    GoogleMap mMap;
    String n = "19";
    String latitud, longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getCurrentLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).check();
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        mMap = googleMap;

                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Aqui se encuentra").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            mMap.addMarker(markerOptions);

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        } else {
                            Toast.makeText(MainActivity.this, "Por favor, active su ubicación para utilizar la aplicación", Toast.LENGTH_SHORT).show();
                        }

                        mMap.setOnMapClickListener(MainActivity.this);
                        mMap.setOnMapLongClickListener(MainActivity.this);

                        LatLng guadalajara = new LatLng(40.635479, -3.174281);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(guadalajara));

                        find(n);

                        LatLng g1 = new LatLng(40.64390338991715, -3.142959697317834);
                        googleMap.addMarker(new MarkerOptions().position(g1));

                        LatLng g2 = new LatLng(40.63366404469122, -3.1702702570533714);
                        googleMap.addMarker(new MarkerOptions().position(g2));

                        LatLng g3 = new LatLng(40.63280456713761, -3.1828241483232027);
                        googleMap.addMarker(new MarkerOptions().position(g3));

                        LatLng g4 = new LatLng(40.63579518051684, -3.189177109407755);
                        googleMap.addMarker(new MarkerOptions().position(g4));

                    }
                });
            }
        });
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (txtLatitud != null && txtLongitud != null) {
            txtLatitud.setText(String.valueOf(latLng.latitude));
            txtLongitud.setText(String.valueOf(latLng.longitude));
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (txtLatitud != null && txtLongitud != null) {
            txtLatitud.setText(String.valueOf(latLng.latitude));
            txtLongitud.setText(String.valueOf(latLng.longitude));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
    }

    public void find(String idProvincia) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://sedeaplicaciones.minetur.gob.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GasolineraAPI gasolineraAPI = retrofit.create(GasolineraAPI.class);
        Call<Gasolinera> call = gasolineraAPI.obtenerGasolinerasPorProvincia(idProvincia);
        call.enqueue(new Callback<Gasolinera>() {
            @Override
            public void onResponse(Call<Gasolinera> call, Response<Gasolinera> response) {
                if (response.isSuccessful()) {
                    Gasolinera gasolinera = response.body();
                    List<Gasolinera> gasolinerasList = gasolinera.getListaGasolineras();

                    if (gasolinerasList != null) {
                        for (Gasolinera gasolineraItem : gasolinerasList) {
                            double latitud = Double.parseDouble(gasolineraItem.getLatitud());
                            double longitud = Double.parseDouble(gasolineraItem.getLongitud());
                            String localidad = gasolineraItem.getLocalidad();

                            LatLng gasolineraLatLng = new LatLng(latitud, longitud);
                            mMap.addMarker(new MarkerOptions().position(gasolineraLatLng).title(localidad));
                        }
                        Toast.makeText(MainActivity.this, "Marcadores agregados al mapa", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "No se encontraron datos de gasolineras", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error en la respuesta del servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Error en la respuesta del servidor: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Gasolinera> call, Throwable t) {

            }


        });

    }
}
