package com.example.kevin.make;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import static com.example.kevin.make.WelcomeActivity.PROJECT_DB_REFERENCE;

public class SubmitProjectActivity extends AppCompatActivity
{
    static final int RC_PHOTO_PICKER = 1;
    private static final String FILE_TYPE = "image/jpeg";
    private static final String CATEGORIES_DB_REF = "categories";
    private static final String PROJECT_PICS_STORAGE_REF = "project_pics";

    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;

    private Uri downloadURL;
    private String downloadURLString;
    private String titleString;
    private EditText titleText;
    private EditText categoryText;
    private ArrayList<String> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Button addPictureButton;
        final Button submitProjectButton;
        final Button addCategoryButton;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_project);

        app = FirebaseApp.getInstance();
        database = FirebaseDatabase.getInstance(app);
        auth = FirebaseAuth.getInstance(app);
        storage = FirebaseStorage.getInstance(app);
        databaseRef = database.getReference(PROJECT_DB_REFERENCE);

        categories = new ArrayList<String>();

        categoryText = (EditText) findViewById(R.id.categoryText);
        titleText = (EditText) findViewById(R.id.titleInputText);
        addCategoryButton = (Button) findViewById(R.id.addCategoryButton);
        addCategoryButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final String categoryString = categoryText.getText().toString();
                if(categories.size() <= 9)
                {
                    categories.add(categoryString);
                    Toast.makeText(SubmitProjectActivity.this,
                        R.string.category_added,
                        Toast.LENGTH_SHORT).show();
                    categoryText.setText(""); //reset category text box
                } else {
                    Toast.makeText(SubmitProjectActivity.this,
                            R.string.too_many_categories,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPictureButton = (Button) findViewById(R.id.addPictureButton);
        addPictureButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(FILE_TYPE);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent,
                        getString(R.string.complete_using)),
                        RC_PHOTO_PICKER);
            }
        });

        submitProjectButton = (Button) findViewById(R.id.submitProjectButton);
        submitProjectButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                titleString = titleText.getText().toString();
                //validate project
                if (TextUtils.isEmpty(titleString))
                {
                    titleText.setError(getString(R.string.error_field_required));
                    titleText.requestFocus();
                } else if (downloadURL == null) {
                    titleText.setError(getString(R.string.please_upload_picture));
                    addPictureButton.requestFocus();
                } else {
                    downloadURLString = downloadURL.toString();
                    Project project = new Project(titleString, downloadURLString, categories);

                    databaseRef.push().setValue(project);

                    Toast.makeText(SubmitProjectActivity.this,
                            R.string.project_submitted,
                            Toast.LENGTH_LONG).show();
                    Log.w("SubmitProjectActivity", "Project Submitted");

                    //save all categories to DB for sorting later
                    databaseRef = database.getReference(CATEGORIES_DB_REF);
                    for (String s: categories)
                    {
                        databaseRef.push().setValue(s);
                    }


                    try
                    {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
        });
    }

    /**
     * From ZeroToApp Firebase example
     *
     * called when picture has been uploaded or not
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        final StorageReference photoRef;
        final ImageView imageView;
        final StorageReference storageRef;
        final ProgressBar progressBar;

        progressBar = (ProgressBar) findViewById(R.id.uploadProgressBar);
        imageView = (ImageView)findViewById(R.id.imageView);
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();


            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
/*
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            */

            progressBar.setVisibility(View.VISIBLE);
            progressBar.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });




            // Get a reference to the location where we'll store our photos
            storageRef = storage.getReference(PROJECT_PICS_STORAGE_REF);
            // Get a reference to store file at chat_photos/<FILENAME>
            photoRef = storageRef.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // When the image has successfully uploaded, we get its download URL for Project uploading to DB
                            @SuppressWarnings("VisibleForTests")
                            Uri temp = taskSnapshot.getDownloadUrl();
                            downloadURL = temp; //hacky warning suppression

                            Toast.makeText(SubmitProjectActivity.this,
                                    "Image uploaded",
                                    Toast.LENGTH_SHORT).show();

                            //display chosen image from Storage to confirm upload
                            Glide.with(SubmitProjectActivity.this)
                                    .using(new FirebaseImageLoader())
                                    .load(photoRef)
                                    .into(imageView);
                        }
                    });
        }
    }
}
