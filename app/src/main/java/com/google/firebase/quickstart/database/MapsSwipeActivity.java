package com.google.firebase.quickstart.database;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.quickstart.database.adapter.MapAdapter;
import com.google.firebase.quickstart.database.constants.NetworkConstants;
import com.google.firebase.quickstart.database.controller.BaseInterface;
import com.google.firebase.quickstart.database.models.AddressModel;
import com.google.maps.android.ui.IconGenerator;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by skyrreasure on 12/5/16.
 */
public class MapsSwipeActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        SeekBar.OnSeekBarChangeListener,
        OnMapReadyCallback,
        RoutingListener,
        GoogleMap.OnMapClickListener {

    private ProgressDialog progressDialog;
    private static boolean flag = true;

    Marker prevMarker;
    String prevVendorName;
    boolean doubleBackToExitPressedOnce = false;
    private Map<String, AddressModel> mDealMap = new HashMap<>();
    public List<Marker> mMarkerList = new ArrayList<>();
    private List<AddressModel> myDealsList = new ArrayList<AddressModel>();
    private List<Polyline> polylines = new ArrayList<Polyline>();
    private List<LatLng> latLngsList = new ArrayList<LatLng>();
    private LatLng[] latLngsArray = new LatLng[20];

    private GoogleMap mMap;

    private ViewPager mViewPager;
    private MapAdapter mAdapter;
    private AddressModel mAddressModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_swipe);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Custom Markers and Viewpager");

        polylines = new ArrayList<>();

        mMarkerList = new ArrayList<>();
        myDealsList = new ArrayList<AddressModel>();
        mDealMap = new HashMap<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        myDealsList = new ArrayList<>();

        mViewPager = (ViewPager) findViewById(R.id.vp_details);
        mViewPager.setPadding(16, 0, 16, 0);
        mViewPager.setClipToPadding(true);
        mViewPager.setPageMargin(8);

        mAdapter = new MapAdapter(getSupportFragmentManager(), myDealsList, this);
        mViewPager.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        mAddressModel = new AddressModel(this, new BaseInterface() {
            @Override
            public void handleNetworkCall(Object object, int requestCode) {
                if (requestCode == NetworkConstants.ADDRESS_REQUEST) {
                    if (object instanceof ArrayList) {
                        myDealsList = new ArrayList<>();
                        myDealsList = (ArrayList) object;

                        mAdapter = new MapAdapter(getSupportFragmentManager(), myDealsList, MapsSwipeActivity.this);
                        mViewPager.setAdapter(mAdapter);

                        for (int j = 0; j < myDealsList.size(); j++) {
                            LatLng newLatLngTemp = new LatLng(Double.parseDouble(myDealsList.get(j).getLatitude()), Double.parseDouble(myDealsList.get(j).getLongitude()));

                            MarkerOptions options = new MarkerOptions();
                            IconGenerator iconFactory = new IconGenerator(MapsSwipeActivity.this);
                            iconFactory.setRotation(0);
                            iconFactory.setBackground(null);

                            View view = View.inflate(MapsSwipeActivity.this, R.layout.map_marker_text, null);
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

                        LatLng[] array = latLngsList.toArray(new LatLng[0]);
                        //LatLng[] array = latLngsList.stream().toArray(LatLng[]::new);
                        route(array);

                        if (myDealsList.size() > 0) {
                            LatLng latlngOne = new LatLng(Double.parseDouble(myDealsList.get(0).getLatitude()), Double.parseDouble(myDealsList.get(0).getLongitude()));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlngOne, 16));
                        }

                        mAdapter.notifyDataSetChanged();


                    }
                }
            }
        });


        mViewPager.setOffscreenPageLimit(4);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (flag) {
                    flag = false;
                    final AddressModel temp = myDealsList.get(position);
                    LatLng newLatLng = new LatLng(Double.parseDouble(temp.getLatitude()), Double.parseDouble(temp.getLongitude()));
                    // map.animateCenterZoom(newLatLng, 15);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 13));
                    Marker marker = mMarkerList.get(position);


                    if (prevMarker != null) {
                        //Set prevMarker back to default color
                        IconGenerator iconFactory = new IconGenerator(MapsSwipeActivity.this);
                        iconFactory.setRotation(0);
                        iconFactory.setBackground(null);
                        View view = View.inflate(MapsSwipeActivity.this, R.layout.map_marker_text, null);
                        TextView tvVendorTitle;

                        tvVendorTitle = (TextView) view.findViewById(R.id.tv_vendor_title);
                        tvVendorTitle.setText(prevVendorName);
                        tvVendorTitle.setBackground(getResources().getDrawable(R.mipmap.map_pin_white));
                        tvVendorTitle.setTextColor(Color.parseColor("#0097a9"));


                        iconFactory.setContentView(view);
                        //prevVendorName = myDealsList.get(position).getmVendorName();
                        prevMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(temp.getRating())));

                    }

                    //leave Marker default color if re-click current Marker
                    if (!marker.equals(prevMarker)) {


                        IconGenerator iconFactory = new IconGenerator(MapsSwipeActivity.this);
                        iconFactory.setRotation(0);
                        iconFactory.setBackground(null);
                        View view = View.inflate(MapsSwipeActivity.this, R.layout.map_marker_text_active, null);
                        TextView tvVendorTitle;

                        tvVendorTitle = (TextView) view.findViewById(R.id.tv_vendor_title);
                        tvVendorTitle.setText(myDealsList.get(position).getRating() + "");

                        iconFactory.setContentView(view);
                        //
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()));
                        prevMarker = marker;
                        prevVendorName = myDealsList.get(position).getRating() + "";

                    }
                    prevMarker = marker;
                    prevVendorName = myDealsList.get(position).getRating() + "";
                    flag = true;
                } else {
                    Log.i("", "" + mMarkerList);
                    Log.i("", "" + position);
                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void route(LatLng... points) {

        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Fetching route of ("+points.length+") pins information.", true);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.WALKING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(points)
                .build();
        routing.execute();
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mAddressModel.fetchAddressFromServer();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        if (flag) {
            flag = false;
            mViewPager.setVisibility(View.VISIBLE);
            String aid = marker.getId().substring(1, marker.getId().length());
            //final AddressModel temp = myDealsList.get(Integer.parseInt(marker.getSnippet()));
            // mViewPager.setCurrentItem(Integer.parseInt(marker.getSnippet()));
            final AddressModel temp = myDealsList.get(Integer.parseInt(aid));
            mViewPager.setCurrentItem(Integer.parseInt(aid));

            if (prevMarker != null) {
                //Set prevMarker back to default color
                IconGenerator iconFactory = new IconGenerator(MapsSwipeActivity.this);
                iconFactory.setRotation(0);
                iconFactory.setBackground(null);
                View view = View.inflate(MapsSwipeActivity.this, R.layout.map_marker_text, null);
                TextView tvVendorTitle;

                tvVendorTitle = (TextView) view.findViewById(R.id.tv_vendor_title);
                tvVendorTitle.setText(prevVendorName);
                tvVendorTitle.setBackground(getResources().getDrawable(R.mipmap.map_pin_white));
                tvVendorTitle.setTextColor(Color.parseColor("#0097a9"));

                iconFactory.setContentView(view);

                prevMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(temp.getRating())));

            }

            //leave Marker default color if re-click current Marker
            if (!marker.equals(prevMarker)) {

                IconGenerator iconFactory = new IconGenerator(MapsSwipeActivity.this);
                iconFactory.setRotation(0);
                iconFactory.setBackground(null);
                View view = View.inflate(MapsSwipeActivity.this, R.layout.map_marker_text, null);
                TextView tvVendorTitle;

                tvVendorTitle = (TextView) view.findViewById(R.id.tv_vendor_title);
                tvVendorTitle.setText(myDealsList.get(Integer.parseInt(marker.getSnippet())).getRating());


                tvVendorTitle.setBackground(getResources().getDrawable(R.mipmap.map_pin_green));
                tvVendorTitle.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.star_icon_white), null, null, null);
                tvVendorTitle.setTextColor(Color.parseColor("#FFFFFF"));
                iconFactory.setContentView(view);
                //
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(temp.getRating())));
                prevMarker = marker;
                prevVendorName = myDealsList.get(Integer.parseInt(marker.getSnippet())).getRating();

            }
            prevMarker = marker;
            prevVendorName = myDealsList.get(Integer.parseInt(marker.getSnippet())).getRating();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            flag = true;
        }


        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mViewPager.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if(id == R.id.action_home){
            Intent i=new Intent(MapsSwipeActivity.this,MapsSwipeActivity.class);
            startActivity(i);
            return true;
        }*/
