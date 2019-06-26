package com.android.example.gawala;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
// TODO: 6/26/2019  make an early version of this app with in these 5 days
//  below are some abstract level steps each one need further expalanation.
//  1) make a connection to firebase // now here
//  2) initially no map is to be displayed
//  3) create a  mechanism to have connections of the consumers and providers
//  4) on the tap of a button producer will share location to consumer
//  5) consimer will simple get the values initilly then we will go for the map later
//  6) its time to add maps on both sides
//  7) now make the things funtional on the app.