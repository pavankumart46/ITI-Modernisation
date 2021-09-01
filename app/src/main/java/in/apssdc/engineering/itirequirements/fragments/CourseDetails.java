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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.helper.ConnectionCheck;
import in.apssdc.engineering.itirequirements.model.CourseDetailsModel;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CourseDetails extends Fragment {


    Spinner sp1;

    EditText noonIndustryPartners_et,placedStudentLocations_et,remedialStepsforPlacement_et,noofLoans_et,enterpreneurshipPromotingApproach_et;

    String noonIndustryPartners,placedStudentLocations,remedialStepsforPlacement,noofLoans,enterpreneurshipPromotingApproach,dedicatedPlacementOfficer;

    Button submit;

    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences sharedPreferences;
    String selected_dis, selected_college;


    public CourseDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_course_details, container, false);

        noonIndustryPartners_et=v.findViewById(R.id.noofIndustryPartners_id);
        placedStudentLocations_et=v.findViewById(R.id.studentPlacedLocations_id);
        remedialStepsforPlacement_et=v.findViewById(R.id.remedialSteps_id);
        noofLoans_et=v.findViewById(R.id.noofLoans_id);
        enterpreneurshipPromotingApproach_et=v.findViewById(R.id.promotingApproach_id);

        sp1=v.findViewById(R.id.isDedicatedPlacementOfficer);

        submit=v.findViewById(R.id.course_details_submit_button_id);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        sharedPreferences = getActivity().getSharedPreferences("signindetails", MODE_PRIVATE);
        if (sharedPreferences.contains("SELECTED_DISTRICT")) {
            selected_dis = sharedPreferences.getString("SELECTED_DISTRICT", null);
            selected_college = sharedPreferences.getString("SELECTED_COLLEGE", null);
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noonIndustryPartners=noonIndustryPartners_et.getText().toString();
                placedStudentLocations=placedStudentLocations_et.getText().toString();
                remedialStepsforPlacement=remedialStepsforPlacement_et.getText().toString();
                noofLoans=noofLoans_et.getText().toString();
                enterpreneurshipPromotingApproach=enterpreneurshipPromotingApproach_et.getText().toString();

               dedicatedPlacementOfficer=sp1.getSelectedItem().toString();

                if(dedicatedPlacementOfficer.equalsIgnoreCase("Select"))
                {
                    Toast.makeText(getActivity(), "Please choose one option", Toast.LENGTH_SHORT).show();
                }else if(noonIndustryPartners.equalsIgnoreCase(""))
                {
                    noonIndustryPartners_et.setError("Please fill this");
                }else if(placedStudentLocations.equalsIgnoreCase(""))
                {
                    placedStudentLocations_et.setError("Please fill this");
                }else if(remedialStepsforPlacement.equalsIgnoreCase(""))
                {
                    remedialStepsforPlacement_et.setError("Please fill this");
                }else if(noofLoans.equalsIgnoreCase(""))
                {
                    noofLoans_et.setError("Please fill this");
                }else if(enterpreneurshipPromotingApproach.equalsIgnoreCase(""))
                {
                    enterpreneurshipPromotingApproach_et.setError("Please fill this");
                }else {
                   if(ConnectionCheck.checkConnection(getContext()))
                   {
                       CourseDetailsModel courseDetailsModel=new CourseDetailsModel(noonIndustryPartners,placedStudentLocations,
                               remedialStepsforPlacement,noofLoans,enterpreneurshipPromotingApproach,dedicatedPlacementOfficer,String.valueOf(new Date().getTime()));
                       myRef.child(selected_dis).child(selected_college).child("Course Details").setValue(courseDetailsModel, new DatabaseReference.CompletionListener() {
                           @Override
                           public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                               if (databaseError != null) {
                                   Toast.makeText(getActivity(), "Not Saved Successfully", Toast.LENGTH_SHORT).show();
                               } else {
                                   Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                                   noofLoans_et.setText("");
                                   placedStudentLocations_et.setText("");
                                   remedialStepsforPlacement_et.setText("");
                                   noofLoans_et.setText("");
                                   enterpreneurshipPromotingApproach_et.setText("");
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
