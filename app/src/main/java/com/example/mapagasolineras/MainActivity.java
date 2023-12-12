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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private RequestQueue mQueue;

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mQueue = Volley.newRequestQueue(this);

        // Llama directamente al método getCurrentLocation() al inicio de la actividad
        getCurrentLocation();
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

                            // Realiza la solicitud y parseo del JSON aquí
                            jsonParse();
                        } else {
                            Toast.makeText(MainActivity.this, "Por favor, active su ubicación para utilizar la aplicación", Toast.LENGTH_SHORT).show();
                        }

                        mMap.setOnMapClickListener(MainActivity.this);
                        mMap.setOnMapLongClickListener(MainActivity.this);

                        LatLng ubicacion = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));
                    }
                });
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

                        String latitudStr = gasolinera.getString("Latitud").replace(",", ".");
                        String longitudStr = gasolinera.getString("Longitud (WGS84)").replace(",", ".");
                        String rotulo = gasolinera.getString("Rótulo");
                        String cpostal = gasolinera.getString("C.P.");
                        String direccion = gasolinera.getString("Dirección");

                        try {
                            LatLng g1 = new LatLng(Double.parseDouble(latitudStr), Double.parseDouble(longitudStr));
                            mMap.addMarker(new MarkerOptions().position(g1).title(rotulo).snippet(cpostal + ", " + direccion));

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

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Manejo de clics en el mapa si es necesario
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        // Manejo de clics largos en el mapa si es necesario
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // No es necesario implementar código aquí
    }
}
