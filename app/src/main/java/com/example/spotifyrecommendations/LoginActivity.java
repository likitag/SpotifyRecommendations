package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.net.URL;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spotify.api.spotify.SpotifyApi;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";
    Button btnLogin;
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "com.example.playlistmaker://callback";
    private static final String CLIENT_ID = "7c796e4edc85468f91527ac1df57de48";
    String token;
    String username;
    String password;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    LottieAnimationView music_listen;
    LottieAnimationView notes;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        btnLogin = findViewById(R.id.btnLogin);
        music_listen = findViewById(R.id.lottie_music_listen);
        notes = findViewById(R.id.lottie_music_notes);
        notes.playAnimation();
        music_listen.playAnimation();
        if (ParseUser.getCurrentUser()!=null){
            ParseUser.logOut();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnection()){
                    AuthorizationRequest.Builder builder =
                            new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
                    builder.setScopes(new String[]{"user-modify-playback-state", "user-library-modify", "user-top-read", "playlist-modify-public", "playlist-modify-private", "playlist-read-collaborative", "playlist-read-private"});
                    builder.setShowDialog(true);
                    AuthorizationRequest request = builder.build();

                    AuthorizationClient.openLoginActivity(LoginActivity.this, REQUEST_CODE, request);
                }
                else{
                    Toast.makeText(LoginActivity.this, "Please connect to the internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public  boolean isInternetConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token

                case TOKEN:
                    //indicates that spotify login was successful
                    Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();

                    //retrieves access token
                    token = response.getAccessToken();
                    editor.putString("token", token);
                    editor.apply();

                    //executes AsyncTask class
                    getUserCredentials();
                    //new Task().execute();

                    break;

                // Auth flow returned an error
                case ERROR:

                    Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases

            }
        }
    }

    private void getUserCredentials() {
        kaaes.spotify.webapi.android.SpotifyApi api_kaees = new kaaes.spotify.webapi.android.SpotifyApi();
        api_kaees.setAccessToken(token);
        SpotifyService service = api_kaees.getService();

        service.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                username = userPrivate.display_name;
                password = userPrivate.id;
                editor.putString("password", password);
                editor.apply();
                loginUser(username, password);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void loginUser(String username, String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e!=null ){
                    //if there is no Parse User that exists with these credentials, we will create a sign up as a new Parse User using these credentials
                    signUp(username, password);
                    String email = username + "@gmail.com";
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Log.i(TAG, "successfully created firebase user: ");
                                Toast.makeText(LoginActivity.this, "successfully created firebase user", Toast.LENGTH_SHORT).show();

                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "error login firebase: " + message, Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "error creating firebase user: "+ message);

                            }

                        }
                    });

                    //Log.e("error", "issue with login", e);
                    return;

                }
                String email = username + "@gmail.com";

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "loggin in firebase", Toast.LENGTH_SHORT).show();
                            //goMainActivity();
                            showPrivacyRules();
                        }
                        else {
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Log.i(TAG, "successfully created firebase user: ");
                                        Toast.makeText(LoginActivity.this, "successfully created firebase user", Toast.LENGTH_SHORT).show();

                                    }
                                    else{
                                        String message = task.getException().toString();
                                        Toast.makeText(LoginActivity.this, "error login firebase: " + message, Toast.LENGTH_SHORT).show();
                                        Log.i(TAG, "error creating firebase user: "+ message);

                                    }

                                }
                            });

                        }


                    }
                });
                Log.i(TAG, "logging in old user");
               // goMainActivity();


            }
        });




    }

    private void signUp(String username, String password){
        Log.i(TAG, "signing up new user");
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    String email = username + "@gmail.com";
                    //goMainActivity();
                    showPrivacyRules();
                } else {
                    Log.e("tag", "done:", e);

                }

            }
        });






    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("token", token);
        startActivity(i);
    }

    private void showPrivacyRules() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Spotifind user privacy");

        final TextView information = new TextView(LoginActivity.this);
        information.setPadding(20, 20, 20, 20);
        information.setTextSize(15);
        information.setText("Spotifind will use information regarding your Spotify listening history to create customized playlists for you. " +  "\n\n" +
                "Spotifind will maintain data regarding the following: " + "\n" +
                "- every playlist created through this app"+ "\n" +
                "- all posts that you create" + "\n" + "- all playlists that you favorite or save" + "\n" + "- all messages that are sent in groupchats" + "\n\n" +
                "You are able at any point to view all personal information collected on you, and also delete all personal information collected on you");

        builder.setView(information);
        builder.setPositiveButton("Accept and proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goMainActivity();
            }
        });

        builder.setNegativeButton("Don't accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();


            }
        });
        builder.show();
    }
}