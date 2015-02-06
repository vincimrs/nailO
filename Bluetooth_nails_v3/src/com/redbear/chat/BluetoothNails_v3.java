package com.redbear.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.Instrumentation;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BluetoothNails_v3 extends Activity {
	private final static String TAG = BluetoothNails_v3.class.getSimpleName();

	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
	private TextView tv = null;
	private EditText et = null;
	private Button btn = null;
	private String mDeviceName;
	private String mDeviceAddress;
	private RBLService mBluetoothLeService;
	private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

	int [] CapSenseChannels = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	int [] CapSenseChannelsOrdered = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
	Paint myPaint = new Paint();
	DrawView drawView;
	
	
	dataProcessor dataBuffer  = new dataProcessor(); 
	
	
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((RBLService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
			} else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				getGattService(mBluetoothLeService.getSupportedGattService());
			} else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second);

		tv = (TextView) findViewById(R.id.textView);
		tv.setMovementMethod(ScrollingMovementMethod.getInstance());
		et = (EditText) findViewById(R.id.editText);
		btn = (Button) findViewById(R.id.send);
		
        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        setContentView(drawView);
        
        
		btn.setOnClickListener(new OnClickListener() {

			
			@Override
			public void onClick(View v) {
				BluetoothGattCharacteristic characteristic = map
						.get(RBLService.UUID_BLE_SHIELD_TX);

				String str = et.getText().toString();
				byte b = 0x00;
				byte[] tmp = str.getBytes();
				byte[] tx = new byte[tmp.length + 1];
				tx[0] = b;
				for (int i = 1; i < tmp.length + 1; i++) {
					tx[i] = tmp[i - 1];
				}

				characteristic.setValue(tx);
				mBluetoothLeService.writeCharacteristic(characteristic);

				et.setText("");
			}
		});

		Intent intent = getIntent();

		mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
		mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent gattServiceIntent = new Intent(this, RBLService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		
		
	  //  final View touchView = findViewById();
	    drawView.setOnTouchListener(new View.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            	Log.d("touch", String.valueOf(event.getX()) + " x " + String.valueOf(event.getY()));
	                return true;
	        }
	    });
		
	}//end on create

	@Override
	protected void onResume() {
		super.onResume();

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();

			System.exit(0);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();

		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();

		System.exit(0);
	}

	private void displayData(byte[] byteArray) {
		if (byteArray != null) {
			String data = new String(byteArray);
			
			//Log.d("data", data);
			//Log.d("data", "size array: " + Integer.toString(byteArray.length));
			
			if (data != null && data.charAt(0) == 'S' && byteArray.length == 20) {
				//data = data.trim(data);
				CapSenseChannels[0] = byteArray[2] & 0xFF ; 
				CapSenseChannels[1]= byteArray[4] & 0xFF ; 
				CapSenseChannels[2]= byteArray[6] & 0xFF ;  
				CapSenseChannels[3]= byteArray[8] & 0xFF ;  
				CapSenseChannels[4]= byteArray[10] & 0xFF ;  
				CapSenseChannels[5]= byteArray[12] & 0xFF ;  
				CapSenseChannels[6]= byteArray[14] & 0xFF ;  
				CapSenseChannels[7]= byteArray[16] & 0xFF ;  
				CapSenseChannels[8]= byteArray[18] & 0xFF ; 
				
				//rearange the data
				CapSenseChannelsOrdered[0] = CapSenseChannels[0]; 
				CapSenseChannelsOrdered[1] = CapSenseChannels[5]; 
				CapSenseChannelsOrdered[2] = CapSenseChannels[8]; 
				CapSenseChannelsOrdered[3] = CapSenseChannels[1]; 
				CapSenseChannelsOrdered[4] = CapSenseChannels[3]; 
				CapSenseChannelsOrdered[5] = CapSenseChannels[7]; 
		    	CapSenseChannelsOrdered[6] = CapSenseChannels[2]; 
		    	CapSenseChannelsOrdered[7] = CapSenseChannels[4]; 
		    	CapSenseChannelsOrdered[8] = CapSenseChannels[6]; 
				
				
				String DataToPrint = Integer.toString(CapSenseChannels[0]) + "," +  Integer.toString(CapSenseChannels[1]) + "," + 
						 Integer.toString(CapSenseChannels[2]) + "," +  Integer.toString(CapSenseChannels[3]) 
						  + "," +  Integer.toString(CapSenseChannels[4])  + "," +  Integer.toString(CapSenseChannels[5]) 
						   + "," +  Integer.toString(CapSenseChannels[6])  + "," +  Integer.toString(CapSenseChannels[7]) 
								   + "," +  Integer.toString(CapSenseChannels[8]) ; 
				Log.d("data", DataToPrint); 
					
				tv.setText(DataToPrint);
				drawView.changeColor(CapSenseChannels);
				
				
				
				//Log.d("test", Integer.toString(maxValue(CapSenseChannelsOrdered))); 
				int maxChannel = maxValue(CapSenseChannelsOrdered);
				if (maxChannel == 1) { 
					drawView.displayGesture("UP");
				}
				else if (maxChannel == 3) { 
					drawView.displayGesture("LEFT");	
				}
				else if (maxChannel == 5) { 
					drawView.displayGesture("RIGHT");					
				}
				else if (maxChannel ==7) {
					drawView.displayGesture("DOWN");
				}
				else if(maxChannel ==4) { 
					drawView.displayGesture("PRESS");
				}
				
				
				
				
				setContentView(drawView);
				
				dataBuffer.add(CapSenseChannels); 
				

				 
				
			}
		
		

			//run this code in the terminal 
			//cd /Users/adementyev/Downloads/adt-bundle-mac-x86_64-20140321/sdk/platform-tools
			//./adb logcat -s data > "/Users/adementyev/Documents/LogCatOutput.txt"
			// find the amount we need to scroll. This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			
			/*
			final int scrollAmount = tv.getLayout().getLineTop(
					tv.getLineCount())
					- tv.getHeight();
			// if there is no need to scroll, scrollAmount will be <=0
			if (scrollAmount > 0)
				tv.scrollTo(0, scrollAmount);
			else
				tv.scrollTo(0, 0);				
				*/
			
		}//end if not null
	}//end function

	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;

		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
		map.put(characteristic.getUuid(), characteristic);

		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		mBluetoothLeService.setCharacteristicNotification(characteristicRx,
				true);
		mBluetoothLeService.readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();

		
		
		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);

		return intentFilter;
	}
	
	private static int maxValue(int[] chars) {
		int max = chars[0];
		int index = 0 ;
		for (int ktr = 0; ktr < chars.length; ktr++) {
			if (chars[ktr] > max) {
				max = chars[ktr];
				index = ktr;
				
			}
		}
		if (max<240) { 
			index = 0; 
		}
		return index;
	}
	

}
