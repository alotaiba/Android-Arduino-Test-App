package com.mawqey.arduino.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ArduinoTest extends Activity implements SensorEventListener {
    /** Called when the activity is first created. */
	Socket connectionSocket = null;
	PrintWriter writeOut = null;
	SensorManager sensorManager = null;
	private TextView xValueView;
	private TextView yValueView;
	private TextView zValueView;
	private EditText ipEditText;
	private EditText portEditText;
	private Button openCommButton;
	private Button closeCommButton;
	int accelerometerOldVal = -1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        xValueView = (TextView) findViewById(R.id.XValue);
        yValueView = (TextView) findViewById(R.id.YValue);
        zValueView = (TextView) findViewById(R.id.ZValue);
        ipEditText = (EditText) findViewById(R.id.IPText);
        portEditText = (EditText) findViewById(R.id.PortText);
        openCommButton = (Button) findViewById(R.id.OpenComm);
        closeCommButton = (Button) findViewById(R.id.CloseComm);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //accelerometerOldVal = -1;
        
        openCommButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
            	connectArduino();
			}
        });
        
        closeCommButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				disconnectArduino();
			}
        });
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }
    
    private void connectArduino() {
	    connectionSocket = null;
	    try {
	    	InetAddress serverAddr = InetAddress.getByName(ipEditText.getText().toString());
	    	connectionSocket = new Socket(serverAddr, Integer.valueOf(portEditText.getText().toString()));
	    	writeOut = new PrintWriter(connectionSocket.getOutputStream(), true);
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
    }
    
    private void disconnectArduino() {
    	try {
			connectionSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void sendArduinoValue(float value) {
    	if (value > 0.1) {
        	int intValue = (int)Math.floor(value);
        	int delta = intValue - accelerometerOldVal;
        	if (Math.abs(delta) > 0) {
        		accelerometerOldVal = intValue;
        		//Log.d("Y", Integer.toString(intValue));
        		if (connectionSocket != null) {
            		writeOut.print(Integer.toString(intValue));
            	    writeOut.flush();
        		}
        	}
    	}
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			switch (event.sensor.getType()){
				case Sensor.TYPE_ACCELEROMETER:
					float x = event.values[0];
					float y = event.values[1];
					float z = event.values[2];
					xValueView.setText(Float.toString(x));
					yValueView.setText(Float.toString(y));
					zValueView.setText(Float.toString(z));
					sendArduinoValue(y);
				break;
			}
		}
	}
}