package com.example.spotifyrecommendations.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.spotifyrecommendations.GroupChatActivity;
import com.example.spotifyrecommendations.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_groups = new ArrayList<>();
    private ArrayList<String> list_my_groups = new ArrayList<>();
    private String list_type;
    private HashSet saved_chats;

    private DatabaseReference groupRef;

    public GroupsFragment(){}



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false);
        
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list_view = view.findViewById(R.id.group_list);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list_groups);
        list_view.setAdapter(arrayAdapter);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        
        getGroups();

        list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                saved_chats.add(parent.getItemAtPosition(position).toString());

                return true;
            }
        });


        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String curr_group_name = parent.getItemAtPosition(position).toString();

                Intent gc_intent = new Intent(getContext(), GroupChatActivity.class);
                gc_intent.putExtra("group name", curr_group_name);
                startActivity(gc_intent);

            }
        });

    }

    private void getGroups() {
       groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()){

                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                list_groups.clear();
                list_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

    });


} }