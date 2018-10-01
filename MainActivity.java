package com.example.lucianolimina.signalmap;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSION_REQUEST = 1;
    private final int REQUEST_CHECK_SETTING = 0x01;

    private TextView latitude;
    private TextView longitude;
    private TextView altitude;

    private TextView signalStrengt;
    private TextView carrier;

    private Button start;


    private LocationRequest locationRequest;
    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallback;


    private MyPhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = findViewById(R.id.latitudeTxt);
        longitude = findViewById(R.id.longitudeTxt);
        altitude = findViewById(R.id.altitudeTxt);
        signalStrengt = findViewById(R.id.signalTxt);
        carrier = findViewById(R.id.carrierTxt);
        start = findViewById(R.id.startButton);

        // Richiesta permesso run-time
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST);
        }

        //Controllo attivazione posizione
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null)
                    return;

                //Prendi i dati dalla list e li mette nella view
                for (Location location : locationResult.getLocations()) {
                    latitude.setText("" + location.getLatitude());
                    longitude.setText("" + location.getLongitude());
                    altitude.setText(""+ location.getAltitude());
                }
            }
        };

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        //Analisi risposta
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("Position", "GPS ATTIVO");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTING);
                        Thread.sleep(1000);
                    } catch (IntentSender.SendIntentException sendEx) {

                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        // Client posizione
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //Client segnale

        phoneStateListener = new MyPhoneStateListener();
        phoneStateListener.setSignalPowertText(signalStrengt);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);




        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

                carrier.setText(telephonyManager.getSimOperator());
                Log.d("SIGNAL INFO", "" + telephonyManager.getSimOperator() + signalStrengt);

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }


                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                //Prende l'ultima posizione

                locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location == null) {
                            longitude.setText("Not available");
                            latitude.setText("Not available");
                            altitude.setText("Not available");
                        }
                        else {
                            longitude.setText("" + location.getLongitude());
                            latitude.setText("" + location.getLatitude());
                            altitude.setText("" + location.getAltitude());
                        }
                    }
                });

                //Get signal strength


            }
        });

    }
}
