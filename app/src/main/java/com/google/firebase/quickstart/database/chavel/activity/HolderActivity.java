package com.google.firebase.quickstart.database.chavel.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.ui.activity.BaseActivity;

public class HolderActivity extends BaseActivity {

    Toolbar toolbar;
    TextView toolbar_title;
    ImageView toolbar_icon;
    ImageView toolbar_back_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holder);

        // Begin the transaction
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.frame_holder, new NewRouteActivity());
//        ft.commit();

        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_icon = (ImageView) findViewById(R.id.toolbar_icon);
        toolbar_back_icon = (ImageView) findViewById(R.id.back_icon);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setDisplayUseLogoEnabled(true);

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        toolbar_back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        if(toolbar != null) {
//            //setSupportActionBar(toolbar);
//            if(getIntent().getExtras().getInt("index") == 1) {
//                toolbar_title.setText("My Route (24)");
//                toolbar_icon.setImageDrawable(getDrawable(R.drawable.ic_tab_my_route));
//                //getSupportActionBar().setLogo(R.drawable.ic_tab_my_route);
//                //getSupportActionBar().setTitle("My Route");
//            } else {
//                toolbar_title.setText("Favorite (5)");
//                toolbar_icon.setImageDrawable(getDrawable(R.drawable.ic_tab_my_favourite));
//               // getSupportActionBar().setLogo(R.drawable.ic_tab_my_favourite);
//                //getSupportActionBar().setTitle("Favorite");
//            }
//
//        }
    }
}
