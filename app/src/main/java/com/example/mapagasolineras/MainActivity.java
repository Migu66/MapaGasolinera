package com.example.mapagasolineras;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private TextView mTextViewResult;
    private RequestQueue mQueue;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    EditText txtLatitud, txtLongitud;
    GoogleMap mMap;
    String n = "19";

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


        mTextViewResult = findViewById(R.id.text_view_result);
        Button buttonParse = findViewById(R.id.button_parse);

        mQueue = Volley.newRequestQueue(this);

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonParse();
            }
        });


    }

    private void jsonParse() {
        String url = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/FiltroProvincia/19";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("ListaEESSPrecio");

                    for(int i = 0; i < jsonArray.length(); i++){
                        JSONObject gasolinera = jsonArray.getJSONObject(i);

                        String latitudStr = gasolinera.getString("Latitud");
                        String longitudStr = gasolinera.getString("Longitud (WGS84)");

                        try {

                            mTextViewResult.append((latitudStr + " " + longitudStr + " "));
                            double latitud = Double.parseDouble(latitudStr);
                            double longitud = Double.parseDouble(longitudStr);

                            LatLng gasolineraLatLng = new LatLng(latitud, longitud);
                            mMap.addMarker(new MarkerOptions().position(gasolineraLatLng));
                        } catch (NumberFormatException e) {
                            Log.e("GasolineraParsing", "Error parsing coordinates: " + e.getMessage());
                        }
                    }
                } catch (JSONException e) {
                    Log.e("GasolineraJSON", "Error parsing JSON: " + e.getMessage());
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("GasolineraAPI", "Error fetching gas stations: " + error.getMessage());
            }
        });

        mQueue.add(request);
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
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Aqui se encuentra");
                            mMap.addMarker(markerOptions);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        } else {
                            Toast.makeText(MainActivity.this, "Por favor, active su ubicación para utilizar la aplicación", Toast.LENGTH_SHORT).show();
                        }

                        mMap.setOnMapClickListener(MainActivity.this);
                        mMap.setOnMapLongClickListener(MainActivity.this);

                        LatLng guadalajara = new LatLng(40.635479, -3.174281);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(guadalajara));


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


}
