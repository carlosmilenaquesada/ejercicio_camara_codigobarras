package com.example.ejercicio_camara_codigobarras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity{
	Button btnScan;
	EditText txtResultado;
	Socket socket;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread(){
			@Override
			public void run(){
				//Creo el socket de conexion (para conectar el teléfono con el servidor)
				int puertoDelServidor = 10000;
				String direccionDelServidor = "192.168.0.4";
				try{
					socket = new Socket(direccionDelServidor, puertoDelServidor);
				}catch(IOException e){
					throw new RuntimeException(e);
				}
			}
		}.start();
		btnScan = findViewById(R.id.btnScan);
		txtResultado = findViewById((R.id.txtResultado));
		btnScan.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				IntentIntegrator integrador = new IntentIntegrator(MainActivity.this);
				//Define el tipo de código de de barras que se pretenden scanear.
				//En este caso, voy a elegir cualquiera (QR, EAN13, etc)
				integrador.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
				//Promp en la pantalla de lector
				integrador.setPrompt("Lector - CDP");
				//Cámara que va a usarse (delantera, trasera, etc)
				//0 es trasera
				integrador.setCameraId(0);
				//beep de sonido al escanear
				integrador.setBeepEnabled(true);
				//
				integrador.setBarcodeImageEnabled(true);
				//bloquea/desbloquea la orientación del teléfono (he tenido que agregar lo
				// siguiente al manifest:)
				/*  <activity
					android:name="com.journeyapps.barcodescanner.CaptureActivity"
					android:screenOrientation="fullSensor"
					tools:replace="screenOrientation" />* */
				integrador.setOrientationLocked(false);
				//inicializa el scaneo
				integrador.initiateScan();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if(result != null){
			if(result.getContents() == null){
				Toast.makeText(this, "Lectora cancelada", Toast.LENGTH_LONG).show();
			}else{

				Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
				txtResultado.setText(result.getContents());
				new Thread(){
					@Override
					public void run(){
						ObjectOutputStream oos = null;
						try{
							oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(result.getContents());
						}catch(IOException e){
							throw new RuntimeException(e);
						}
					}
				}.start();
			}
		}else{
			super.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}