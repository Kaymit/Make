package com.example.kevin.make;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ProjectActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener
{
    private static final String PROJECT_SERIALIZABLE_KEY = "project";
    private TextView projectTitle;
    private ImageView imageView;
    private ListView list;
    private Project project;
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Intent intent;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        list = (ListView) findViewById(R.id.categoriesList);
        projectTitle = (TextView) findViewById(R.id.projectTitleText);
        imageView = (ImageView) findViewById(R.id.imageView);

        project = (Project) getIntent().getExtras().getSerializable(PROJECT_SERIALIZABLE_KEY);
        projectTitle.setText(project.title);
        String projectImageURL = project.imageURL;

        ArrayList<String> categoriesStrings = project.categories;

        if (categoriesStrings != null)
        {
            ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    categoriesStrings);

            list.setAdapter(categoriesAdapter);
        }

        //pure magic
        setListViewHeightBasedOnChildren(list);

        //load image
        Glide.with(ProjectActivity.this)
                .load(projectImageURL)
                .into(imageView);

        list.setOnItemClickListener(this);
    }

    /**
     * Because apparently Android doesn't do nested scrolling, but also doesn't want to scroll without a ScrollView.
     * So more magic like measure(0, 0)
     * https://stackoverflow.com/questions/10709411/android-listview-rows-in-scrollview-not-fully-displayed-clipped
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    public void onItemClick(AdapterView<?> l, View v, int position, long id)
    {
        /*
        Intent intent = new Intent();
        intent.setClass(this, .class);
        intent.putExtra("position", position);
        intent.putExtra("id", id);
        startActivity(intent);
        */

    }



}