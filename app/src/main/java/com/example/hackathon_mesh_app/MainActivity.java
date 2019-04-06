package com.example.hackathon_mesh_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText username;
    private EditText password;
    private Button submit;
    private CallbackManager callbackManager;

    private static final String TAG = MainActivity.class.getName();
    private LoginButton loginButton;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("pref", 0); // 0 - for private mode
        editor = pref.edit();

        if(pref.getBoolean("loggedIn", false)) {
            Toast.makeText(getApplicationContext(), "user is logged in!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "user not logged in!", Toast.LENGTH_SHORT).show();
        }

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageResource(R.drawable.network);

        loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final String userId = loginResult.getAccessToken().getUserId();
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {

                                displayUserInfo(object);
                                editor.putBoolean("loggedIn", true); // Storing boolean - true/false
                                editor.putString("userId", userId);
                                editor.commit();
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {}
        });

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                        String email = username.getText().toString().trim();
                        String pw = password.getText().toString().trim();

                        if (TextUtils.isEmpty(email)) {
                            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(pw)) {
                            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (password.length() < 6) {
                            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mAuth.signInWithEmailAndPassword(email, pw)
                                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        Toast.makeText(MainActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                                        if (!task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Authentication failed." + task.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            editor.putBoolean("loggedIn", true); // Storing boolean - true/false
                                            editor.putString("userId", "USER ID HERE");
                                            editor.commit();
                                            //startActivity(new Intent(MainActivity.this, MainActivity.class));
                                            //finish();
                                        }
                                    }
                                });
                    }
                });

        if(currentUser != null) {
            Toast toast = Toast.makeText(context, "user logged in!", duration);
            toast.show();
        } else {
            Toast toast = Toast.makeText(context, "user not logged in!", duration);
            toast.show();
        }

    }

    private void displayUserInfo(JSONObject object) {
        String firstName, lastName, email, id;
        try {
            firstName = object.getString("name");

            Toast.makeText(MainActivity.this, firstName,
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
