package com.android.bachelor.timetravel.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

public class webClient {

	public webClient() {
		
	}
	
	public Bitmap LoadImageFromWebOperations(String url){
 		try{
 			InputStream is = (InputStream) new URL(url).getContent();
 			Drawable d = Drawable.createFromStream(is, "src name");
 			Bitmap b=null;
 			BitmapDrawable dBitmap = (BitmapDrawable) d;
 			if (dBitmap != null)
 				Log.e("time travel", "bitmap got");
 			    b = dBitmap .getBitmap ();
 			    
 			return b;
 		}catch (Exception e) {
 			System.out.println("Exc="+e);
 			return null;
 		}
 	}
	
	
	public JSONArray flickrPhotoList(Location loc) throws IOException, JSONException {
	
        
        URL url = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.search&lat=" 
    		+loc.getLatitude() +"&lon="+ loc.getLongitude() +"&api_key=7bd171e581b9c79ad415a608451ca8bf&format=json&extras=geo,date_taken&user_id=61470049@N06");
        	//user_id=61470049@N06");
        URLConnection connection = url.openConnection();
        String line;
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
        while ((line = reader.readLine()) != null) {
                builder.append(line);
        }
 
 
        int start = builder.toString().indexOf("(") + 1;
        int end = builder.toString().length() -1 ;
        String jSONString = builder.toString().substring( start, end);
       
 
        JSONObject object = new JSONObject(jSONString); //whole json object
        JSONObject objectInner = object.getJSONObject("photos"); //inner Json object
        JSONArray photoArray = objectInner.getJSONArray("photo"); // inner array of photos
     
 
        return photoArray;
	}
	
	
	public String getPhotoTitle(JSONObject photo) throws JSONException{
		return photo.getString("title");
		
	}
	public String getPhotoDateTaken(JSONObject photo) throws JSONException{
		return photo.getString("datetaken");
		
	}
	
	public double[] getPhotoGeoInfo(JSONObject photo) throws JSONException{
		double[] geos=new double[2];
		geos[0]=Double.parseDouble(photo.getString("latitude"));
		geos[1]=Double.parseDouble(photo.getString("longitude"));
		return geos;
		
	}
	
	public JSONObject getPhotoFromTitle(JSONArray photos,String title) throws JSONException{
		for(int i=0;i<photos.length();i++){
			if(title==photos.getJSONObject(i).getString("title")){
				return photos.getJSONObject(i);
			}
		}
		Log.e("webClient-Timetravel", "no image with that title");
		return null;
	}
	 
	// source: flickr.com/services/api/misc.urls.html
	public enum size {
	        _s , _t ,_m, _b
	};
	
	public Long getPhotoID(JSONObject photo) throws JSONException{
		return Long.parseLong(photo.getString("id"));
	}
	 
	//helper method, to construct the url from the json object. You can define the size of the image that you want, with the size parameter. 
	//Be aware that not all images on flickr are available in all sizes.
	public String constructFlickrImgUrl(JSONObject input, Enum size) throws JSONException {
	        String FARMID = input.getString("farm");
	        String SERVERID = input.getString("server");
	        String SECRET = input.getString("secret");
	        String ID = input.getString("id");
	 
	        StringBuilder sb = new StringBuilder();
	      
	        sb.append("http://farm");
	        sb.append(FARMID);
	        sb.append(".static.flickr.com/");
	        sb.append(SERVERID);
	        sb.append("/");
	        sb.append(ID);
	        sb.append("_");
	        sb.append(SECRET);
	        sb.append(size.toString());                    
	        sb.append(".jpg");
	 
	        return sb.toString();
	}
	
	public String apiSignature(Double lat, Double lon, Long photoID){
		StringBuilder sb = new StringBuilder();
		//184f55306c096e10api_key7bd171e581b9c79ad415a608451ca8bfauth_token72157626861688932-97933114a3cf5078lat30.006072lon31.417283methodflickr.photos.geo.setLocationphoto_id5771165177 
        sb.append("184f55306c096e10");
        sb.append("api_key7bd171e581b9c79ad415a608451ca8bf");
        sb.append("auth_token72157626861688932-97933114a3cf5078");
        sb.append("lat"+Double.toString(lat));
        sb.append("lon"+Double.toString(lon));
        sb.append("methodflickr.photos.geo.setLocation");
        sb.append("photo_id"+Long.toString(photoID));
        
        
        return sb.toString();
	}
	
	public static String md5(String input){
        String res = "";
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res += "0" + tmp;
                } else {
                    res += tmp;
                }
            }
        } catch (NoSuchAlgorithmException ex) {}
        return res;
    }
	
	/*public String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	        
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}*/
	
	public void updateFlickrPhoto(Double lat, Double lon, Long photoID ) throws IOException{
		String apiSignatureString=apiSignature(lat, lon, photoID);
		//Log.e("timetravel", "md5 check="+md5("184f55306c096e10api_key7bd171e581b9c79ad415a608451ca8bfauth_token72157626861688932-97933114a3cf5078lat30.006072lon31.417283methodflickr.photos.geo.setLocationphoto_id5771165177"));
		Log.e("TimeTravel", "api_sig="+apiSignatureString);
		String md5ApiSign=md5(apiSignatureString);
		Log.e("Time Travel", "lat="+lat+" lon="+lon+" photoID="+photoID+" md5="+md5ApiSign);
		URL url = new URL("http://api.flickr.com/services/rest/?method=flickr.photos.geo.setLocation&api_key=7bd171e581b9c79ad415a608451ca8bf" +
		 		"&auth_token=72157626861688932-97933114a3cf5078"+
		 		"&lat="+lat+
		 		"&lon="+lon+
		 		"&photo_id="+photoID+
		 		"&api_sig="+md5ApiSign);
		        	
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
	}

}
