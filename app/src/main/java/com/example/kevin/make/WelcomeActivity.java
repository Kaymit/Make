package com.example.kevin.make;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

/**
 * Kevin Mitchell 2017
 * BCIT A00955303
 *
 * Inspired by and borrows from ZeroToApp Firebase demo chat app
 *      https://www.youtube.com/watch?v=xAsvwy1-oxE&t=141s
 *      https://gist.github.com/puf/f49a1b07e92952b44f2dc36d9af04e3c
 */
public class WelcomeActivity extends AppCompatActivity
{
    public static final String PROJECT_TITLE = "title";
    public static final String PROJECT_IMG_URL = "url";
    public static final String PROJECT_CATEGORIES = "categories";
    public static final String PROJECT_DB_REFERENCE = "projects";
    private static final String PROJECT_SERIALIZABLE_KEY = "project";
    public static final String TAG = "WelcomeActivity";

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<Project> projects;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Button loginButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginButton = (Button)findViewById(R.id.loginOrCreateButton);
        projects = new ArrayList<Project>();

        FirebaseApp app = FirebaseApp.getInstance();
        database = FirebaseDatabase.getInstance(app);
        auth = FirebaseAuth.getInstance(app);

        updateUI();
    }

    public void loginButtonPress(View view)
    {
        final Intent intent;

        Button button = (Button) view;
        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
        {
            auth.signOut();
        } else
        {
            intent = new Intent(this, EmailLoginActivity.class);
            updateUI();

            startActivity(intent);
        }
        updateUI();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateUI();
    }

    protected void onStart()
    {
        super.onStart();
        updateUI();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        updateUI();
    }



    public void submitProjectButtonPress(View view)
    {
        final Intent intent;
        final Button loginButton;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            Toast.makeText(WelcomeActivity.this,
                    R.string.login_before_submitting,
                    Toast.LENGTH_SHORT).show();
            loginButton = (Button) findViewById(R.id.loginOrCreateButton);
            loginButton.requestFocus();
            return;
        }

        intent = new Intent(this, SubmitProjectActivity.class);
        updateUI();
        startActivity(intent);
        updateUI();
    }

    public void makeSomethingButtonPress(View view)
    {
        Log.w("WelcomeActivity", "Make button pressed...");
        DatabaseReference databaseReference = database.getReference(PROJECT_DB_REFERENCE);
        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Random random = new Random();
                        int index = random.nextInt((int) dataSnapshot.getChildrenCount());
                        int count = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (count == index) {
                                Project project = snapshot.getValue(Project.class);
                                displayProject(project);
                                return;
                            }
                            count++;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "ERROR RETRIEVING DATA FROM DATABASE");
                    }
                });
        updateUI();
    }

    public void displayProject(Project project)
    {
        final Intent intent;

        intent = new Intent(WelcomeActivity.this, ProjectActivity.class);
        //intent.putExtra(PROJECT_SERIALIZABLE_KEY, project);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PROJECT_SERIALIZABLE_KEY, project);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void updateUI()
    {
        final Button loginButton;
        final TextView emailText;
        final String emailTextString;
        final String tempEmailString;

        loginButton = (Button)findViewById(R.id.loginOrCreateButton);
        emailText = (TextView)findViewById(R.id.emailText);
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) //if user is logged in...
        {
            loginButton.setText(R.string.logout);
            emailTextString = user.getEmail();
            tempEmailString = getString(R.string.logged_in) + " " + emailTextString;
            emailText.setText(tempEmailString);
        } else
        {
            loginButton.setText(R.string.login_or_sign_up);
            emailText.setText("Logged Out.");
        }
    }
}