//        if (id == R.id.action_search) {
//            Intent i=new Intent(MapsSwipeActivity.this,SearchPlaceOnMapActivity.class);
//            startActivity(i);
//            return true;
//        }
//        if(id == R.id.action_show_direction){
//            Intent i=new Intent(MapsSwipeActivity.this,ShowDirectionActivity.class);
//            startActivity(i);
//            return true;
//        }
//        if(id== R.id.action_moving_marker){
//            Intent i=new Intent(MapsSwipeActivity.this,MovingMarkerActivity.class);
//            startActivity(i);
//            return true;
//
//        }
//        if(id == R.id.action_list_view){
//            Intent i=new Intent(MapsSwipeActivity.this,ListViewActivity.class);
//            startActivity(i);
//            return true;
//
//        }
//        if(id==R.id.action_lite_mode){
//            Intent i=new Intent(MapsSwipeActivity.this,RecyclerViewLiteModeMapActivity.class);
//            startActivity(i);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        progressDialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    private static final int[] COLORS = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};


    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int ii) {

        progressDialog.dismiss();

        CameraUpdate center = CameraUpdateFactory.newLatLng(routes.get(0).getLatLgnBounds().getCenter());
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

        mMap.moveCamera(center);
        mMap.moveCamera(zoom);


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        Log.e("routes.size()",routes.size() + "");

        //add route(s) to the map.
        for (int i = 0; i < routes.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
//            @SuppressLint({"NewApi", "LocalSuppress"})
//            int randomNum = ThreadLocalRandom.current().nextInt(0, routes.size() + 1);
            //colorIndex = randomNum;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(routes.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

//the include method will calculate the min and max bound.
        for(Marker marker : mMarkerList) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
