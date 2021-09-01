package in.apssdc.engineering.itirequirements.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.angmarch.views.NiceSpinner;

import java.util.Arrays;
import java.util.List;

import at.markushi.ui.CircleButton;
import in.apssdc.engineering.itirequirements.R;

public class SelectCollegeActivity extends AppCompatActivity {

    Spinner district_spinner, college_spinner;
    String selected_district = "",selected_college = "";
    SharedPreferences sharedPreferences;
    CircleButton button;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_college);
        district_spinner = findViewById(R.id.district_spinner);
        college_spinner = findViewById(R.id.college_spinner);
        button = findViewById(R.id.next_go);
        button.setVisibility(View.GONE);
        sharedPreferences = getSharedPreferences("signindetails",MODE_PRIVATE);
        final String districts_array[] = getResources().getStringArray(R.array.districts);
        college_spinner.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, districts_array);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        district_spinner.setAdapter(adapter);
        district_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                college_spinner.setVisibility(View.GONE);
                if(!parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[0]) && college_spinner.getVisibility()!=View.VISIBLE)
                {
                    String college_names[]={};
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[1]))
                        college_names = getResources().getStringArray(R.array.Srikakulam);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[2]))
                        college_names = getResources().getStringArray(R.array.Vizianagaram);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[3]))
                        college_names = getResources().getStringArray(R.array.Visakhapatnam);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[4]))
                        college_names = getResources().getStringArray(R.array.EastGodavari);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[5]))
                        college_names = getResources().getStringArray(R.array.WestGodavari);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[6]))
                        college_names = getResources().getStringArray(R.array.Krishna);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[7]))
                        college_names = getResources().getStringArray(R.array.Guntur);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[8]))
                        college_names = getResources().getStringArray(R.array.Prakasam);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[9]))
                        college_names = getResources().getStringArray(R.array.Nellore);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[10]))
                        college_names = getResources().getStringArray(R.array.Chittoor);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[11]))
                        college_names = getResources().getStringArray(R.array.Kadapa);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[12]))
                        college_names = getResources().getStringArray(R.array.Kurnool);
                    if(parent.getSelectedItem().toString().equalsIgnoreCase(districts_array[13]))
                        college_names = getResources().getStringArray(R.array.Anantapur);
                    ArrayAdapter<String> colleges_adapter = new ArrayAdapter<>(SelectCollegeActivity.this,android.R.layout.simple_spinner_item,college_names);
                    colleges_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    college_spinner.setAdapter(colleges_adapter);
                    college_spinner.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                }
                else
                {
                    college_spinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void goToNext(View view) 
    {
        selected_district = district_spinner.getSelectedItem().toString();
        selected_college = college_spinner.getSelectedItem().toString();
        if(!selected_college.equals("--Select College--"))
        {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putString("SELECTED_DISTRICT",selected_district);
            edit.putString("SELECTED_COLLEGE",selected_college);
            edit.apply();
            startActivity(new Intent(this,NavigationActivity.class));   
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("SORRY! Please Select District and College!")
                    .setMessage("You cannot Proceed without selecting the college name and district.")
                    .setIcon(R.drawable.ic_error)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        }
        

    }
}
