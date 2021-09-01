package in.apssdc.engineering.itirequirements.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import in.apssdc.engineering.itirequirements.fragments.BasicDetails;
import in.apssdc.engineering.itirequirements.fragments.CaptureImages;
import in.apssdc.engineering.itirequirements.fragments.CourseDetails;
import in.apssdc.engineering.itirequirements.fragments.FetchLocation;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.fragments.FinancialDetails;
import in.apssdc.engineering.itirequirements.fragments.HRDDetails;
import in.apssdc.engineering.itirequirements.fragments.Home;
import in.apssdc.engineering.itirequirements.fragments.InfrastructureDetails;
import in.apssdc.engineering.itirequirements.fragments.ManagementDetails;
import in.apssdc.engineering.itirequirements.fragments.SoundRecording;
import in.apssdc.engineering.itirequirements.map_measure.Map;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView u_name, u_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);
        CircleImageView civ = v.findViewById(R.id.profile_image);
        u_email = v.findViewById(R.id.email_tv_id);
        u_name = v.findViewById(R.id.name_tv_id);
        SharedPreferences spf = getSharedPreferences("signindetails", MODE_PRIVATE);
        if (spf.contains("user_name")) {
            u_name.setText("Welcome, " + spf.getString("user_name", null));
            u_email.setText(spf.getString("user_email", null));
            Glide.with(this).load(Uri.parse(spf.getString("user_photo", null))).into(civ);
        }

        Home home = new Home();
        getSupportFragmentManager().beginTransaction().replace(R.id.contentss, home).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.fetch_Location) {
            FetchLocation fragment = new FetchLocation();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, fragment).commit();
        } else if (id == R.id.captureImageScreen) {
            CaptureImages fragment2 = new CaptureImages();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, fragment2).commit();
        } else if (id == R.id.soundRecording) {
            SoundRecording fragment3 = new SoundRecording();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, fragment3).commit();
        } else if (id == R.id.mapMeasure) {
            Intent intent = new Intent(NavigationActivity.this, Map.class);
            startActivity(intent);
        } else if (id == R.id.basicDetailsNav) {
            BasicDetails basicDetails = new BasicDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, basicDetails).commit();
        } else if (id == R.id.managementDetailsNav) {
            ManagementDetails managementDetails = new ManagementDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, managementDetails).commit();
        } else if (id == R.id.courseDetailsNav) {
            CourseDetails courseDetails = new CourseDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, courseDetails).commit();
        } /*else if (id == R.id.infraDetailsNav) {
            InfrastructureDetails infrastructureDetails = new InfrastructureDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, infrastructureDetails).commit();
        } else if (id == R.id.financeDetailsNav) {
            FinancialDetails financialDetails = new FinancialDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, financialDetails).commit();
        } else if (id == R.id.HRDDetailsNav) {
            HRDDetails hrdDetails = new HRDDetails();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, hrdDetails).commit();*/
        /*}*/ else if (id == R.id.homeNav) {
            Home home = new Home();
            getSupportFragmentManager().beginTransaction().replace(R.id.contentss, home).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
