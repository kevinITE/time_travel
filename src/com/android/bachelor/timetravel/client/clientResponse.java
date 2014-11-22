package com.android.bachelor.timetravel.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import com.android.bachelor.ARkit.ARSphericalView;
import com.android.bachelor.timetravel.CameraPreview;


public class clientResponse extends ARSphericalView {
	public String name;
	public Bitmap thumbnail;
	
	public Long id;
	public int x;
	public int y;
	public Bitmap image;
	public String dateTaken;
	public double latitude;
	public double longitude;
	
	public clientResponse(Context ctx) {
		super(ctx);
		inclination=0;
		setFocusable(true);
		setClickable(true);
		// TODO Auto-generated constructor stub
		
	}
	
	
	public clientResponse(Context ctx,Location device, Location object) {
		super(ctx,device,object);
		inclination=0;
		setFocusable(true);
		setClickable(true);
		
		// TODO Auto-generated constructor stub
		
	}
	
	public void draw(Canvas can){
		
		if(thumbnail!=null && name!=null){
		
			int i = 0;
			while (i < CameraPreview.buttonPlaces.size()) {
				if (CameraPreview.buttonPlaces.get(i).getText().toString() == name)
					break;
				i++;
			}

			if (i > CameraPreview.buttonPlaces.size() - 1) {
				Log.e("GeonameObject",
						"***** DIDN'T FIND BUTTON with same name: " + name);
				return;
			}
			x=getLeft();
			y=getTop();
			CameraPreview.imageButtonPlaces.get(i).setImageBitmap(thumbnail);
			AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
					AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT, x, y);
			CameraPreview.linearLayoutPlaces.get(i).setLayoutParams(params);
			CameraPreview.linearLayoutPlaces.get(i).setVisibility(View.VISIBLE);
			
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		
		case MotionEvent.ACTION_DOWN:
			Log.e("TimeTravel", "action_downcr");
		case MotionEvent.ACTION_UP:
			Log.e("TimeTravel", "action_upcr");
		
		}
		return super.onTouchEvent(event);
	}
	
	
	
	

}
