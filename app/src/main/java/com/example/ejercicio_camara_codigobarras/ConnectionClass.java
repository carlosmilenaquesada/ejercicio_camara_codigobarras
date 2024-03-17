package com.example.ejercicio_camara_codigobarras;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass{
	//ip del host al que se va a conectar (si se prueba en el simulador, tiene que ser el "10.0.2.2" o la puerta de enlace del simulador, si no, es la ip del equipo servidor (si están en la misma red))
	public static final String HOSTDB = "192.168.0.4";//"10.0.2.2";
	public static final String NOMBREDB = "tienda_carlosmilena";
	public static final String USUARIODB = "root";
	public static final String CLAVEDB = "";
	private static final String OPCIONESHORA = "?useUnicode=true&useJDBCCompliantTimezoneShift" +
											   "=true&useLegacyDatetimeCode=false&serverTimezone" +
											   "=UTC";
	// las opciones de hora tambien las puedes poner en mysql
	// SET GLOBAL time_zone = '+1:00';
	public static final String PUERTOMYSQL = "3306";
	public static final String URLMYSQL =
			"jdbc:mysql://" + HOSTDB + ":" + PUERTOMYSQL + "/" + NOMBREDB + OPCIONESHORA;

	public static Connection conectarConBaseDeDatos(){
		try{

			Connection conexion = DriverManager.getConnection(URLMYSQL, USUARIODB, CLAVEDB);
			return conexion;
		}catch(Exception e){
			System.out.println(e);
			throw new RuntimeException(e);

		}
	}
}
