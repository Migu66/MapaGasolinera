package com.example.mapagasolineras.interfaces;

import com.example.mapagasolineras.models.Gasolinera;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GasolineraAPI {
    @GET("ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/FiltroProvincia/{id_provincia}")
    Call<Gasolinera> obtenerGasolinerasPorProvincia(@Path("id_provincia") String idProvincia);

}
