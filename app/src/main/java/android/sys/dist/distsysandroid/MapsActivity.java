package android.sys.dist.distsysandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
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
    private boolean isStartingPointConfirmed = false,
            isEndingointConfirmed = false;
    private NumberFormat formatter;
    private EditText editText3;
    private EditText editText4;
    private String ip, port;
    private LatLng centerAthens = new LatLng(37.984368, 23.728198);

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
        editText = (EditText) findViewById(R.id.startingPointLatitudeEditText);
        editText2 = (EditText) findViewById(R.id.startingPointLongitudeEditText);
        editText3 = (EditText) findViewById(R.id.endingPointLatitudeEditText);
        editText4 = (EditText) findViewById(R.id.endingPointLongitudeEditText);
        linearLayout = (LinearLayout) findViewById(R.id.startingPointLinearLayout);
        linearLayout2 = (LinearLayout) findViewById(R.id.endingPointLinearLayout);
        linearLayout3 = (LinearLayout) findViewById(R.id.getNewDirectionsLinearLayout);

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
            final PolylineOptions rectLineFinal = rectLine;
            main.runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    mMap.addPolyline(rectLineFinal);
                }

            });
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
