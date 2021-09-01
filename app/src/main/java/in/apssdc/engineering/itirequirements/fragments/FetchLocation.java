package in.apssdc.engineering.itirequirements.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import at.markushi.ui.CircleButton;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.helper.ConnectionCheck;

public class FetchLocation extends Fragment implements android.location.LocationListener {


    private static final int REQUEST_PERMISSION_CODE = 2000;
    LocationManager locationManager;
    TextView textView;
    double latitude;
    double longitude;
    Button get_loc_button;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.activity_fetch_location,container,false);
        textView = v.findViewById(R.id.location_coordinates);
        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        get_loc_button = v.findViewById(R.id.get_loc_button_id);
        if (checkPermission()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        } else {
            requestPermission();
        }

        get_loc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ConnectionCheck.checkConnection(getContext()))
                {
                    if (locationManager != null && checkPermission()) {
                        Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastKnownLocationGPS != null) {
                            textView.setText(lastKnownLocationGPS.getLatitude()+","+lastKnownLocationGPS.getLongitude());
                        } else {
                            Location loc =  locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                            textView.setText("Try Again Later please!");
                        }
                    }
                }
                else {
                    ConnectionCheck.showConnectionDisabledAlert();
                }
            }
        });

        return v;
    }


    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        /*textView.setText(latitude + "," + longitude);*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]
                {       Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
               }, REQUEST_PERMISSION_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int access_loc_re = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        int cam_re=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA);
        int write_to_ExternalStorage=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_storage=ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE);
        return access_loc_re == PackageManager.PERMISSION_GRANTED && cam_re==PackageManager.PERMISSION_GRANTED &&
                write_to_ExternalStorage==PackageManager.PERMISSION_GRANTED && read_external_storage==PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
        }

    }
}
