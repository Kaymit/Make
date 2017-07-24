package com.example.kevin.make;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kevin on 20/07/2017.
 */

public class Project implements Serializable
{
    public String title;
    public String imageURL;
    public ArrayList<String> categories;

    public Project() {}

    // Each project holds its own categories
    public Project(String title, String imageURL, ArrayList<String> categories)
    {
        this.title = title;
        this.imageURL = imageURL;
        this.categories = categories;
    }
}
