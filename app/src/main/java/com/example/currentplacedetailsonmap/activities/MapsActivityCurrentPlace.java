package com.example.currentplacedetailsonmap.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.currentplacedetailsonmap.R;
import com.example.currentplacedetailsonmap.services.DataService;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;

/**
 * An activity that displays a map showing the place at the device's current location.
 */

public class MapsActivityCurrentPlace extends AppCompatActivity {

    // Side menu and toolbar customization.
    private Toolbar mToolbar;
    private Drawer mDrawer;

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private VoiceRecognition voiceRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Attaching the layout to the toolbar object
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        // Setup side menu
        setupNavigationMenu();

        // Load cached data into temporary storage

        try {
            DataService.getInstance().readSessionsFromDatabase("DATABASE");
            DataService.getInstance().readMapColorTypeFromDatabase("MAP_COLOR");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }

        View[] voiceViews = new View[1];
        voiceViews[0] = findViewById(R.id.voice_result);
        Intent intent = new Intent(getApplicationContext(), NavigationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        voiceRec = new VoiceRecognition(getApplicationContext(), voiceViews, "start new trip", intent);
        runRecognizerSetup();
    }

    public void setupNavigationMenu() {
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.city1)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        // Initialize mDrawer
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult)
                .withDrawerLayout(R.layout.material_drawer_fits_not)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName("Account Name").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withIdentifier(2).withName("Statistics"),
                        new SecondaryDrawerItem().withIdentifier(3).withName("Tutorial"),
                        new SecondaryDrawerItem().withIdentifier(4).withName("Settings")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        return true;
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int id = (int) drawerItem.getIdentifier();
                        switch (id) {
                            case 1:
                                Log.v("ID", id + " was chosen");
                                break;
                            case 2:
                                Log.v("ID", id + " was chosen");
                                loadStatsView();
                                break;
                            case 3:
                                Log.v("ID", id + " was chosen");
                                loadSlidesView();
                                break;
                            case 4:
                                Log.v("ID", id + " was chosen");
                                loadSettingsView();
                                break;
                            default:
                                break;
                        }

                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Eco Driving Inc."));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void loadStatsView() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    public void loadSlidesView() {
        Intent intent = new Intent(this, WelcomeSlidesActivity.class);
        intent.putExtra("SHOW_ONCE_MORE", "Show");

        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.v("LOAD", "Welcome slides activity was started");
            startActivity(intent);
        }
    }

    public void loadSettingsView() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startSession(View view) {
        Log.v("SESSION", "Start button was clicked");

        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);

        voiceRec.cancelVoiceDetection();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer();
                return true;
            case R.id.option_get_place:
                /*showCurrentPlace();*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**** Voice Recognition ****/

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MapsActivityCurrentPlace.this);
                    File assetDir = assets.syncAssets();
                    if (voiceRec != null) {
                        voiceRec.setupRecognizer(assetDir);
                    }
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.voice_result))
                            .setText("Failed to init recognizer " + result);
                } else {
                    if (voiceRec != null) {
                        voiceRec.switchSearch("wakeup"); //Speaking to wake up the recognizer
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                if (voiceRec != null) {
                    voiceRec.cancelVoiceDetection();
                }
            }
        }
    }
}
