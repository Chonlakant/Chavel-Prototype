package com.google.firebase.quickstart.database.ui.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.constants.NetworkConstants;
import com.google.firebase.quickstart.database.controller.BaseInterface;
import com.google.firebase.quickstart.database.models.AddressModel;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.util.IsOnlineUtil;
import com.google.firebase.quickstart.database.util.PermissionUtils;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PostDetailActivity extends BaseActivity implements OnMapReadyCallback,RoutingListener,BaseInterface {

    public static final String EXTRA_POST_KEY = "post_key";

    private static final String TAG = "PostDetailActivity";
    private static final String REQUIRED = "Required";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private DatabaseReference mPostReference;
    private ValueEventListener mPostListener;
    private String mPostKey;


    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private String mLatitude,mLongitude;
    private boolean mPermissionDenied = false;

//    private LatLng camera = new LatLng(37.782437, -122.4281893);
//    private LatLng origin = new LatLng(37.7849569, -122.4068855);
//    private LatLng destination = new LatLng(37.7814432, -122.4460177);

    private LatLng camera = new LatLng(37.782437, -122.4281893);
    private LatLng loc2 = new LatLng(37.7849569, -122.4068855);
    private LatLng loc3 = new LatLng(37.7814432, -122.4460177);
    private LatLng loc4 = new LatLng(37.801326, -122.424520);

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};

    private void addSampleMarkers(GoogleMap googleMap) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 13));
        googleMap.addMarker(new MarkerOptions().position(loc2));
        googleMap.addMarker(new MarkerOptions().position(loc3));
        googleMap.addMarker(new MarkerOptions().position(loc4));

        CameraUpdate center = CameraUpdateFactory.newLatLng(camera);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //addSampleMarkers(googleMap);

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        if (IsOnlineUtil.Operations.isOnline(this)) {
            hideKeyboard();
            mAddressModel.fetchAddressFromServer();
        } else {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }

    }

    public void route(LatLng... points) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(points)
                .build();
        routing.execute();
    }

    private Map<String, AddressModel> mDealMap = new HashMap<>();
    public List<Marker> mMarkerList = new ArrayList<>();
    private List<AddressModel> myDealsList = new ArrayList<AddressModel>();
    private List<Polyline> polylines = new ArrayList<Polyline>();
    private List<LatLng> latLngsList = new ArrayList<LatLng>();

    @Override
    public void handleNetworkCall(Object object, int requestCode) {
        if (requestCode == NetworkConstants.ADDRESS_REQUEST) {
            if (object instanceof ArrayList) {
                myDealsList = new ArrayList<>();
                myDealsList = (ArrayList) object;

                for (int j = 0; j < myDealsList.size(); j++) {
                    LatLng newLatLngTemp = new LatLng(Double.parseDouble(myDealsList.get(j).getLatitude()), Double.parseDouble(myDealsList.get(j).getLongitude()));

                    MarkerOptions options = new MarkerOptions();
                    IconGenerator iconFactory = new IconGenerator(PostDetailActivity.this);
                    iconFactory.setRotation(0);
                    iconFactory.setBackground(null);

                    View view = View.inflate(PostDetailActivity.this, R.layout.map_marker_text, null);
                    TextView tvVendorTitle;
                    tvVendorTitle = (TextView) view.findViewById(R.id.tv_vendor_title);
                    tvVendorTitle.setText(myDealsList.get(j).getRating());
                    iconFactory.setContentView(view);

                    options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(myDealsList.get(j).getRating())));
                    options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
                    options.position(newLatLngTemp);
                    options.snippet(String.valueOf(j));

                    latLngsList.add(newLatLngTemp);

                    Marker mapMarker = mMap.addMarker(options);
                    mMarkerList.add(mapMarker);
                    mDealMap.put(myDealsList.get(j).getRating(), myDealsList.get(j));

                }

                LatLng[] markerArray = latLngsList.toArray(new LatLng[0]);
                //LatLng[] array = latLngsList.stream().toArray(LatLng[]::new);
                route(markerArray);

                if (myDealsList.size() > 0) {
                    LatLng latlngOne = new LatLng(Double.parseDouble(myDealsList.get(0).getLatitude()), Double.parseDouble(myDealsList.get(0).getLongitude()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngOne, 16));
                }

                //mAdapter.notifyDataSetChanged();


            }
        }
    }

    private TheAnimator animator = new TheAnimator();

    private final Handler mHandler = new Handler();
    private List<Marker> markers = new ArrayList<Marker>();
    private Marker selectedMarker;

    Handler handler = new Handler();
    Random random = new Random();
    Runnable runner = new Runnable() {
        @Override
        public void run() {
            // setHasOptionsMenu(true);
        }
    };


    int currentPt;
    GoogleMap.CancelableCallback MyCancelableCallback =
            new GoogleMap.CancelableCallback() {

                @Override
                public void onCancel() {
                    System.out.println("onCancelled called");
                }

                @Override
                public void onFinish() {


                    if (++currentPt < markers.size()) {

                        float targetBearing = bearingBetweenLatLngs(mMap.getCameraPosition().target, markers.get(currentPt).getPosition());

                        LatLng targetLatLng = markers.get(currentPt).getPosition();

                        System.out.println("currentPt  = " + currentPt);
                        System.out.println("size  = " + markers.size());
                        //Create a new CameraPosition
                        CameraPosition cameraPosition =
                                new CameraPosition.Builder()
                                        .target(targetLatLng)
                                        .tilt(currentPt < markers.size() - 1 ? 90 : 0)
                                        .bearing(targetBearing)
                                        .zoom(mMap.getCameraPosition().zoom)
                                        .build();


                        mMap.animateCamera(
                                CameraUpdateFactory.newCameraPosition(cameraPosition),
                                3000,
                                MyCancelableCallback);
                        System.out.println("Animate to: " + markers.get(currentPt).getPosition() + "\n" +
                                "Bearing: " + targetBearing);


                    } else {

                    }

                }

            };


    private FloatingActionButton fab;
    private AddressModel mAddressModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        fab = findViewById(R.id.fab_walk);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animator.startAnimation(false);
            }
        });

        // Initialize Views
        mAuthorView = findViewById(R.id.post_author);
        mTitleView = findViewById(R.id.post_title);
        mBodyView = findViewById(R.id.post_body);

        mAddressModel = new AddressModel(this,this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        //Toast.makeText(getApplicationContext(),"postKey"+mPostKey,Toast.LENGTH_LONG).show();
        Log.e("postKey",mPostKey);

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);

        handler.postDelayed(runner, random.nextInt(2000));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;

    @Override
    public void onStart() {
        super.onStart();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                // [START_EXCLUDE]
                mAuthorView.setText(post.author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }


    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int ii) {

        polylines = new ArrayList<>();

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

        for (LatLng pt : route.get(0).getPoints()) {
            MarkerOptions options = new MarkerOptions();
            options.position(pt);

            Marker marker = mMap.addMarker(options);
            markers.add(marker);
            marker.setVisible(false);
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        for(Marker marker : mMarkerList) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);

        //animator.startAnimation(false);

    }

    @Override
    public void onRoutingCancelled() {

    }

    public class TheAnimator implements Runnable {

        private static final int ANIMATE_SPEEED = 1500;
        private static final int ANIMATE_SPEEED_TURN = 1000;
        private static final int BEARING_OFFSET = 20;

        private final Interpolator interpolator = new LinearInterpolator();

        int currentIndex = 0;

        float tilt = 90;
        float zoom = 15.5f;
        boolean upward = true;

        long start = SystemClock.uptimeMillis();

        LatLng endLatLng = null;
        LatLng beginLatLng = null;

        boolean showPolyline = false;

        private Marker trackingMarker;

        public void reset() {
            resetMarkers();
            start = SystemClock.uptimeMillis();
            currentIndex = 0;
            endLatLng = getEndLatLng();
            beginLatLng = getBeginLatLng();

        }

        public void stop() {
            trackingMarker.remove();
            mHandler.removeCallbacks(animator);

        }

        public void initialize(boolean showPolyLine) {
            reset();
            this.showPolyline = showPolyLine;

            highLightMarker(0);

            if (showPolyLine) {
                polyLine = initializePolyLine();
            }

            // We first need to put the camera in the correct position for the first run (we need 2 markers for this).....
            LatLng markerPos = markers.get(0).getPosition();
            LatLng secondPos = markers.get(1).getPosition();

            setupCameraPositionForMovement(markerPos, secondPos);

        }

        private void setupCameraPositionForMovement(LatLng markerPos,
                                                    LatLng secondPos) {

            float bearing = bearingBetweenLatLngs(markerPos, secondPos);


            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.car_small);

            trackingMarker = mMap.addMarker(new MarkerOptions().position(markerPos)
                    .icon(icon)
                    .title("Car")
                    .snippet("Yo"));

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(markerPos)
                            .bearing(bearing + BEARING_OFFSET)
                            .tilt(90)
                            .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16)
                            .build();

            mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPosition),
                    ANIMATE_SPEEED_TURN,
                    new GoogleMap.CancelableCallback() {

                        @Override
                        public void onFinish() {
                            System.out.println("finished camera");
                            animator.reset();
                            Handler handler = new Handler();
                            handler.post(animator);
                        }

                        @Override
                        public void onCancel() {
                            System.out.println("cancelling camera");
                        }
                    }
            );
        }

        private Polyline polyLine;
        private PolylineOptions rectOptions = new PolylineOptions();


        private Polyline initializePolyLine() {
            //polyLinePoints = new ArrayList<LatLng>();
            rectOptions.add(markers.get(0).getPosition());
            return mMap.addPolyline(rectOptions);
        }

        /**
         * Add the marker to the polyline.
         */
        private void updatePolyLine(LatLng latLng) {
            List<LatLng> points = polyLine.getPoints();
            points.add(latLng);
            polyLine.setPoints(points);
        }


        public void stopAnimation() {
            animator.stop();
        }

        public void startAnimation(boolean showPolyLine) {
            if (markers.size() > 1) {
                animator.initialize(showPolyLine);
            }
        }


        @Override
        public void run() {

            long elapsed = SystemClock.uptimeMillis() - start;
            double t = interpolator.getInterpolation((float) elapsed / ANIMATE_SPEEED);


            double lat = t * endLatLng.latitude + (1 - t) * beginLatLng.latitude;
            double lng = t * endLatLng.longitude + (1 - t) * beginLatLng.longitude;
            LatLng newPosition = new LatLng(lat, lng);

            trackingMarker.setPosition(newPosition);

            if (showPolyline) {
                updatePolyLine(newPosition);
            }


            if (t < 1) {
                mHandler.postDelayed(this, 16);
            } else {

                System.out.println("Move to next marker.... current = " + currentIndex + " and size = " + markers.size());
                // imagine 5 elements -  0|1|2|3|4 currentindex must be smaller than 4
                if (currentIndex < markers.size() - 2) {

                    currentIndex++;

                    endLatLng = getEndLatLng();
                    beginLatLng = getBeginLatLng();


                    start = SystemClock.uptimeMillis();

                    LatLng begin = getBeginLatLng();
                    LatLng end = getEndLatLng();

                    float bearingL = bearingBetweenLatLngs(begin, end);

                    highLightMarker(currentIndex);

                    CameraPosition cameraPosition =
                            new CameraPosition.Builder()
                                    .target(end) // changed this...
                                    .bearing(bearingL + BEARING_OFFSET)
                                    .tilt(tilt)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();


                    mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            ANIMATE_SPEEED_TURN,
                            null
                    );

                    start = SystemClock.uptimeMillis();
                    mHandler.postDelayed(animator, 16);

                } else {
                    currentIndex++;
                    highLightMarker(currentIndex);
                    //    stopAnimation();
                }

            }
        }


        private LatLng getEndLatLng() {
            return markers.get(currentIndex + 1).getPosition();
        }

        private LatLng getBeginLatLng() {
            return markers.get(currentIndex).getPosition();
        }

        private void adjustCameraPosition() {

            if (upward) {

                if (tilt < 90) {
                    tilt++;
                    zoom -= 0.01f;
                } else {
                    upward = false;
                }

            } else {
                if (tilt > 0) {
                    tilt--;
                    zoom += 0.01f;
                } else {
                    upward = true;
                }
            }
        }
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    public void toggleStyle() {
        if (GoogleMap.MAP_TYPE_NORMAL == mMap.getMapType()) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }


    /**
     * Adds a marker to the map.
     */
    public void addMarkerToMap(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng)
                .title("title")

                .snippet("snippet"));
        markers.add(marker);

    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        mMap.clear();
        markers.clear();
    }

    /**
     * Remove the currently selected marker.
     */
    public void removeSelectedMarker() {
        this.markers.remove(this.selectedMarker);
        this.selectedMarker.remove();
    }

    /**
     * Highlight the marker by index.
     */
    private void highLightMarker(int index) {
        highLightMarker(markers.get(index));
    }

    /**
     * Highlight the marker by marker.
     */
    private void highLightMarker(Marker marker) {

		/*
        for (Marker foundMarker : this.markers) {
			if (!foundMarker.equals(marker)) {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			} else {
				foundMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				foundMarker.showInfoWindow();
			}
		}
		*/
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        //  marker.showInfoWindow();
        //marker.remove();
        //Utils.bounceMarker(googleMap, marker);

        this.selectedMarker = marker;
    }

    private void resetMarkers() {
        for (Marker marker : this.markers) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }
}
