package com.carteleradiaria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class CarteleraDiariaApplication {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// El nombre de cada sede según aparece en la URL de los pdf
	private String[] sedes = {"anexo","evaperon","monteagudo","matilde","ecana","inta"};
	// La lista donde van a ir a parar todas las cursadas de Junin
	private List<Cursada> junin;
	// La lista donde van a ir a parar todas las cursadas de Pergamino
	private List<Cursada> pergamino;

	public static void main(String[] args) {
		SpringApplication.run(CarteleraDiariaApplication.class, args);
	}

	@Scheduled(fixedDelay=10000)
	public void start() {
		logger.info("-------------------------------------------");
		logger.info("\u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620");
		// Reseteamos las listas con cada ejecución
		this.junin = new ArrayList<>();
		this.pergamino = new ArrayList<>();
		// La fecha actual. La formateamos a ddMMYYYY para usarla en la URL de los pdf
		Date date = new Date();
		PdfManager pdfManager = new PdfManager();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMYYYY");

		// Analizamos el PDF de cada sede
		for (String s : sedes) {
			// Creamos un objeto Sede con todas las cursadas del día por aula
			//Sede sede = pdfManager.analizeSedeData(s,simpleDateFormat.format(date));
			Sede sede = pdfManager.analizeSedeData(s,"09092017");
			// Actualizamos las listas Junin/Pergamino según corresponda
			actualizarListas(sede);
			// Actualizamos la base de datos en Firebase
			actualizarFirebase();
		}
		//actualizarListas(pdfManager.analizeSedeData("anexo","09092017"));
	}

	/**
	 *  Actualiza la lista de cada ciudad
	 *
	 *  @param sede	Un objeto sede con todas las cursadas por aula de una sede
	 */
	public void actualizarListas(Sede sede) {
		Cursada cursada;
		String hora = "";
		String nombre = "";
		String aula;

		// Recorremos todas las aulas
		for (Map.Entry entry : sede.cursadas.entrySet()) {
			// Guardamos las cursadas del aula
			String value = entry.getValue().toString();

			// Si no hay cursadas, no analizamos nada
			if (!value.isEmpty()) {
				// Creamos array de cursadas
				for (String s : value.split("&")) {
					try {
						// Separamos nombre y hora de cada cursada
						nombre = s.substring(0,s.indexOf(":00")-2).trim();
						hora = s.substring(nombre.length()+1).trim();
					} catch (Exception e) {
						try {
							// Separamos nombre y hora de cada cursada
							nombre = s.substring(0,s.indexOf("hs.")-2).trim();
							hora = s.substring(nombre.length()+1,nombre.length()+4).trim();
						} catch (Exception e1) {
							//e.printStackTrace();
						}
						//e.printStackTrace();
					}

					aula = entry.getKey().toString();
					cursada = new Cursada(sede.getNombre(),hora,nombre,aula);

					if (!cursada.getNombre().isEmpty()) {
						if (sede.getCiudad().equals("Junin")) {
							junin.add(cursada);
						} else {
							pergamino.add(cursada);
						}
					}
				}
			}
		}
	}

	/**
	 *  Actualiza la base de datos en Firebase
	 *
	 */
	public void actualizarFirebase() {

	}
}
