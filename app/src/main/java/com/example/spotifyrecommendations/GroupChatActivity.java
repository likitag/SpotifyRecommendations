package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private static final String TAG = "GC";
    private EditText etMessage;
    private ImageButton btnSend;
    private ScrollView scroll_view;
    private TextView displayTexts;
    private String currentGroupName, currentUserID, currentUsername, currentDate, currentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    private ImageButton back;
    private TextView tvDisplayName;
    private TextView tvDate;
    private TextView tvTimeStamp;
    String initTime = "";
    String chatDay = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        displayTexts = findViewById(R.id.gc_text_display);
//        tvDate = findViewById(R.id.tvDate);
//        tvTimeStamp = findViewById(R.id.tvTimeStamp);
//        tvDisplayName = findViewById(R.id.tvDisplayName);

        etMessage = findViewById(R.id.input_group_message);
        btnSend = findViewById(R.id.btnSend);
        scroll_view = findViewById(R.id.scroll_view);
        currentGroupName = getIntent().getExtras().get("group name").toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();
        getSupportActionBar().setTitle(currentGroupName);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentUsername = ParseUser.getCurrentUser().getUsername();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        back = findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupChatActivity.this.finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessage();
                etMessage.setText("");
                scroll_view.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

    }

    private void SaveMessage() {
        String message = etMessage.getText().toString();
        String messageKey = GroupNameRef.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this, "please add a message!", Toast.LENGTH_SHORT).show();

        }
        else {
            Calendar curr_date = Calendar.getInstance();
            SimpleDateFormat currDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currDateFormat.format(curr_date.getTime());

            Calendar curr_time = Calendar.getInstance();
            SimpleDateFormat currTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currTimeFormat.format(curr_time.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);
            GroupMessageKeyRef = GroupNameRef.child(messageKey);
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);
            messageInfoMap.put("name", mAuth.getCurrentUser().getEmail());
            GroupMessageKeyRef.updateChildren(messageInfoMap);



        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMessages(snapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();


        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

            chatName = chatName.substring(0, chatName.indexOf("@"));


            Boolean full  = timeDiff(chatTime, initTime);


            if (!chatDate.equals(chatDay)) {
                displayTexts.append(chatDate + " \n " + chatTime + " : \n" + chatName + " \n" + chatMessage + "\n\n\n");

            }
            else if (chatDate.equals(chatDay)&&!full){
                displayTexts.append(chatTime + " : \n" + chatName + " \n" + chatMessage + "\n\n\n");

            }

            else{
                displayTexts.append(chatName + " :\n" + chatMessage + "\n\n\n");


            }
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN);
            initTime = chatTime;

            chatDay = chatDate;




        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Boolean timeDiff(String chatTime, String initTime) {
        if(initTime.equals("")){
            return false;
        }
        int hours_curr = Integer.parseInt(chatTime.substring(0, chatTime.indexOf(":")));
        int mins_curr = Integer.parseInt(chatTime.substring(chatTime.indexOf(":") + 1, chatTime.indexOf(" ")));
        String AM_PM_curr = chatTime.substring(chatTime.indexOf(" "));

        if(AM_PM_curr.equals("PM")){
            hours_curr = hours_curr + 12;
        }

        int hours_init = Integer.parseInt(initTime.substring(0, initTime.indexOf(":")));
        int mins_init = Integer.parseInt(initTime.substring(initTime.indexOf(":") +1, initTime.indexOf(" ")));
        String AM_PM_init = initTime.substring(initTime.indexOf(" "));

        if(AM_PM_init.equals("PM")){
            hours_init = hours_init + 12;

        }

        String hours_curr_string = Integer.toString(hours_curr);
        String hours_init_string= Integer.toString(hours_init);
        String mins_curr_string= Integer.toString(mins_curr);
        String mins_init_string= Integer.toString(mins_init);

        if (hours_curr < 10) {
            hours_curr_string = "0" + hours_curr;
        }

        if (hours_init < 10) {
            hours_init_string= "0" + hours_init;
        }

        if (mins_curr < 10) {
            mins_curr_string = "0" + mins_curr;
        }

        if (mins_init < 10) {
            mins_init_string = "0" + mins_init;
        }




        String time_curr_chat = hours_curr_string + ":" + mins_curr_string + ":" + "00";


        String time_init_chat = hours_init_string + ":" + mins_init_string + ":" + "00";


        long seconds = Duration.between(LocalTime.parse(time_curr_chat), LocalTime.parse(time_init_chat)).getSeconds();
        if(Math.abs(seconds) < 3600){
            return true;
        }

        return false;
    }
}