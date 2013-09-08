package com.openxc.openxcstarter;
 
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.openxc.VehicleManager;

import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.measurements.BatteryLevel;
import com.openxc.measurements.ChargingStatus;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;

import com.openxc.remote.VehicleServiceException;


public class StarterActivity extends FragmentActivity {
	
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

	private VehicleManager mVehicleManager;
	private TextView mVehicleSpeedView;
	private TextView mVehicleBatteryLevel;
	private TextView mVehicleChargingStatus;
	private TextView mLatitudeView;
	private TextView mLongitudeView;
	
	private double latitude;
	private double longitude;
	private LatLng currentCoord;
	
    private final Handler mHandler = new Handler();
 
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        Log.i("openxc", "Bound to VehicleManager");
	        mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
	        	        	        
	        try {
				mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
				mVehicleManager.addListener(BatteryLevel.class, mBatteryListener);
				mVehicleManager.addListener(ChargingStatus.class, mChargingListener);
				mVehicleManager.addListener(Latitude.class, mLatitudeListener);
				mVehicleManager.addListener(Longitude.class, mLongitudeListener);

			} catch (VehicleServiceException e) {
				e.printStackTrace();
			} catch (UnrecognizedMeasurementTypeException e) {
				e.printStackTrace();
			}
	    }
 
	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.w("openxc", "VehicleService disconnected unexpectedly");
	        mVehicleManager = null;
	    }
	};
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_starter);
		
        setUpMapIfNeeded();
        
        /* TODO: replace with current position on start */
        latitude = 37.482906;
        longitude = -122.177956;

        /* Calculate Route every 10 meters */
        
        /* Set Interval */
		
		mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
		mVehicleBatteryLevel = (TextView) findViewById(R.id.battery_level);
		mVehicleChargingStatus = (TextView) findViewById(R.id.charging_status);
		mLatitudeView = (TextView) findViewById(R.id.latitude);
		mLongitudeView = (TextView) findViewById(R.id.longitude);

		Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);   
	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.starter, menu);
		return true;
	}
	
	public void onPause() {
	    super.onPause();
	    Log.i("openxc", "Unbinding from vehicle service");
	    unbindService(mConnection);
	}
	
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
        	
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
    
	VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
	    public void receive(Measurement measurement) {
	    	final VehicleSpeed speed = (VehicleSpeed) measurement;
	        StarterActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	                mVehicleSpeedView.setText(
	                    "Vehicle speed (km/h): " + speed.getValue().doubleValue());
	            }
	        });
	    }
	};
	
	BatteryLevel.Listener mBatteryListener = new BatteryLevel.Listener() {
	    public void receive(Measurement measurement) {
	    	final BatteryLevel battery = (BatteryLevel) measurement;
	        StarterActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	mVehicleBatteryLevel.setText(
	                    "Battery level (% remains): " + battery.getValue().doubleValue());
	            }
	        });
	    }
	};
	
	ChargingStatus.Listener mChargingListener = new ChargingStatus.Listener() {
	    public void receive(Measurement measurement) {
	    	final ChargingStatus status = (ChargingStatus) measurement;
	    	
	    	System.out.print("Have SOC value.");

	    	System.out.print(status.getValue());
	        StarterActivity.this.runOnUiThread(new Runnable() {
	            public void run() {
	            	mVehicleChargingStatus.setText(
	                    "State of Change : " + status.getValue().toString());
	            }
	        });
	    }
	};
	
	Latitude.Listener mLatitudeListener =new Latitude.Listener() {
        public void receive(Measurement measurement) {
            final Latitude lat = (Latitude) measurement;
        	latitude = lat.getValue().doubleValue();

            mHandler.post(new Runnable() {
                public void run() {
                    mLatitudeView.setText("" + latitude);
                }
            });
        }
    };

    Longitude.Listener mLongitudeListener = new Longitude.Listener() {
        public void receive(Measurement measurement) {
            final Longitude lng = (Longitude) measurement;
        	longitude= lng.getValue().doubleValue();

            mHandler.post(new Runnable() {
                public void run() {
                    mLongitudeView.setText("" + longitude);
                    currentCoord = new LatLng(latitude, longitude);
                    /*mMap.addMarker(new MarkerOptions().position(currentCoord).title("Test"));*/
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentCoord, 15));

                }
            });	
        }
    };
	
}