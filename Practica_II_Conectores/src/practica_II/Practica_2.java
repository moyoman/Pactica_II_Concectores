package practica_II;

import java.sql.*;
import java.util.*;

public class Practica_2 {

	private static Scanner entrada = new Scanner(System.in);
	private static ArrayList<String>nombreColumnas = new ArrayList<String>();
	private static ArrayList<String>nombreTipoColumnas = new ArrayList<String>();
	private static ArrayList<Integer>tipoColumnas = new ArrayList<Integer>();
	private static int numColumnas;

	public static void identificarColumnas(Connection conexion,Statement sentencia,ResultSetMetaData resultadoMetadatos) throws SQLException {

		numColumnas = resultadoMetadatos.getColumnCount();
		for(int i=1;i<=numColumnas;i++) {

			nombreColumnas.add(resultadoMetadatos.getColumnName(i));
			nombreTipoColumnas.add(resultadoMetadatos.getColumnTypeName(i));
			tipoColumnas.add(resultadoMetadatos.getColumnType(i));
		}
	}

	public static String generarUpdate(ArrayList<String>sentencias) {

		String update = "INSERT INTO emple ("; 
		int j = nombreColumnas.size()-1;
		for(int i=0;i<j;i++) {
			update += nombreColumnas.get(i) + ",";
		}
		update += nombreColumnas.get(j) + ") VALUES (";

		for(int i=0;i<j;i++) {
			if(tipoColumnas.get(i) == 6 || tipoColumnas.get(i) == 4 || tipoColumnas.get(i) == 2 || tipoColumnas.get(i) == 8) {
				update += sentencias.get(i) + ",";
			}
			else {				
				update += "'" + sentencias.get(i) + "',";
			}
		}
		update += "CURDATE());";
		return update;
	}

	public static boolean mostrarDatos(ArrayList<String>sentencias) {
		if((sentencias.size()+1) == numColumnas) {
			System.out.println("Los datos introducidos son:");
			for(int i=0;i<sentencias.size();i++) {				
				System.out.println("\t" + nombreColumnas.get(i) + ":" + sentencias.get(i));
			}
			return true;
		}				
		return false;
	}

	public static boolean comprobarCodigo(Connection conexion,Statement sentencia, String codigo) throws SQLException {

		ResultSet resultado = sentencia.executeQuery("SELECT * FROM emple WHERE codigo=" + codigo +";");

		int numeroResultados = 0;

		while(resultado.next()) {
			numeroResultados++;
		}

		if(numeroResultados > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean comprobarDepartamento(Connection conexion,Statement sentencia, String codigo) throws SQLException{

		ResultSet resultado = sentencia.executeQuery("SELECT * FROM departamento WHERE codigo=" + codigo +";");

		int numeroResultados = 0;

		while(resultado.next()) {
			numeroResultados++;
		}

		if(numeroResultados > 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void main(String[] args) {

		String opcion = "";
		int filasAfectadas = 0;

		try {
			Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost/empleados","vespertino","password");
			Statement sentencia = conexion.createStatement();
			String consulta ="SELECT * FROM emple";
			ResultSetMetaData resultadoMetadatos = sentencia.executeQuery(consulta).getMetaData();


			try {
				identificarColumnas(conexion, sentencia, resultadoMetadatos);

			}catch(SQLException e) {
				e.printStackTrace();
			}

			System.out.println("Hola, ¿Desea introducir un nuevo empleado en la Base de Datos? (si/no)");
			opcion = entrada.nextLine();

			while(!opcion.equalsIgnoreCase("no")) {
				if(opcion.equalsIgnoreCase("si")){

					ArrayList<String>sentencias = new ArrayList<String>();
					for(int i=0;i<nombreColumnas.size()-1;i++) {

						System.out.println("Introduzca " + nombreColumnas.get(i) + " de tipo " + nombreTipoColumnas.get(i));
						String dato = entrada.nextLine();
						if(dato.length() != 0) {
							sentencias.add(i,dato);
							switch(i) {
								case 0:
									if(comprobarCodigo(conexion, sentencia, sentencias.get(i))) {
										System.out.println("Ya existe el código introducido.");
										sentencias.remove(i);
										i--;
									}
								break;
								case 5:
									try {
										double salario = Double.parseDouble(dato);
										if(salario <= 0) {
											System.out.println("El salario no puede ser menor o igual a 0");
											sentencias.remove(i);
											i--;
										}
									} catch(NumberFormatException e) {
										System.out.println("Formato no válido");
										sentencias.remove(i);
										i--;
									}
								break;
								case 6:
									if(!comprobarDepartamento(conexion, sentencia, sentencias.get(i))) {
										System.out.println("No existe el código de departamento introducido");
										sentencias.remove(i);
										i--;
									}
								break;
							}
						}	
						else {
							System.out.println("Debe introducir algún dato");
							i--;
						}
					}
					if(mostrarDatos(sentencias)) {

						System.out.println("¿Son correctos los datos introducidos? (si/no)");
						opcion = entrada.nextLine();
						while(!opcion.equalsIgnoreCase("si") && !opcion.equalsIgnoreCase("no")) {
							System.out.println("¿Son correctos los datos introducidos? (si/no)");
							opcion = entrada.nextLine();
						}
					}
					else {
						System.out.println(sentencias.toString());
						System.out.println(numColumnas);
					}
					if(opcion.equalsIgnoreCase("si")) {
						try {
							String update = generarUpdate(sentencias); 
							System.out.println(update);
							filasAfectadas += sentencia.executeUpdate(update);	

						} catch(IndexOutOfBoundsException e) {
							System.out.println("Lo sentimos, no se pudo realizar la operación");
						}
						catch(SQLIntegrityConstraintViolationException e) {
							System.out.println("Lo sentimos, no se pudo realizar la operación");
						}
					}								

					System.out.println("¿Desea introducir un nuevo empleado en la Base de Datos?");
					opcion = entrada.nextLine();
				}
				else {
					System.out.println("Opción incorrecta, pruebe otra vez (si/no)");
					opcion = entrada.nextLine();
				}
			}
			System.out.println("Registros introducidos: " + filasAfectadas);

		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("Gracias por usar el programa, Hasta pronto.");
		entrada.close();

	}
}
