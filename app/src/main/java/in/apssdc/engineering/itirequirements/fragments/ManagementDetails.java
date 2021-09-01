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
import in.apssdc.engineering.itirequirements.model.ManagementDetailsModel;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManagementDetails extends Fragment {

    Spinner sp1,sp2,sp3;

    EditText yearOfEst_et, capital_exp_et, unutilized_fund_et, challenges_et;

    String yearOfEstablishment, capitalExpenditure, unutilizedFund, challengesInManagement, isManagementCommittee, presentCapitalExpenditure, presentOperatingExpenditure;

    Button submit;

    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences sharedPreferences;
    String selected_dis, selected_college;


    public ManagementDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_management_details, container, false);

        sp1=v.findViewById(R.id.isManagementCommittee_id);
        sp2=v.findViewById(R.id.presentCapitalExp_id);
        sp3=v.findViewById(R.id.presentOperatingExp_id);

        yearOfEst_et = v.findViewById(R.id.yoe);
        capital_exp_et = v.findViewById(R.id.cee);
        unutilized_fund_et = v.findViewById(R.id.unutilized_fund_id);
        challenges_et = v.findViewById(R.id.challenges);

        submit = v.findViewById(R.id.management_details_submit_button_id);

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

                isManagementCommittee=sp1.getSelectedItem().toString();
                presentCapitalExpenditure=sp2.getSelectedItem().toString();
                presentOperatingExpenditure=sp3.getSelectedItem().toString();

                yearOfEstablishment = yearOfEst_et.getText().toString();
                capitalExpenditure = capital_exp_et.getText().toString();
                unutilizedFund = unutilized_fund_et.getText().toString();
                challengesInManagement = challenges_et.getText().toString();

                if (isManagementCommittee.equalsIgnoreCase("Select")) {
                    Toast.makeText(getActivity(), "Please select yes or no", Toast.LENGTH_SHORT).show();
                } else if (presentCapitalExpenditure.equalsIgnoreCase("Select")) {
                    Toast.makeText(getActivity(), "Please choose option", Toast.LENGTH_SHORT).show();
                } else if (presentOperatingExpenditure.equalsIgnoreCase("Select")) {
                    Toast.makeText(getActivity(), "Please choose option", Toast.LENGTH_SHORT).show();
                } else if (yearOfEstablishment.equalsIgnoreCase("")) {
                    yearOfEst_et.setError("Please fill this");
                } else if (capitalExpenditure.equalsIgnoreCase("")) {
                    capital_exp_et.setError("Please fill this");
                } else if (unutilizedFund.equalsIgnoreCase("")) {
                    unutilized_fund_et.setError("Please fill this");
                } else if (challengesInManagement.equalsIgnoreCase("")) {
                    challenges_et.setError("Please fill this");
                } else {
                    if(ConnectionCheck.checkConnection(getContext()))
                    {
                        ManagementDetailsModel managementDetailsModel = new ManagementDetailsModel(yearOfEstablishment, capitalExpenditure, unutilizedFund, challengesInManagement, isManagementCommittee, presentCapitalExpenditure, presentOperatingExpenditure,String.valueOf(new Date().getTime()));
                        myRef.child(selected_dis).child(selected_college).child("Management Details").setValue(managementDetailsModel, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Toast.makeText(getActivity(), "Not Saved Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                                    yearOfEst_et.setText("");
                                    capital_exp_et.setText("");
                                    unutilized_fund_et.setText("");
                                    challenges_et.setText("");
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
