package in.apssdc.engineering.itirequirements.map_measure;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import in.apssdc.engineering.itirequirements.BuildConfig;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.map_measure.wrapper.API17Wrapper;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    enum MeasureType {
        DISTANCE, AREA, ELEVATION
    }

    // the map to draw to
    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;

    // the stacks - everytime the user touches the map, an entry is pushed
    private final Stack<LatLng> trace = new Stack<>();
    private final Stack<Polyline> lines = new Stack<>();
    private final Stack<Marker> points = new Stack<>();

    private Polygon areaOverlay;

    private Pair<Float, Float> altitude;
    private float distance; // in meters
    private MeasureType type; // the currently selected measure type
    private TextView valueTv; // the view displaying the distance/area & unit

    static boolean metric; // display in metric units

    private static BitmapDescriptor marker;

    private IInAppBillingService mService;
    private static boolean PRO_VERSION = false;
    static String ELEVATION_API_KEY;

    private DrawerListAdapter drawerListAdapert;
    private GoogleApiClient mGoogleApiClient;

    private ElevationView elevationView;

    private boolean navBarOnRight;
    private int drawerSize, statusbar, navBarHeight;

    // store last location callback in case we dont have location permission yet and need to execute it later
    private LocationCallback lastLocationCallback;

    // Constants
    private final static int COLOR_LINE = Color.argb(128, 0, 0, 0), COLOR_POINT =
            Color.argb(128, 255, 0, 0);
    private final static float LINE_WIDTH = 5f;
    final static NumberFormat formatter_two_dec = NumberFormat.getInstance(Locale.getDefault());
    private final static NumberFormat formatter_no_dec =
            NumberFormat.getInstance(Locale.getDefault());
    private final static int REQUEST_LOCATION_PERMISSION = 0;

    final static String SKU = "de.j4velin.mapsmeasure.billing.pro";

    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                if (ownedItems.getInt("RESPONSE_CODE") == 0) {
                    PRO_VERSION =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST") != null &&
                                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                                            .contains(SKU);
                    getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                            .putBoolean("pro", PRO_VERSION).commit();
                }
            } catch (RemoteException e) {
                Toast.makeText(Map.this, e.getClass().getName() + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    };

    public GoogleMap getMap() {
        return mMap;
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) mDrawerLayout.closeDrawers();
    }

    /**
     * Get the formatted string for the valueTextView.
     * <p/>
     * Depending on whether 'showArea' is set, the returned string shows the
     * distance of the trace or the area between them. If 'showArea' is set,
     * this call might be expensive as the area is computed here and not cached.
     *
     * @return the formatted text for the valueTextView
     */
    private String getFormattedString() {
        if (type == MeasureType.DISTANCE) {
            elevationView.setVisibility(View.GONE);
            if (metric) {
                if (distance > 1000) return formatter_two_dec.format(distance / 1000) + " km";
                else return formatter_two_dec.format(Math.max(0, distance)) + " m";
            } else {
                if (distance > 1609) return formatter_two_dec.format(distance / 1609.344f) + " mi";
                else if (distance > 30)
                    return formatter_two_dec.format(distance / 1609.344f) + " mi\n" +
                            formatter_two_dec.format(Math.max(0, distance / 0.3048f)) + " ft";
                else return formatter_two_dec.format(Math.max(0, distance / 0.3048f)) + " ft";
            }
        } else if (type == MeasureType.AREA) {
            elevationView.setVisibility(View.GONE);
            double area;
            if (areaOverlay != null) areaOverlay.remove();
            if (trace.size() >= 3) {
                area = SphericalUtil.computeArea(trace);
                areaOverlay = mMap.addPolygon(
                        new PolygonOptions().addAll(trace).strokeWidth(0).fillColor(COLOR_POINT));
            } else {
                area = 0;
            }
            if (metric) {
                if (area > 1000000)
                    return formatter_two_dec.format(Math.max(0, area / 1000000d)) + " kmÂ²";
                else return formatter_no_dec.format(Math.max(0, area)) + " mÂ²";
            } else {
                if (area >= 2589989)
                    return formatter_two_dec.format(Math.max(0, area / 2589988.110336d)) + " miÂ²";
                else return formatter_no_dec.format(Math.max(0, area / 0.09290304d)) + " ftÂ²";
            }
        } else if (type == MeasureType.ELEVATION) {
            if (altitude == null) {
                final Handler h = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            altitude = Util.updateElevationView(elevationView, trace, null);
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isFinishing()) return;
                                    if (altitude == null) {
                                        Dialogs.getElevationErrorDialog(Map.this).show();
                                        changeType(MeasureType.DISTANCE);
                                    } else {
                                        updateValueText();
                                        elevationView.invalidate();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isFinishing()) return;
                                    Dialogs.getElevationErrorDialog(Map.this).show();
                                }
                            });
                        }
                    }
                }).start();
                return "Loading...";
            } else {
                String re = metric ? formatter_two_dec.format(altitude.first) + " m\u2B06, " +
                        formatter_two_dec.format(-1 * altitude.second) + " m\u2B07" :
                        formatter_two_dec.format(altitude.first / 0.3048f) + " ft\u2B06" +
                                formatter_two_dec.format(-1 * altitude.second / 0.3048f) +
                                " ft\u2B07";
                if (!trace.isEmpty()) {
                    try {
                        float lastPoint = Util.lastElevation;
                        if (lastPoint > -Float.MAX_VALUE) {
                            re += "\n" + (metric ? formatter_two_dec.format(lastPoint) + " m" :
                                    formatter_two_dec.format(lastPoint / 0.3048f) + " ft");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                elevationView.setVisibility(trace.size() > 1 ? View.VISIBLE : View.GONE);
                altitude = null;
                return re;
            }
        } else {
            return "not yet supported";
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            metric = savedInstanceState.getBoolean("metric");
            @SuppressWarnings("unchecked")
            // Casting to Stack<LatLng> apparently results in
                    // "java.lang.ClassCastException: java.util.ArrayList cannot be cast to java.util.Stack"
                    // on some devices
                    List<LatLng> tmp = (List<LatLng>) savedInstanceState.getSerializable("trace");
            Iterator<LatLng> it = tmp.iterator();
            while (it.hasNext()) {
                addPoint(it.next());
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(savedInstanceState.getDouble("position-lat"),
                            savedInstanceState.getDouble("position-lon")),
                    savedInstanceState.getFloat("position-zoom")));
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("trace", trace);
        outState.putBoolean("metric", metric);
        if (mMap != null) { // might be null if there is an issue with Google
            // Play Services
            outState.putDouble("position-lon", mMap.getCameraPosition().target.longitude);
            outState.putDouble("position-lat", mMap.getCameraPosition().target.latitude);
            outState.putFloat("position-zoom", mMap.getCameraPosition().zoom);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Adds a new point, calculates the new distance and draws the point and a
     * line to it
     *
     * @param p the new point
     */
    void addPoint(final LatLng p) {
        if (!trace.isEmpty()) {
            lines.push(mMap.addPolyline(
                    new PolylineOptions().color(COLOR_LINE).width(LINE_WIDTH).add(trace.peek())
                            .add(p)));
            distance += SphericalUtil.computeDistanceBetween(p, trace.peek());
        }
        points.push(drawMarker(p));
        trace.push(p);
        updateValueText();
    }

    /**
     * Resets the map by removing all points, lines and setting the text to 0
     */
    void clear() {
        mMap.clear();
        trace.clear();
        lines.clear();
        points.clear();
        distance = 0;
        updateValueText();
    }

    /**
     * Removes the last added point, the line to it and updates the distance
     */
    private void removeLast() {
        if (trace.isEmpty()) return;
        points.pop().remove();
        LatLng remove = trace.pop();
        if (!trace.isEmpty())
            distance -= SphericalUtil.computeDistanceBetween(remove, trace.peek());
        if (!lines.isEmpty()) lines.pop().remove();
        updateValueText();
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 23 && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        try {
            super.onCreate(savedInstanceState);
        } catch (final BadParcelableException bpe) {
            if (BuildConfig.DEBUG) Logger.log(bpe);
        }
        init();
    }

    /**
     * Initializes everything
     */
    private void init() {
        setContentView(R.layout.activity_map);

        elevationView = (ElevationView) findViewById(R.id.elevationsview);

        formatter_no_dec.setMaximumFractionDigits(0);
        formatter_two_dec.setMaximumFractionDigits(2);

        final SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);

        ELEVATION_API_KEY =
                prefs.getString("elevation_api_key", "0000");

        // use metric a the default everywhere, except in the US
        metric = prefs.getBoolean("metric", !Locale.getDefault().equals(Locale.US));

        final View topCenterOverlay = findViewById(R.id.topCenterOverlay);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        final View menuButton = findViewById(R.id.menu);
        if (menuButton != null) {
            menuButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

            mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

                private boolean menuButtonVisible = true;

                @Override
                public void onDrawerStateChanged(int newState) {

                }

                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onDrawerSlide(final View drawerView, final float slideOffset) {
                    if (android.os.Build.VERSION.SDK_INT >=
                            android.os.Build.VERSION_CODES.HONEYCOMB)
                        topCenterOverlay.setAlpha(1 - slideOffset);
                    if (menuButtonVisible && menuButton != null && slideOffset > 0) {
                        menuButton.setVisibility(View.INVISIBLE);
                        menuButtonVisible = false;
                    }
                }

                @Override
                public void onDrawerOpened(final View drawerView) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
                        topCenterOverlay.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onDrawerClosed(final View drawerView) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
                        topCenterOverlay.setVisibility(View.VISIBLE);
                    if (menuButton != null) {
                        menuButton.setVisibility(View.VISIBLE);
                        menuButtonVisible = true;
                    }
                }
            });
        }

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMapAsync(this);

        valueTv = (TextView) findViewById(R.id.distance);
        updateValueText();
        valueTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (type == MeasureType.DISTANCE) {
                    changeType(MeasureType.AREA);
                }
                // only switch to elevation mode is an internet connection is
                // available and user has access to this feature
                else if (type == MeasureType.AREA && Util.checkInternetConnection(Map.this) &&
                        PRO_VERSION) {
                    changeType(MeasureType.ELEVATION);
                } else {
                    if (BuildConfig.DEBUG) Logger.log("internet connection available: " +
                            Util.checkInternetConnection(Map.this));
                    changeType(MeasureType.DISTANCE);
                }
            }
        });

        View delete = findViewById(R.id.delete);
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                removeLast();
            }
        });
        delete.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Map.this);
                builder.setMessage(getString(R.string.delete_all, trace.size()));
                builder.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clear();
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
                return true;
            }
        });


        // Drawer stuff
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerListAdapert = new DrawerListAdapter(this);
        drawerList.setAdapter(drawerListAdapert);
        drawerList.setDivider(null);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, int position,
                                    long id) {
                switch (position) {
                    case 0: // Search before Android 5.0
                        Dialogs.getSearchDialog(Map.this).show();
                        closeDrawer();
                        break;
                    case 2: // Units
                        Dialogs.getUnits(Map.this, distance, SphericalUtil.computeArea(trace))
                                .show();
                        closeDrawer();
                        break;
                    case 3: // distance
                        changeType(MeasureType.DISTANCE);
                        break;
                    case 4: // area
                        changeType(MeasureType.AREA);
                        break;
                    case 5: // elevation
                        if (PRO_VERSION) {
                            changeType(MeasureType.ELEVATION);
                        } else {
                            Dialogs.showElevationAccessDialog(Map.this, mService);
                        }
                        break;
                    case 7: // map
                        changeView(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 8: // satellite
                        changeView(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 9: // terrain
                        changeView(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 11: // save
                        Dialogs.getSaveNShare(Map.this, trace,valueTv.getText().toString()).show();
                        closeDrawer();
                        break;
                    case 12: // more apps
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://search?q=pub:j4velin"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } catch (ActivityNotFoundException anf) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "https://play.google.com/store/apps/developer?id=j4velin"))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        break;
                    case 13: // about
                        Dialogs.getAbout(Map.this).show();
                        closeDrawer();
                        break;
                    default:
                        break;
                }
            }
        });

        changeType(MeasureType.DISTANCE);

        // KitKat translucent decor enabled? -> Add some margin/padding to the
        // drawer
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            statusbar = Util.getStatusBarHeight(this);

            FrameLayout.LayoutParams lp =
                    (FrameLayout.LayoutParams) topCenterOverlay.getLayoutParams();
            lp.setMargins(0, statusbar + 10, 0, 0);
            topCenterOverlay.setLayoutParams(lp);

            // on most devices and in most orientations, the navigation bar
            // should be at the bottom and therefore reduces the available
            // display height
            navBarHeight = Util.getNavigationBarHeight(this);

            DisplayMetrics total, available;
            total = new DisplayMetrics();
            available = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(available);
            API17Wrapper.getRealMetrics(getWindowManager().getDefaultDisplay(), total);

            navBarOnRight = getResources().getConfiguration().orientation ==
                    android.content.res.Configuration.ORIENTATION_LANDSCAPE &&
                    (total.widthPixels - available.widthPixels > 0);

            FrameLayout.LayoutParams elevationParams =
                    (FrameLayout.LayoutParams) elevationView.getLayoutParams();

            drawerSize = mDrawerLayout == null ? Util.dpToPx(this, 200) : 0;

            if (navBarOnRight) {
                // in landscape on phones, the navigation bar might be at the
                // right side, reducing the available display width
                drawerList.setPadding(0, statusbar + 10, 0, 0);
                if (menuButton != null) menuButton.setPadding(0, 0, 0, 0);
                elevationParams.setMargins(drawerSize, 0, navBarHeight, 0);
            } else {
                drawerList.setPadding(0, statusbar + 10, 0, 0);
                drawerListAdapert.setMarginBottom(navBarHeight);
                if (menuButton != null) menuButton.setPadding(0, 0, 0, navBarHeight);
                elevationParams.setMargins(Math.max(drawerSize, Util.dpToPx(this, 25)), 0, 0,
                        navBarHeight);
            }
            elevationView.setLayoutParams(elevationParams);
        }

        PRO_VERSION |= prefs.getBoolean("pro", false);
        if (!PRO_VERSION) {
            bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND")
                    .setPackage("com.android.vending"), mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        marker = BitmapDescriptorFactory.fromResource(R.drawable.marker);

        changeView(getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("mapView", GoogleMap.MAP_TYPE_NORMAL));

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
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
        Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(l!=null)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(),l.getLongitude()),20.0f));

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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker click) {
                addPoint(click.getPosition());
                return true;
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                getCurrentLocation(new LocationCallback() {
                    @Override
                    public void gotLocation(final Location location) {
                        if (location != null) {
                            LatLng myLocation =
                                    new LatLng(location.getLatitude(), location.getLongitude());
                            double distance = SphericalUtil.computeDistanceBetween(myLocation,
                                    mMap.getCameraPosition().target);

                            // Only if the distance is less than 50cm we are on our location, add the marker
                            if (distance < 0.5) {
                                Toast.makeText(Map.this, R.string.marker_on_current_location,
                                        Toast.LENGTH_SHORT).show();
                                addPoint(myLocation);
                            } else {
                                if (BuildConfig.DEBUG)
                                    Logger.log("location accuracy too bad to add point");
                                moveCamera(myLocation);
                            }
                        }
                    }
                });
                return true;
            }
        });

        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng center) {
                addPoint(center);
            }
        });

        if (hasLocationPermission()) {
            //noinspection MissingPermission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                mMap.setMyLocationEnabled(true);
                return;
            }

        }

        // KitKat translucent decor enabled? -> Add some margin/padding to the map
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (navBarOnRight) {
                // in landscape on phones, the navigation bar might be at the
                // right side, reducing the available display width
                mMap.setPadding(drawerSize, statusbar, navBarHeight, 0);
            } else {
                mMap.setPadding(0, statusbar, 0, navBarHeight);
            }
        }

        // check if open with csv file
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            try {
                Util.loadFromFile(getIntent().getData(), this);
            } catch (IOException e) {
                if (BuildConfig.DEBUG) Logger.log(e);
                Toast.makeText(this, getString(R.string.error,
                        e.getClass().getSimpleName() + "\n" + e.getMessage()), Toast.LENGTH_LONG)
                        .show();
                e.printStackTrace();
            }
        } else {
            // dont move to current position if started with a csv file
            getCurrentLocation(new LocationCallback() {
                @Override
                public void gotLocation(final Location location) {
                    if (location != null && mMap.getCameraPosition().zoom <= 2) {
                        moveCamera(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            });
        }
    }

    /**
     * Tries to get the users current position
     *
     * @param callback the callback which should be called when we got a location
     */
    private void getCurrentLocation(final LocationCallback callback) {
        if (hasLocationPermission()) {
            if (callback == null) return;
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(final Bundle bundle) {
                            //noinspection ResourceType
                            if (ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Map.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                Location l = LocationServices.FusedLocationApi
                                        .getLastLocation(mGoogleApiClient);
                                mGoogleApiClient.disconnect();
                                callback.gotLocation(l);
                                return;
                            }

                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            if (BuildConfig.DEBUG) Logger.log("connection suspended: " + cause);
                        }
                    }).build();
            mGoogleApiClient.connect();
        } else { // no permission
            if (Build.VERSION.SDK_INT >= 23) {
                lastLocationCallback = callback;
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else if (BuildConfig.DEBUG) Logger.log("no permission and no way to request them");
        }
    }

    /**
     * Moves the map view to the given position
     *
     * @param pos the position to move to
     */
    public void moveCamera(final LatLng pos) {
        moveCamera(pos, 16f);
    }

    /**
     * Moves the map view to the given position
     *
     * @param pos  the position to move to
     * @param zoom the zoom to apply
     */
    private void moveCamera(final LatLng pos, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
    }

    /**
     * Change the "type" of measuring: Distance, Area or Altitude
     *
     * @param newType the type to change to
     */
    private void changeType(final MeasureType newType) {
        type = newType;
        drawerListAdapert.changeType(newType);
        updateValueText();
        if (mDrawerLayout != null) mDrawerLayout.closeDrawers();
        if (newType != MeasureType.AREA) {
            if (areaOverlay != null) areaOverlay.remove();
        }
    }

    /**
     * Change between normal map, satellite hybrid and terrain view
     *
     * @param newView the new view, should be one of GoogleMap.MAP_TYPE_NORMAL,
     *                GoogleMap.MAP_TYPE_HYBRID or GoogleMap.MAP_TYPE_TERRAIN
     */
    private void changeView(int newView) {
        if (mMap != null) mMap.setMapType(newView);
        drawerListAdapert.changeView(newView);
        if (mDrawerLayout != null) mDrawerLayout.closeDrawers();
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putInt("mapView", newView)
                .commit();
    }

    /**
     * Draws a marker at the given point.
     * <p/>
     * Should be called when the users touches the map and adds an entry to the
     * stacks
     *
     * @param center the point where the user clicked
     * @return the drawn Polygon
     */
    private Marker drawMarker(final LatLng center) {
        return mMap.addMarker(
                new MarkerOptions().position(center).flat(true).anchor(0.5f, 0.5f).icon(marker));
    }

    /**
     * Updates the valueTextView at the top of the screen
     */
    void updateValueText() {
        if (valueTv != null) valueTv.setText(getFormattedString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    getCurrentLocation(lastLocationCallback);
                    //noinspection ResourceType
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
                    mMap.setMyLocationEnabled(true);
                } else {
                    String savedLocation = getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .getString("lastLocation", null);
                    if (savedLocation != null && savedLocation.contains("#")) {
                        String[] data = savedLocation.split("#");
                        try {
                            if (data.length == 3) {
                                moveCamera(new LatLng(Double.parseDouble(data[0]),
                                        Double.parseDouble(data[1])), Float.parseFloat(data[2]));
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.printStackTrace();
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (mDrawerLayout == null) return true;
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) mDrawerLayout.closeDrawers();
        else mDrawerLayout.openDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMap != null) {
            CameraPosition lastPosition = mMap.getCameraPosition();
            if (lastPosition != null) {
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                        .putString("lastLocation",
                                lastPosition.target.latitude + "#" + lastPosition.target.longitude +
                                        "#" + lastPosition.zoom).commit();
            }
        }
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 42 && resultCode == RESULT_OK) {
            if (data.getIntExtra("RESPONSE_CODE", 0) == 0) {
                try {
                    JSONObject jo = new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA"));
                    PRO_VERSION = jo.getString("productId").equals(SKU) &&
                            jo.getString("developerPayload").equals(getPackageName());
                    getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                            .putBoolean("pro", PRO_VERSION).commit();
                    changeType(MeasureType.ELEVATION);
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) Logger.log(e);
                    Toast.makeText(this, e.getClass().getName() + ": " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean hasLocationPermission() {
        return PermissionChecker
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED && PermissionChecker
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PermissionChecker.PERMISSION_GRANTED;
    }
}
