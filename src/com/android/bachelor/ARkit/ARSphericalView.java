package com.android.bachelor.ARkit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.widget.Button;

public class ARSphericalView extends Button
{
	public volatile float azimuth; //Angle from north
	public volatile float distance; //Distance to object
	public volatile float inclination = -1; //angle off horizon.
	public volatile Location location;
	
	public volatile int x;
	public volatile int y;
	public volatile boolean visible = false;
	
	public static Location deviceLocation;
	//used to compute inclination
	public static float currentAltitude = 0;
	protected Paint p = new Paint();
	private float azimuth1;
	
	public ARSphericalView(Context ctx)
	{
		super(ctx);
		setClickable(true);
	}
	public ARSphericalView(Context ctx, Location deviceLocation, Location objectLocation)
	{
		super(ctx);
		setClickable(true);
		if (deviceLocation != null) {
			azimuth1 = deviceLocation.bearingTo(objectLocation);
			if(azimuth1>0)
				azimuth=azimuth1;
			else
				azimuth=(azimuth1+360)%360;
			
			distance = deviceLocation.distanceTo(objectLocation);
			if (deviceLocation.hasAccuracy() && objectLocation.hasAltitude()) {
				double opposite;
				boolean neg = false;
				if (objectLocation.getAltitude() > deviceLocation.getAltitude()) {
					opposite = objectLocation.getAltitude()
							- deviceLocation.getAltitude();
				} else {
					opposite = deviceLocation.getAltitude()
							- objectLocation.getAltitude();
					neg = true;
				}
				inclination=((float) Math
						.atan(((double) opposite / getDistance())));
				if (neg)
					inclination=(inclination * -1);
			}
		}
	}
	
	
	
	public void draw(Canvas c)
	{
		
	}
	public float getAzimuth() {
		return azimuth;
	}
	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}
	public float getDistance() {
		return distance;
	}
	public void setDistance(float distance) {
		this.distance = distance;
	}
	public float getInclination() {
		return inclination;
	}
	public void setInclination(float inclination) {
		this.inclination = inclination;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public static Location getDeviceLocation() {
		return deviceLocation;
	}
	public static void setDeviceLocation(Location deviceLocation) {
		ARSphericalView.deviceLocation = deviceLocation;
	}
	public static float getCurrentAltitude() {
		return currentAltitude;
	}
	public static void setCurrentAltitude(float currentAltitude) {
		ARSphericalView.currentAltitude = currentAltitude;
	}
	public Paint getP() {
		return p;
	}
	public void setP(Paint p) {
		this.p = p;
	}


}
