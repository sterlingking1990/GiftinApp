package com.giftinapp.merchant;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AboutFragment extends Fragment {


    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        expandableListDetail = ExpandableListDataPump.getData();
        expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(getContext(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

//        expandableListView.setOnGroupExpandListener(groupPosition -> Toast.makeText(getContext(),
//                expandableListTitle.get(groupPosition) + " List Expanded.",
//                Toast.LENGTH_SHORT).show());

//        expandableListView.setOnGroupCollapseListener(groupPosition -> Toast.makeText(getContext(),
//                expandableListTitle.get(groupPosition) + " List Collapsed.",
//                Toast.LENGTH_SHORT).show());


//        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
//            Toast.makeText(
//                    getContext(),
//                    expandableListTitle.get(groupPosition)
//                            + " -> "
//                            + expandableListDetail.get(
//                            expandableListTitle.get(groupPosition)).get(
//                            childPosition), Toast.LENGTH_SHORT
//            ).show();
//            return false;
//        });


    }



}