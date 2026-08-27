package com.amc.window;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;

// Orientataion related 
import android.content.pm.ActivityInfo;

// Packages for fullscreen 
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity
{
	@SuppressLint("SetTextI18n")
    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		//Fullscreen 
		// HideActionBar
		getSupportActionBar().hide();

		// Tell the Android System to make your window expand edge to edge
		WindowCompat.setDecorFitsSystemWindows(getWindow(), false); // false for expand edge to edge not fit 

		// Get window insets controller and hide system bars
		WindowInsetsControllerCompat windowInsetsControllerCompat = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

		windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());

		GLESView glesView=new GLESView(this);

		getWindow().getDecorView().setBackgroundColor(Color.BLACK); // End Fullscreen

		setContentView(glesView);	
	}
}



