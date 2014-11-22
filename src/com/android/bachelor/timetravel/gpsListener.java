package com.android.bachelor.timetravel;

import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.bachelor.timetravel.client.clientResponse;
import com.android.bachelor.timetravel.client.webClient;
import com.android.bachelor.timetravel.client.webClient.size;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class gpsListener implements LocationListener {

    static boolean locationChanged;
	public static Location curLocation;
	public static String CurLocationText;
	static int photosLength;
	static boolean locGot;
	protected static JSONArray photos;
	protected static ArrayList<clientResponse> crvc;
	
	@Override
	public void onLocationChanged(Location location) {
		
		if(curLocation == null)
        {
           curLocation = location;
           locationChanged = true;
           locGot=true;
        }
        
        if(curLocation.getLatitude() == location.getLatitude() && curLocation.getLongitude() == location.getLongitude()){
        	locationChanged = false;
        	locGot=true;
        }
        else{
           locationChanged = true;
           locGot=true;
        }
        curLocation = location;
        CurLocationText="Long.:"+curLocation.getLongitude()+", Lat.:"+curLocation.getLatitude();
        
        CameraPreview.ad.setTitle("GPS Coordinates");
        CameraPreview.ad.setMessage("Latitude: "+curLocation.getLatitude()+"\n"
    			+"Longitude: "+curLocation.getLongitude());
        CameraPreview.ad.setIcon(R.drawable.success);
        CameraPreview.ad.setButton("Dismiss",new AlertDialog.OnClickListener(){
    		public void onClick(DialogInterface dialog, int which){
    			return;
    		}
    		
    	});
        
		
        CameraPreview.pd.dismiss();
		
        Log.e("Time Travel","CurLocation LA:"+curLocation.getLatitude()+" LO:"+curLocation.getLongitude());
        CameraPreview.ar.clearARViews();
        
		
        new Thread(){

			public void run() {
        		webClient wc=new webClient();

                try {
        			
        			JSONObject photo;
        			photos=wc.flickrPhotoList(curLocation);
        			Long photoID;
        			
        			Location objectLoc = new Location("FLickrLocation") ;
        			if(photos != null && photos.length() > 0){
	        			Log.e("Time Travel",Integer.toString(photos.length()));
	        			photosLength=photos.length();
	        			String photoname;
	            		
	            	

	        			crvc=new ArrayList<clientResponse>();
	        			for(int i=0;i<photos.length();i++){
		            		//CameraPreview.pd2.show();
	        				photo=photos.getJSONObject(i);
	        				Bitmap b=wc.LoadImageFromWebOperations(wc.constructFlickrImgUrl(photo, size._s));
	        				Bitmap oimage=wc.LoadImageFromWebOperations(wc.constructFlickrImgUrl(photo, size._m));
	        				double[]geoInfo=wc.getPhotoGeoInfo(photo);
	        				objectLoc.setLatitude(geoInfo[0]);
	        				objectLoc.setLongitude(geoInfo[1]);
	        				photoID=wc.getPhotoID(photo);
	        				String dateTaken=wc.getPhotoDateTaken(photo);
	        				Log.e("Time Travel","objLocation "+i+ " LA:"+objectLoc.getLatitude()+" LO:"+objectLoc.getLongitude());
	        				photoname=wc.getPhotoTitle(photo);
	        				
	        				clientResponse cr=new clientResponse(CameraPreview.ctx, curLocation, objectLoc);
	        				CameraPreview.buttonPlaces.get(i).setText(photoname);
	        				cr.name=photoname;
	        				cr.thumbnail=b;
	        				cr.id=photoID;
	        				cr.image=oimage;
	        				cr.dateTaken=dateTaken;
	        				cr.latitude=objectLoc.getLatitude();
	        				cr.longitude=objectLoc.getLongitude();
	        				cr.setClickable(true);
	        				
	        				
	        				//Log.e("Time Travel","azimuth"+ curLocation.bearingTo(objectLoc));
	        				Log.e("Time Travel","cr azimuth"+ cr.getAzimuth()+"cr incl= "+cr.inclination);
	        				crvc.add(cr);
	        				CameraPreview.add(cr);
	        				//CameraPreview.ar.addARView(cr);
	        			}
	        			CameraPreview.pd2.dismiss();
	        			
        			}
        			
        			

        			
        			
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (JSONException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		
        		
        	};
        	
        }.start();
		
        
        
       
       LocationManager locman=(LocationManager)CameraPreview.ctx.getSystemService(Context.LOCATION_SERVICE);
       locman.removeUpdates(this);
	}
	
	private static double A;
	private static double F;
	private static final double EPS = 0.5E-13;
	private static double Ra;
	private static final double rad = Math.toRadians(1.0);
	private static final double deg = Math.toDegrees(1.0);
	public static Location findPoint(double lat1,
			double lon1, double az,
			double dist) {
		
		
		Location result = new Location("newLocation");
		

		

		A = 6378137.0;
		F = 1.0 / 298.257223563;
		Ra = 1.0 - F;

		// Algorithm from National Geodetic Survey, FORTRAN program "forward,"
		// subroutine "DIRCT1," by stephen j. frakes.
		// http://www.ngs.noaa.gov/TOOLS/Inv_Fwd/Inv_Fwd.html
		// Conversion to JAVA from FORTRAN was made with as few changes as
		// possible to avoid errors made while recasting form, and
		// to facilitate any future comparisons between the original
		// code and the altered version in Java.
		// Original documentation:
		//   SUBROUTINE DIRCT1(GLAT1,GLON1,GLAT2,GLON2,FAZ,BAZ,S)
		//
		//   SOLUTION OF THE GEODETIC DIRECT PROBLEM AFTER T.VINCENTY
		//   MODIFIED RAINSFORD'S METHOD WITH HELMERT'S ELLIPTICAL TERMS
		//   EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
		//
		//   A IS THE SEMI-MAJOR AXIS OF THE REFERENCE ELLIPSOID
		//   F IS THE FLATTENING OF THE REFERENCE ELLIPSOID
		//   LATITUDES AND LONGITUDES IN RADIANS POSITIVE NORTH AND EAST
		//   AZIMUTHS IN RADIANS CLOCKWISE FROM NORTH
		//   GEODESIC DISTANCE S ASSUMED IN UNITS OF SEMI-MAJOR AXIS A
		//
		//   PROGRAMMED FOR CDC-6600 BY LCDR L.PFEIFER NGS ROCKVILLE MD 20FEB75
		//   MODIFIED FOR SYSTEM 360 BY JOHN G GERGEN NGS ROCKVILLE MD 750608
		//

	
		double FAZ   = az * rad;
		double GLAT1 = lat1 * rad;
		double GLON1 = lon1 * rad;
		double S     = dist * 1000.;  // convert to meters
		double TU    = Ra * Math.sin(GLAT1) / Math.cos(GLAT1);
		double SF    = Math.sin(FAZ);
		double CF    = Math.cos(FAZ);
		double BAZ   = 0.;
		if (CF != 0) {
			BAZ = Math.atan2(TU, CF) * 2;
		}
		double CU  = 1. / Math.sqrt(TU * TU + 1.);
		double SU  = TU * CU;
		double SA  = CU * SF;
		double C2A = -SA * SA + 1.;
		double X   = Math.sqrt((1. / Ra / Ra - 1.) * C2A + 1.) + 1.;
		X = (X - 2.) / X;
		double C = 1. - X;
		C = (X * X / 4. + 1) / C;
		double D = (0.375 * X * X - 1.) * X;
		TU = S / Ra / A / C;
		double Y = TU;
		double SY, CY, CZ, E, GLAT2, GLON2;
		do {
			SY = Math.sin(Y);
			CY = Math.cos(Y);
			CZ = Math.cos(BAZ + Y);
			E  = CZ * CZ * 2. - 1.;
			C  = Y;
			X  = E * CY;
			Y  = E + E - 1.;
			Y = (((SY * SY * 4. - 3.) * Y * CZ * D / 6. + X) * D / 4. - CZ)
			* SY * D + TU;
		} while (Math.abs(Y - C) > EPS);
		BAZ   = CU * CY * CF - SU * SY;
		C     = Ra * Math.sqrt(SA * SA + BAZ * BAZ);
		D     = SU * CY + CU * SY * CF;
		GLAT2 = Math.atan2(D, C);
		C     = CU * CY - SU * SY * CF;
		X     = Math.atan2(SY * SF, C);
		C     = ((-3. * C2A + 4.) * F + 4.) * C2A * F / 16.;
		D     = ((E * CY * C + CZ) * SY * C + Y) * SA;
		GLON2 = GLON1 + X - (1. - C) * D * F;
		BAZ   = (Math.atan2(SA, BAZ) + Math.PI) * deg;
		result.setLatitude(GLAT2 * deg);
		result.setLongitude(GLON2 * deg);
		return result;
	}


	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
}
