package com.android.bachelor.timetravel;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.android.bachelor.ARkit.ARLayout;
import com.android.bachelor.timetravel.client.clientResponse;
import com.android.bachelor.timetravel.client.webClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;



public class CameraPreview extends Activity implements SensorEventListener {    

	private Preview mPreview;
    
    SensorManager sensorManag;
    public float direction = (float) 0;
	public static float inclination;
	public static float rollingZ = (float)0;
	public static float rollingX = (float)0;
	public static float kFilteringFactor = (float)0.05;
	public float aboveOrBelow = (float)0;
    
	public static ARLayout ar;
	public static volatile Context ctx;
	LocationManager locMan;
	LocationListener gpsLn;

	protected Button buttonClicked;

	private int flickrphotos=50;
	
	static ImageView pic;
	public static FrameLayout f;
	static TextView txt;
	static TextView txt2;
	static String url;
	
	clientResponse crFocused;
	
	static ProgressDialog pd;
	static ProgressDialog pd2;
	Button saveEdit;
	Button cancelEdit;
	
	static AlertDialog ad;
	
	public static AbsoluteLayout al;
	
	public static Button buttonPlace1Button;
	
	public static LinearLayout linearLayoutPlace1;
	
	public static List<LinearLayout> linearLayoutPlaces = new ArrayList<LinearLayout>();
	public static List<Button> buttonPlaces = new ArrayList<Button>();
	public static List<ImageButton> imageButtonPlaces = new ArrayList<ImageButton>();
	static List<clientResponse> photosList = new ArrayList<clientResponse>();
	Vibrator vibrator ;
	AbsoluteLayout lEdit;
	AbsoluteLayout lPic;
	 AlertDialog adEdit;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this.getApplicationContext();
    	vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);//sets the format of the window of the current activity to support alpha bits 
        requestWindowFeature(Window.FEATURE_NO_TITLE);//extends extra feature of the windows to remove the title on the window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//sets the flags of the activity window by the full screen flags predefined
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        ar= new ARLayout(this.getApplicationContext());
        ar.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN){
					Log.e("Time Travel", "x="+event.getX()+"y="+event.getY());
				}
				return false;
			}
		});
        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
    	int width = d.getWidth();
        int height = d.getHeight(); 
        ar.screenHeight = height;
        ar.screenWidth = width;
        
        mPreview = new Preview(this);
        /////////////////////////////////////////////////////////////////////////////
        sensorManag = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        ///////////////////////////////////////////////GPS thread////////////////////////////////////////
        
        //gpsLn= new gpsListener();  
        //locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, gpsLn);
       
        
        
        
        /////////////////////////////////////////Picture////////////////////////////////////////////////////
        AbsoluteLayout.LayoutParams paramsSave = new AbsoluteLayout.LayoutParams(
				150, 80, 46, 390);
        saveEdit=new Button(this);
        saveEdit.setText("Save");
        saveEdit.setLayoutParams(paramsSave);
        saveEdit.setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float oldAzimuth=crFocused.azimuth;
				float oldIncl=crFocused.inclination;
				setAzimuth(ar.direction, ar.inclination);
				ar.addARView(crFocused);
				Location lo=gpsListener.findPoint(gpsListener.curLocation.getLatitude(), gpsListener.curLocation.getLongitude(), crFocused.azimuth, crFocused.distance);
				Log.e("time travel", "crfocused newlatitude= "+lo.getLatitude()+" new longiude= "+lo.getLongitude());
				adEdit.setMessage("Image: "+crFocused.name
								+"\n Old lat.= "+crFocused.latitude
								+"\n New lat.="+lo.getLatitude()
								+"\n Old long.="+crFocused.longitude
								+"\n New long.="+lo.getLongitude()
								+"\n Date Taken="+crFocused.dateTaken);
				adEdit.setButton("Dismiss",new AlertDialog.OnClickListener(){
		    		public void onClick(DialogInterface dialog, int which){
		    			return;
		    		}
		    		
		    	});
				webClient wcl=new webClient();
				try {
					wcl.updateFlickrPhoto(lo.getLatitude(), lo.getLongitude(), crFocused.id);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adEdit.show();
				ar.postInvalidate();
				lEdit.setVisibility(View.GONE);
				//al.setVisibility(View.VISIBLE);
				
			}
		});
       
    
        AbsoluteLayout.LayoutParams paramsCancel = new AbsoluteLayout.LayoutParams(
				150, 80, 645, 390);
        cancelEdit=new Button(this);
        cancelEdit.setText("Cancel");
        cancelEdit.setLayoutParams(paramsCancel);
        cancelEdit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				lPic.setVisibility(View.GONE);
				lEdit.setVisibility(View.GONE);
				al.setVisibility(View.VISIBLE);
			}
		});
        
        lEdit=new AbsoluteLayout(this);
       	lEdit.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
       	//lEdit.setGravity(Gravity.BOTTOM);
       	lEdit.setVisibility(View.GONE);
       	//lEdit.setOrientation(LinearLayout.HORIZONTAL);
       	lEdit.addView(saveEdit);
       	lEdit.addView(cancelEdit);
       	
       	AbsoluteLayout.LayoutParams paramsPic = new AbsoluteLayout.LayoutParams(
				400, 400, 207, 67);
       	pic=new ImageView(CameraPreview.ctx);
        pic.setLayoutParams(paramsPic);
        
        pic.setOnClickListener(new View.OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				al.setVisibility(View.VISIBLE);
				lEdit.setVisibility(View.GONE);
				lPic.setVisibility(View.GONE);
				vibrator.vibrate(100);
				
			}
		});
        
        lPic=new AbsoluteLayout(this);
        lPic.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        lPic.setVisibility(View.GONE);
        lPic.addView(pic);
        
        
        al = new AbsoluteLayout(getApplicationContext());
       
       	
		f=new FrameLayout(this);
        //f.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        f.addView(mPreview);
        
        
        f.addView(al);
        f.addView(lPic);
		f.addView(lEdit);
        f.addView(ar,width,height);
        setContentView(f);
        addLoadingLayouts();
        linearLayoutPlaces = new ArrayList<LinearLayout>();
    	buttonPlaces = new ArrayList<Button>();
    	imageButtonPlaces = new ArrayList<ImageButton>();
        placeLayout();
        
    }
    
    protected void setAzimuth(float directionFromSensor,double inclinationFromSensor) {
		// TODO Auto-generated method stub
		crFocused.azimuth=directionFromSensor;
		crFocused.inclination=(float) inclinationFromSensor;
		Log.e("Time travel", "Crfocused now"+crFocused.name+" at azimuth="+crFocused.azimuth);
		ar.removeARView(crFocused);
	}

	public void placeLayout() {
		/*OnClickListener clicklistener = new OnClickListener() {
			public void onClick(View v) {
				buttonClicked = buttonPlaces.get(v.getId());
				
		       
				Log.e("TimeTravel", "Clicked " + buttonClicked.getText());

			}
		};*/
		
		OnLongClickListener longlistener=new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				buttonClicked = buttonPlaces.get(v.getId());
				crFocused=CameraPreview.findCR(buttonClicked.getText());
				Log.e("TimeTravel", "long clicked on"+buttonClicked.getText());

				pic.setImageBitmap(CameraPreview.findThumbnail(crFocused.name));
				lPic.setVisibility(View.VISIBLE);
				lEdit.setVisibility(View.VISIBLE);
				al.setVisibility(View.GONE);
				vibrator.vibrate(100);
				
				return false;
			}
		};

		while (buttonPlaces.size() == 0) {
			Log.e("TimeTravel", "initializing views");
			for (int i = 0; i < flickrphotos; i++) {
				
				buttonPlace1Button = new Button(getApplicationContext());
				buttonPlaces.add(buttonPlace1Button);
			}
			
			for (int i = 0; i < flickrphotos; i++) {
				ImageButton imageButtonPlace1Button = new ImageButton(
						getApplicationContext());
				imageButtonPlaces.add(imageButtonPlace1Button);
				
			}
			
			for (int i = 0; i < flickrphotos; i++) {
				linearLayoutPlace1 = new LinearLayout(getApplicationContext());
				linearLayoutPlaces.add(linearLayoutPlace1);
			
			}
			
		}

		for (int i = 0; i < buttonPlaces.size(); i++) {
			buttonPlaces.get(i).setId(i);
			buttonPlaces.get(i).setBackgroundColor(Color.TRANSPARENT);
			buttonPlaces.get(i).setTextColor(Color.TRANSPARENT);
			
			
			imageButtonPlaces.get(i).setId(i);
			//imageButtonPlaces.get(i).setOnClickListener(clicklistener);
			imageButtonPlaces.get(i).setOnLongClickListener(longlistener);
			imageButtonPlaces.get(i).setBackgroundResource(R.drawable.speech_bubble);
			

			linearLayoutPlaces.get(i).setOrientation(LinearLayout.VERTICAL);
			linearLayoutPlaces.get(i).setVisibility(View.GONE);
			linearLayoutPlaces.get(i).addView(imageButtonPlaces.get(i));
			linearLayoutPlaces.get(i).addView(buttonPlaces.get(i));
			al.addView(linearLayoutPlaces.get(i));
		}

		
	}
  
   


	protected static clientResponse findCR(CharSequence name) {
		if(gpsListener.crvc.size()>0){
			Log.e("Time travel", Integer.toString(gpsListener.crvc.size()));
			for (int i = 0; i < gpsListener.crvc.size(); i++) {
				if(name==gpsListener.crvc.get(i).name){
					Log.e("TimeTravel", "crFocused=name "+gpsListener.crvc.get(i).name+" azimuth= "+gpsListener.crvc.get(i).azimuth);
					return gpsListener.crvc.get(i);
				}
			}
		}
		return null;
	}

	public static Bitmap findThumbnail(CharSequence name) {
		if(gpsListener.crvc.size()>0){
			Log.e("Time travel", Integer.toString(gpsListener.crvc.size()));
		for (int i = 0; i < gpsListener.crvc.size(); i++) {
			if(name==gpsListener.crvc.get(i).name){
				return gpsListener.crvc.get(i).image;
			}
		}
		}
		return null;
		
	}

	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		gpsListener.locGot=false;
		locMan.removeUpdates(gpsLn);
		super.onPause();
	}




	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		locMan= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		gpsLn=new gpsListener();
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, gpsLn);
		ad=new AlertDialog.Builder(this).create();
		adEdit=new AlertDialog.Builder(this).create();
		adEdit.setTitle("Edit Successful");
		adEdit.setIcon(R.drawable.success);
		
		pd2=new ProgressDialog(this);
		pd2.setTitle("Please wait....");
		pd2.setMessage("Downloading Photos nearby you");
		pd2.setIcon(R.drawable.icon_download);
		
		if ( !locMan.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        else{
        	if(gpsListener.locGot==false){
        		pd2.show();
        		pd=ProgressDialog.show(this, "Please wait....", "Getting GPS coordinates");
        		pd.setIcon(R.drawable.earth_find);
        		
        	}
        	else{
        		pd.dismiss();
        	}
        }
		
		
		sensorManag.registerListener(this, sensorManag.getDefaultSensor(Sensor.TYPE_ORIENTATION), 
        		SensorManager.SENSOR_DELAY_UI);
        sensorManag.registerListener(this,sensorManag.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		   SensorManager.SENSOR_DELAY_UI);
       
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,0,0,"Show Location info.").setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 2, 0, "Show debugging info.").setIcon(android.R.drawable.ic_menu_compass);
		menu.add(0, 1, 0, "Exit").setIcon(android.R.drawable.ic_lock_power_off);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case 0:
	    	Toast.makeText(this, "show info", Toast.LENGTH_SHORT);
	    	ad.show();
	    	break;
	    case 1:
	    	Toast.makeText(this, "exit", Toast.LENGTH_SHORT);
	    	finish();
	    	break;
	    case 2:
	    	Toast.makeText(this, "debug info", Toast.LENGTH_SHORT);
	    	ar.debug=true;
	    	break;
	    }
	    return true;
	}
	
	
	
	private void launchGPSOptions() {
		 Intent gpsOptionsIntent = new Intent(  
	                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
	        startActivity(gpsOptionsIntent); 
	    }  


	private void buildAlertMessageNoGps() {
	        AlertDialog alertGPS = new AlertDialog.Builder(this)
	        .setTitle("GPS Attention Required!")
	        .setMessage("Your GPS seems to be disabled,\nDo you want to enable it?")
	        .setCancelable(false)
	        .setIcon(R.drawable.do_you_want)
	        .setPositiveButton("Yes", new AlertDialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                launchGPSOptions();
	            }
	        })
	        .setNegativeButton("No", new AlertDialog.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                appError();
	            }

	        }).create();
	        
	        alertGPS.show();
	}
	    
	private void appError() {
	    	final AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	        builder1.setMessage("The Time Travel Application will not work,\n with the GPS turned off. " +
	        		"\nThe Application will have to close")
	        .setCancelable(false)
	        .setTitle("Error")
	        .setIcon(R.drawable.error)
	        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	                finish(); 
	            }
	        });
	        final AlertDialog alert = builder1.create();
	        alert.show();
			
	}
	
	private void addLoadingLayouts()
	{
		clientResponse fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 0;
		fs.inclination = 0;
		fs.name = "Loading 0";
		ar.addARView(fs);
		fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 45;
		fs.inclination = 0;
		fs.name = "Loading 45";
		ar.addARView(fs);
		fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 90;
		fs.inclination = 0;
		fs.name = "Loading 90";
		ar.addARView(fs);
		fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 180;
		fs.inclination = 0;
		fs.name = "Loading 180";
		ar.addARView(fs);
		fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 210;
		fs.inclination = 0;
		fs.name = "Loading 210";
		ar.addARView(fs);
		fs = new clientResponse(this.getApplicationContext());
		fs.azimuth = 270;
		fs.inclination = 0;
		fs.name = "Loading 270";
		ar.addARView(fs);
		ar.postInvalidate();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		sensorManag.unregisterListener(this);
		locMan.removeUpdates(gpsLn);
	
		super.onStop();
	}
	
///////////////////////////////////////////////////////////ACCELEROMETER & ORIENTATION////////////////////////////////////////////
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent evt) {
		float vals[] = evt.values;
	      
	      if(evt.sensor.getType() == Sensor.TYPE_ORIENTATION)
	      {
	         float rawDirection = vals[0];

	         direction =(float) ((rawDirection * kFilteringFactor) + 
	            (direction * (1.0 - kFilteringFactor)));

	          inclination = 
	            (float) ((vals[2] * kFilteringFactor) + 
	            (inclination * (1.0 - kFilteringFactor)));

	                
	          if(aboveOrBelow > 0)
	             inclination = inclination * -1;
	          
	         if(evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
	         {
	            aboveOrBelow =
	               (float) ((vals[2] * kFilteringFactor) + 
	               (aboveOrBelow * (1.0 - kFilteringFactor)));
	         }
	      }
	}




	
	
	public static void add(clientResponse crvc) {
		// TODO Auto-generated method stub
		if(crvc!=null){
			
			Log.e("timetravel", "add cr");
			Log.e("timetravel", "id= "+crvc.id+" , name= "+crvc.name);
			
			ar.addARView(crvc);
			
		//	ar.postInvalidate();
		
		}
	}
	
	

}
