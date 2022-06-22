package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "com.example.playlistmaker://callback";
    private static final String CLIENT_ID = "7c796e4edc85468f91527ac1df57de48";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationRequest.Builder builder =
                        new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
                builder.setScopes(new String[]{"user-library-modify", "user-top-read", "playlist-modify-public", "playlist-modify-private", "playlist-read-collaborative", "playlist-read-private"});
                AuthorizationRequest request = builder.build();

                AuthorizationClient.openLoginActivity(LoginActivity.this, REQUEST_CODE, request);

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Toast.makeText(this, "Successful", Toast.LENGTH_SHORT).show();
                    String token = response.getAccessToken();
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra("token", token);
                    startActivity(i);



//                    SpotifyApi spotifyApi = new SpotifyApi(token);
//                    Map<String, String> optionalParams = new HashMap<>();
//                    Toast.makeText(this, spotifyApi.getAlbum("11dFghVXANMlKmJXsNCbNl", optionalParams).getName(), Toast.LENGTH_SHORT).show();
//                    //spotifyApi.getCurrentUser().getDisplayName();


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
}