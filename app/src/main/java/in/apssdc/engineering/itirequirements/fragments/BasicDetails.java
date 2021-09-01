package in.apssdc.engineering.itirequirements.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.helper.ConnectionCheck;
import in.apssdc.engineering.itirequirements.model.BasicDetailsModel;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class BasicDetails extends Fragment {

    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences sharedPreferences;
    String selected_dis, selected_college;


    EditText itiname_et, itiLoc_et, itiAddr_et, itiPrinci_et, totalCampusArea_et, totalCoveredArea_et, totalOpenArea_et,
            noofCourses_et, noofTwoYearCourses_et, noofOneYearCourses_et, totalStaff_et, noofTeachingStaff_regular_et,
            noofTeachingStaff_contract_et, noofNonTeachingStaff_et;

    String itiname_str, itiLoc_str, itiAddr_str, itiPrinci_str, totalCampusArea_str, totalCoveredArea_str, totalOpenArea_str,
            noofCourses_str, noofTwoYearCourses_str, noofOneYearCourses_str, totalStaff_str, noofTeachingStaff_regular_str,
            noofTeachingStaff_contract_str, noofNonTeachingStaff_str;

    Button submit;


    public BasicDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_basic_details, container, false);

        itiname_et = v.findViewById(R.id.iti_name);
        itiLoc_et = v.findViewById(R.id.location);
        itiAddr_et = v.findViewById(R.id.address);
        itiPrinci_et = v.findViewById(R.id.principal);
        totalCampusArea_et = v.findViewById(R.id.tta);
        totalCoveredArea_et = v.findViewById(R.id.tca);
        totalOpenArea_et = v.findViewById(R.id.toa);
        noofCourses_et = v.findViewById(R.id.noc);
        noofTwoYearCourses_et = v.findViewById(R.id.n2c);
        noofOneYearCourses_et = v.findViewById(R.id.n1c);
        totalStaff_et = v.findViewById(R.id.ns);
        noofTeachingStaff_regular_et = v.findViewById(R.id.nrts);
        noofTeachingStaff_contract_et = v.findViewById(R.id.nrtsc);
        noofNonTeachingStaff_et = v.findViewById(R.id.nnts);
        submit = v.findViewById(R.id.basic_details_submit_button_id);
        database=FirebaseDatabase.getInstance();
        myRef=database.getReference();
        sharedPreferences = getActivity().getSharedPreferences("signindetails", MODE_PRIVATE);
        if (sharedPreferences.contains("SELECTED_DISTRICT")) {
            selected_dis = sharedPreferences.getString("SELECTED_DISTRICT", null);
            selected_college = sharedPreferences.getString("SELECTED_COLLEGE", null);
            itiname_et.setText(selected_college);
        }
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itiname_str = itiname_et.getText().toString();
                itiLoc_str = itiLoc_et.getText().toString();
                itiAddr_str = itiAddr_et.getText().toString();
                itiPrinci_str = itiPrinci_et.getText().toString();
                totalCampusArea_str = totalCampusArea_et.getText().toString();
                totalCoveredArea_str = totalCoveredArea_et.getText().toString();
                totalOpenArea_str = totalOpenArea_et.getText().toString();
                noofCourses_str = noofCourses_et.getText().toString();
                noofTwoYearCourses_str = noofTwoYearCourses_et.getText().toString();
                noofOneYearCourses_str = noofOneYearCourses_et.getText().toString();
                totalStaff_str = totalStaff_et.getText().toString();
                noofTeachingStaff_regular_str = noofTeachingStaff_regular_et.getText().toString();
                noofTeachingStaff_contract_str = noofTeachingStaff_contract_et.getText().toString();
                noofNonTeachingStaff_str = noofNonTeachingStaff_et.getText().toString();

                if (itiname_str.equalsIgnoreCase("")) {
                    itiname_et.setError("Please enter ITI Name");
                } else if (itiLoc_str.equalsIgnoreCase("")) {
                    itiLoc_et.setError("Please enter Location");
                } else if (itiAddr_str.equalsIgnoreCase("")) {
                    itiAddr_et.setError("Please enter address");
                } else if (itiPrinci_str.equalsIgnoreCase("")) {
                    itiPrinci_et.setError("Please enter principal name");
                } else if (totalCampusArea_str.equalsIgnoreCase("")) {
                    totalCampusArea_et.setError("Please enter campus area");
                } else if (totalCoveredArea_str.equalsIgnoreCase("")) {
                    totalCoveredArea_et.setError("Please enter total covered area");
                } else if (totalOpenArea_str.equalsIgnoreCase("")) {
                    totalOpenArea_et.setError("Please enter total open area");
                } else if (noofCourses_str.equalsIgnoreCase("")) {
                    noofCourses_et.setError("Please enter courses");
                } else if (noofTwoYearCourses_str.equalsIgnoreCase("")) {
                    noofTwoYearCourses_et.setError("Please enter noof 2 year courses");
                } else if (noofOneYearCourses_str.equalsIgnoreCase("")) {
                    noofOneYearCourses_et.setError("Please enter noof 1 year courses");
                } else if (totalStaff_str.equalsIgnoreCase("")) {
                    totalStaff_et.setError("Please enter total staff");
                } else if (noofTeachingStaff_regular_str.equalsIgnoreCase("")) {
                    noofTeachingStaff_regular_et.setError("Please enter noof teaching staff - regular");
                } else if (noofTeachingStaff_contract_str.equalsIgnoreCase("")) {
                    noofTeachingStaff_contract_et.setError("Please enter noof teaching staff - contract");
                } else if (noofNonTeachingStaff_str.equalsIgnoreCase("")) {
                    noofNonTeachingStaff_et.setError("Please enter non teaching staff");
                } else {

                    if(ConnectionCheck.checkConnection(getContext()))
                    {
                        BasicDetailsModel basicDetailsModel = new BasicDetailsModel(itiname_str, itiLoc_str, itiAddr_str, itiPrinci_str,
                                totalCampusArea_str, totalCoveredArea_str, totalOpenArea_str, noofCourses_str, noofTwoYearCourses_str, noofOneYearCourses_str,
                                totalStaff_str, noofTeachingStaff_regular_str, noofTeachingStaff_contract_str, noofNonTeachingStaff_str,String.valueOf(new Date().getTime()));

                        myRef.child(selected_dis).child(selected_college).child("Basic Details").setValue(basicDetailsModel, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null)
                                {
                                    Toast.makeText(getActivity(), "Not Saved Successfully", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                                    itiname_et.setText("");
                                    itiLoc_et.setText("");
                                    itiAddr_et.setText("");
                                    itiPrinci_et.setText("");
                                    totalCampusArea_et.setText("");
                                    totalCoveredArea_et.setText("");
                                    totalOpenArea_et.setText("");
                                    noofCourses_et.setText("");
                                    noofTwoYearCourses_et.setText("");
                                    noofOneYearCourses_et.setText("");
                                    totalStaff_et.setText("");
                                    noofTeachingStaff_regular_et.setText("");
                                    noofTeachingStaff_contract_et.setText("");
                                    noofNonTeachingStaff_et.setText("");
                                }
                            }
                        });
                    }else {
                        ConnectionCheck.showConnectionDisabledAlert();
                    }
                }
            }
        });
        return v;
    }
}
