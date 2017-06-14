package android.sys.dist.distsysandroid;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private EditText editText, editText2;
    private LinearLayout linearLayout, linearLayout2, linearLayout3;
    private MarkerOptions startPoint, endPoint;
    private boolean isStartingPointConfirmed = false,
            isEndingointConfirmed = false;
    private NumberFormat formatter;
    private EditText editText3;
    private EditText editText4;
    private String ip, port;
    private LatLng centerAthens = new LatLng(37.984368, 23.728198);
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final Intent intent = getIntent();
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        formatter = new DecimalFormat("#0.00000000");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        editText = (EditText) findViewById(R.id.startingPointLatitudeEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (startPoint != null) {
                    startPoint.position(new LatLng(Double.parseDouble(s.toString().replace(",", ".")), startPoint.getPosition().longitude));
                    mMap.clear();
                    if (endPoint != null)
                        mMap.addMarker(endPoint).showInfoWindow();
                    mMap.addMarker(startPoint).showInfoWindow();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText2 = (EditText) findViewById(R.id.startingPointLongitudeEditText);
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (startPoint != null) {
                    startPoint.position(new LatLng(startPoint.getPosition().latitude, Double.parseDouble(s.toString().replace(",", "."))));
                    mMap.clear();
                    if (endPoint != null)
                        mMap.addMarker(endPoint).showInfoWindow();
                    mMap.addMarker(startPoint).showInfoWindow();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText3 = (EditText) findViewById(R.id.endingPointLatitudeEditText);
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (endPoint != null) {
                    endPoint.position(new LatLng(Double.parseDouble(s.toString().replace(",", ".")), endPoint.getPosition().longitude));
                    mMap.clear();
                    if (startPoint != null)
                        mMap.addMarker(startPoint).showInfoWindow();
                    mMap.addMarker(endPoint).showInfoWindow();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText4 = (EditText) findViewById(R.id.endingPointLongitudeEditText);
        editText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (endPoint != null) {
                    endPoint.position(new LatLng(endPoint.getPosition().latitude, Double.parseDouble(s.toString().replace(",", "."))));
                    mMap.clear();
                    if (startPoint != null)
                        mMap.addMarker(startPoint).showInfoWindow();
                    mMap.addMarker(endPoint).showInfoWindow();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        linearLayout = (LinearLayout) findViewById(R.id.startingPointLinearLayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.endingPointLinearLayout);
        linearLayout3 = (LinearLayout) findViewById(R.id.getNewDirectionsLinearLayout);

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        GetDirections getDirections = new GetDirections(this, "terminate");
        getDirections.execute();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerAthens, 15));
    }

    public void confirmStartingPoint(View view) {
        linearLayout.setVisibility(View.GONE);
        linearLayout2.setVisibility(View.VISIBLE);
        isStartingPointConfirmed = true;
    }

    public void confirmEndingPoint(View view) {
        linearLayout2.setVisibility(View.GONE);
        linearLayout3.setVisibility(View.VISIBLE);
        isEndingointConfirmed = true;
        GetDirections getDirections = new GetDirections(this, null);
        getDirections.execute();
    }

    public void getNewDirections(View view) {
        linearLayout3.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
        mMap.clear();
        isStartingPointConfirmed = false;
        isEndingointConfirmed = false;
        mMap.addMarker(endPoint).showInfoWindow();
        mMap.addMarker(startPoint).showInfoWindow();
    }

    public void returnToStartingPoint(View view) {
        linearLayout2.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
        isEndingointConfirmed = false;
        isStartingPointConfirmed = false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (!isStartingPointConfirmed) {
            mMap.clear();
            editText.setText(String.valueOf(formatter.format(latLng.latitude)));
            editText2.setText(String.valueOf(formatter.format(latLng.longitude)));
            startPoint = new MarkerOptions().position(latLng).title("Starting Location");
            mMap.addMarker(startPoint).showInfoWindow();
        } else if (!isEndingointConfirmed) {
            mMap.clear();
            mMap.addMarker(startPoint).showInfoWindow();
            editText3.setText(String.valueOf(formatter.format(latLng.latitude)));
            editText4.setText(String.valueOf(formatter.format(latLng.longitude)));
            endPoint = new MarkerOptions().position(latLng).title("Ending Location");
            mMap.addMarker(endPoint).showInfoWindow();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (statusOfGPS) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mMap != null && mLastLocation != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLastLocation.getLatitude(),
                                mLastLocation.getLongitude()), 15));
        } else {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("You may enable gps to focus on your location.");
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class GetDirections extends AsyncTask {

        private FragmentActivity main;
        private String message;

        GetDirections(FragmentActivity main, String message) {
            this.main = main;
            this.message = message;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            Socket socketToMaster;
            ObjectOutputStream objectOutputStreamToMaster;
            ObjectInputStream objectInputStreamFromMaster;
            if (message == null) {
                try {
                    socketToMaster = new Socket(ip, Integer.parseInt(port));
                    objectOutputStreamToMaster = new ObjectOutputStream(socketToMaster.getOutputStream());
                    objectInputStreamFromMaster = new ObjectInputStream(socketToMaster.getInputStream());
                    objectOutputStreamToMaster.writeObject(startPoint.getPosition().latitude + " " +
                            startPoint.getPosition().longitude + " " + endPoint.getPosition().latitude + " " +
                            endPoint.getPosition().longitude);
                    objectOutputStreamToMaster.flush();
                    final List<Double> directionsResult = (List<Double>) objectInputStreamFromMaster.readObject();
                    objectOutputStreamToMaster.writeObject("exit");
                    objectOutputStreamToMaster.flush();
                    if (mMap != null) {
                        drawDirections(directionsResult);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("terminate")) {
                try {
                    socketToMaster = new Socket(ip, Integer.parseInt(port));
                    objectOutputStreamToMaster = new ObjectOutputStream(socketToMaster.getOutputStream());
                    objectInputStreamFromMaster = new ObjectInputStream(socketToMaster.getInputStream());
                    objectOutputStreamToMaster.writeObject("terminate");
                    objectOutputStreamToMaster.flush();
                    if (objectOutputStreamToMaster != null)
                        objectOutputStreamToMaster.close();
                    if (objectInputStreamFromMaster != null)
                        objectInputStreamFromMaster.close();
                    if (socketToMaster != null)
                        socketToMaster.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        private void drawDirections(List<Double> directionPoint) {
            PolylineOptions rectLine = new PolylineOptions().width(3).color(
                    Color.RED);

            for (int i = 0; i < directionPoint.size(); i+=2) {
                rectLine.add(new LatLng(directionPoint.get(i), directionPoint.get(i+1)));
            }
            final PolylineOptions rectLineFinal = rectLine;
            main.runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    mMap.addPolyline(rectLineFinal);
                }

            });
        }

    }


}
