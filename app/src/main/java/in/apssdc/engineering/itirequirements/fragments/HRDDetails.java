package in.apssdc.engineering.itirequirements.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.apssdc.engineering.itirequirements.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HRDDetails extends Fragment {


    public HRDDetails() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hrddetails, container, false);
    }

}
