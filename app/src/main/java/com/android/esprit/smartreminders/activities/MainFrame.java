package com.android.esprit.smartreminders.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.esprit.smartreminders.Entities.User;
import com.android.esprit.smartreminders.Fragments.EventsndMettingsFragment;
import com.android.esprit.smartreminders.Fragments.FragmentChild;
import com.android.esprit.smartreminders.Fragments.PlansFragment;
import com.android.esprit.smartreminders.Fragments.ProfileFragment;
import com.android.esprit.smartreminders.Fragments.ScheduleFragment;
import com.android.esprit.smartreminders.Fragments.SettingsFragment;
import com.android.esprit.smartreminders.Fragments.TasksFragment;
import com.android.esprit.smartreminders.Fragments.ZonesFragment;
import com.android.esprit.smartreminders.R;
import com.android.esprit.smartreminders.appcommons.activity.AppCommonsActivity;
import com.android.esprit.smartreminders.sessions.Session;

public class MainFrame extends AppCommonsActivity implements NavigationView.OnNavigationItemSelectedListener {

    private User sessionUser;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FrameLayout fragmentContainer;

    private Object editedObject;

    public Object getEditedObject() {
        return editedObject;
    }

    public void setEditedObject(Object editedObject) {
        this.editedObject = editedObject;
    }

    private Class<? extends FragmentChild> visibleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        clearBackStack();
        setContentView(R.layout.activity_main_frame);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        fragmentContainer=findViewById(R.id.fragment_container);
        this.setTitle(R.string.app_name);
        this.switchFragments(R.id.fragment_container, new ScheduleFragment());
        new Handler().postDelayed(this::init_Layout_data, 500);

    }


    private void init_Layout_data() {

        sessionUser = Session.getSession(this).getSessionUser();
        System.out.println(sessionUser);
        TextView f = ((TextView) findViewById(R.id.tvusername));
        f.setText(sessionUser.getEmail());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            fragmentContainer.removeAllViews();
            super.onBackPressed();

          //  updateHihlightedDrawer();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_frame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            goToSettingsFragment();
            this.visibleFragment = SettingsFragment.class;
            return true;
        } else if (id == R.id.action_profile) {
            goToProfileFragment();
            this.visibleFragment = ProfileFragment.class;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            goToScheduleFragment();
            visibleFragment = ScheduleFragment.class;
        } else if (id == R.id.nav_daily) {
            goToPlansFragment();
            visibleFragment = PlansFragment.class;
        } else if (id == R.id.nav_tasks) {
            goToTasksFragment();
            visibleFragment = TasksFragment.class;
        } else if (id == R.id.nav_events) {
            goToEventndMeetingsFragment();
            visibleFragment = EventsndMettingsFragment.class;
        } else if (id == R.id.nav_zones) {
            goToZonesFragment();
            visibleFragment = ZonesFragment.class;
        } else if (id == R.id.action_logout) {

            logOut();
        } else if (id == R.id.nav_settings) {
            goToSettingsFragment();
            visibleFragment = SettingsFragment.class;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void goToScheduleFragment() {
        if (!ScheduleFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new ScheduleFragment());

    }

    private void goToPlansFragment() {
        if (!PlansFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new PlansFragment());

    }

    private void goToTasksFragment() {
        if (!TasksFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new TasksFragment());

    }

    private void goToEventndMeetingsFragment() {
        if (!EventsndMettingsFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new EventsndMettingsFragment());

    }

    private void goToSettingsFragment() {
        if (!SettingsFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new SettingsFragment());

    }

    private void goToProfileFragment() {
        if (!ProfileFragment.class.equals(visibleFragment))
            switchFragmentsAddToBackStack(R.id.fragment_container, new ProfileFragment());

    }

    public void goToUnStackedFragment(FragmentChild Child) {
        if (!Child.getClass().equals(visibleFragment)) {
            visibleFragment = Child.getClass();
            switchFragments(R.id.fragment_container,Child);
        }

    }


    public void goToZonesFragment() {
        if (!ZonesFragment.class.equals(visibleFragment))
            switchFragments(R.id.fragment_container, new ZonesFragment());
        Log.d("Fragments Behavour", "goToZonesFragment: fragment changed !");
    }

    public void logOut() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setIcon(R.drawable.ic_exit_to_app_black_24dp)
                .setPositiveButton("Logout", (dialog1, which) -> {
                    SharedPreferences sharedPref = getSharedPreferences("Myprefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear().apply();
                    startActivity(new Intent(MainFrame.this, Login.class));
                })
                .setNegativeButton("I changed my mind", (dialog1, which) -> {
                    Toast.makeText(this, "Logout Canceled !", Toast.LENGTH_SHORT).show();
                })
                .setTitle("Logout").setMessage("Are you sure you want to Logout ? All our services will cease to function !  ").create();
        dialog.show();

    }

    private void updateHihlightedDrawer() {

    }


}
