package com.example.ejercicio_camara_codigobarras;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity{
	Button btnScan;
	Button btnMostrar;
	TextView txtResultado;
	TextView txtMostrar;
	Socket socket;
	ConnectionClass connectionClass;
	Connection connection;
	ResultSet rs;
	String nombre;
	String precio;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		connectionClass = new ConnectionClass();
		connect();
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
		btnMostrar = findViewById(R.id.btnMostrar);
		txtResultado = findViewById(R.id.txtResultado);
		txtMostrar = findViewById(R.id.txtMostrar);
		btnScan.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				IntentIntegrator integrador = new IntentIntegrator(MainActivity.this);
				//Define el tipo de código de de barras que se pretenden scanear.
				//En este caso, voy a elegir códigos de barras PRODUCT_CODE_TYPES(los que
				// normalmente
				// usan los productos comerciales UPC_A, UPC_E, EAN_8, EAN_13, RSS_14)
				integrador.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
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
		btnMostrar.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				ExecutorService executorService = Executors.newSingleThreadExecutor();

				executorService.execute(() -> {
					try{
						connection = connectionClass.conectarConBaseDeDatos();
						String query = "Select nombrejuego, preciojuego from " +
									   "carlosmilena_juegos where (`identificador` = ?);";
						PreparedStatement ps = connection.prepareStatement(query);
						ps.setString(1, (String) txtResultado.getText());
						rs = ps.executeQuery();

						while(rs.next()){
							nombre = rs.getString("nombrejuego");
							precio = rs.getString("preciojuego");
						}
						MainActivity.this.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								txtMostrar.setText(nombre + " " + precio);

							}
						});


					}catch(Exception e){
						System.out.println(e);
						throw new RuntimeException(e);
					}
				});
			}
		});
	}

	public void connect(){
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(() -> {
			connection = connectionClass.conectarConBaseDeDatos();
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