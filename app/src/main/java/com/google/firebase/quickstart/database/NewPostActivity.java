package com.google.firebase.quickstart.database;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.adapter.PlaceAutocompleteAdapter;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;
import com.google.firebase.quickstart.database.util.PermissionUtils;
import com.google.firebase.quickstart.database.widget.TouchableWrapper;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends BaseActivity implements


        GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final LatLngBounds BOUNDS_GREATER_INDIA = new LatLngBounds(
            new LatLng(8.062148, 68.212642), new LatLng(37.372499, 96.513423));

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;
    private FloatingActionButton fabAdd;

    private GoogleMap mMap;
    private String mLatitude,mLongitude;
    private boolean mPermissionDenied = false;

    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;

    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Log.e("iamin","onMapReady");
                //mMap.setOnMyLocationButtonClickListener(this);
                //mMap.setOnCameraChangeListener(this);
                enableMyLocation();
            }
        });

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        mSubmitButton = findViewById(R.id.fab_submit_post);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        fabAdd= (FloatingActionButton) findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.googleplacesearch);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, R.layout.item_google_places_search,
                mGoogleApiClient, null, null);

        //TODO:In order to Restrict search to India uncomment this and comment the above line
        /*
        mAdapter = new PlaceAutocompleteAdapter(this, R.layout.item_google_places_search,
                mGoogleApiClient, BOUNDS_GREATER_INDIA, null);
         */
        mAutocompleteView.setAdapter(mAdapter);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        mAutocompleteView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mAutocompleteView.getRight() - mAutocompleteView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        mAutocompleteView.setText("");

                        return true;
                    }
                }
                if (event.getRawX() <= (mAutocompleteView.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {

                    Intent i = new Intent(NewPostActivity.this, MainActivity.class);
                    startActivity(i);

                    finish();
                    return true;
                }
                return false;
            }
        });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add:
                mLatitude=mMap.getCameraPosition().target.latitude+"";
                mLongitude=mMap.getCameraPosition().target.longitude+"";

                Toast.makeText(NewPostActivity.this, "Latitude:"+mLatitude +" Longitude:"+mLongitude,Toast.LENGTH_LONG).show();

                break;
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

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, body,mLatitude,mLongitude);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        mTitleField.setEnabled(enabled);
        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body,String lat,String lng) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body,lat,lng);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }



    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {

        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            hideKeyboard();
            mLatitude=String.valueOf(place.getLatLng().latitude);
            mLongitude=String.valueOf(place.getLatLng().longitude);
            LatLng newLatLngTemp = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            // LatLng centerLatLng=new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLngTemp, 15f));
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);


            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
