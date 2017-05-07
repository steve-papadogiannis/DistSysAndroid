package android.sys.dist.distsysandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private EditText editText, editText2;
    private LinearLayout linearLayout, linearLayout2, linearLayout3;
    private MarkerOptions startPoint, endPoint;
    private boolean isStartingPointConfirmed = false, isEndingointConfirmed = false;
    private NumberFormat formatter;
    private EditText editText3;
    private EditText editText4;
    private String ip, port;
    private Socket socketToMaster;
    private ObjectOutputStream objectOutputStreamToMaster;
    private ObjectInputStream objectInputStreamFromMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final Intent intent = getIntent();
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        formatter = new DecimalFormat("#0.00");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        editText = (EditText) findViewById(R.id.starting_location_latitude);
        editText2 = (EditText) findViewById(R.id.starting_location_longitude);
        editText3 = (EditText) findViewById(R.id.ending_location_latitude);
        editText4 = (EditText) findViewById(R.id.ending_location_longitude);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);
        linearLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);

    }

    @Override
    protected void onDestroy() {
        GetDirections getDirections = new GetDirections(this, "terminate");
        getDirections.execute();
        super.onDestroy();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
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
            startPoint = new MarkerOptions().position(latLng);
            mMap.addMarker(startPoint);
        } else if (!isEndingointConfirmed) {
            mMap.clear();
            mMap.addMarker(startPoint);
            editText3.setText(String.valueOf(formatter.format(latLng.latitude)));
            editText4.setText(String.valueOf(formatter.format(latLng.longitude)));
            endPoint = new MarkerOptions().position(latLng);
            mMap.addMarker(endPoint);
        }
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
            if (message == null) {
                try {
                    socketToMaster = new Socket(ip, Integer.parseInt(port));
                    objectOutputStreamToMaster = new ObjectOutputStream(socketToMaster.getOutputStream());
                    objectInputStreamFromMaster = new ObjectInputStream(socketToMaster.getInputStream());
                    objectOutputStreamToMaster.writeObject(startPoint.getPosition().latitude + " " +
                            startPoint.getPosition().longitude + " " + endPoint.getPosition().latitude + " " +
                            endPoint.getPosition().longitude);
                    objectOutputStreamToMaster.flush();
                    final DirectionsResult directionsResult = (DirectionsResult) objectInputStreamFromMaster.readObject();
                    objectOutputStreamToMaster.writeObject("exit");
                    objectOutputStreamToMaster.flush();
                    if (mMap != null) {
                        ArrayList<LatLng> directionPoint = getDirection(directionsResult);
                        drawDirections(directionPoint);
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

        private ArrayList<LatLng> getDirection(DirectionsResult directionsResult) {
            ArrayList<LatLng> listGeopoints = new ArrayList<>();
            for (DirectionsRoute route : directionsResult.routes) {
                for (DirectionsLeg leg : route.legs) {
                    listGeopoints.add(new LatLng(leg.startLocation.lat, leg.startLocation.lng));
                    for (DirectionsStep step : leg.steps) {
                        ArrayList<LatLng> arr = decodePoly(step.polyline.getEncodedPath());
                        for (int j = 0; j < arr.size(); j++) {
                            listGeopoints.add(new LatLng(arr.get(j).latitude, arr
                                    .get(j).longitude));
                        }
                    }
                    listGeopoints.add(new LatLng(leg.endLocation.lat, leg.endLocation.lng));
                }
            }
            return listGeopoints;
        }

        private void drawDirections(ArrayList<LatLng> directionPoint) {
            PolylineOptions rectLine = new PolylineOptions().width(3).color(
                    Color.RED);

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            main.runOnUiThread(() -> mMap.addPolyline(rectLine));
        }

        private ArrayList<LatLng> decodePoly(String polyline) {
            ArrayList<LatLng> poly = new ArrayList<>();
            int index = 0, len = polyline.length();
            int lat = 0, lng = 0;
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = polyline.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                shift = 0;
                result = 0;
                do {
                    b = polyline.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
                poly.add(position);
            }
            return poly;
        }

    }


}
