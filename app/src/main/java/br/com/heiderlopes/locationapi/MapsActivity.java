package br.com.heiderlopes.locationapi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private final int REQUEST_GPS = 212;
    private String TAG = "APP";

    private GoogleApiClient mGoogleApiClient;

    //Como sera a taxa de atualizacao
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressWarnings({"MissingPermission"})
    private void startLocationUpdate() {
        initLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, MapsActivity.this);
    }

    private void stopLocationUpdate() {
        initLocationRequest();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapsActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            startLocationUpdate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient != null) {
            stopLocationUpdate();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        callConnection();
    }

    private synchronized void callConnection() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        checkPermission();

        configuraUltimaLocalizacao();

        initLocationRequest();
    }

    @SuppressWarnings({"MissingPermission"})
    private void configuraUltimaLocalizacao() {

        initLocationRequest();

        Location l = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (l != null) {
            Log.i("LOG", "latitude: " + l.getLatitude());
            Log.i("LOG", "longitude: " + l.getLongitude());
            LatLng localizacao = new LatLng(l.getLatitude(), l.getLongitude());
            mMap.addMarker(new MarkerOptions().position(localizacao).title("Minha Localizacao"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(localizacao));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(localizacao).zoom(16.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void checkPermission() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("", "Permissão para gravar negada");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Necessária a permissao para GPS")
                        .setTitle("Permissao Requerida");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        requestPermission();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                requestPermission();
            }
        }
    }

    protected void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_GPS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GPS: {
                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permissão negada pelo usuário");
                } else {
                    Log.i(TAG, "Permissao concedida pelo usuario");
                    configuraUltimaLocalizacao();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Latitude", String.valueOf(location.getLatitude()));
        Log.i("Logitude", String.valueOf(location.getLongitude()));
        Log.i("Altitude", String.valueOf(location.getAltitude()));
        Log.i("Provider", String.valueOf(location.getProvider()));
        Log.i("Accuracy", String.valueOf(location.getAccuracy()));
    }
}