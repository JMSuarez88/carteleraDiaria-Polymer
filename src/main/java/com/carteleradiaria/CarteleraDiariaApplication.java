package com.carteleradiaria;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.database.*;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
@EnableScheduling
public class CarteleraDiariaApplication {
	private static DatabaseReference ref;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// El nombre de cada sede según aparece en la URL de los pdf
	private String[] sedes = {"anexo","evaperon","monteagudo","matilde","ecana","inta"};
	// La lista donde van a ir a parar todas las cursadas de Junin
	private List<Cursada> junin;
	// La lista donde van a ir a parar todas las cursadas de Pergamino
	private List<Cursada> pergamino;
	// La fecha actual
	// La formateamos a ddMMYYYY para usarla en la URL de los pdf
	private Date date = new Date();
	private PdfManager pdfManager = new PdfManager();
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMYYYY");

	public static void main(String[] args) {
		SpringApplication.run(CarteleraDiariaApplication.class, args);
		inicializarFirebase();
	}

	// todo: esto está tirando un error: "No TaskScheduler/ScheduledExecutorService bean found for scheduled processing"
	@Scheduled(fixedDelay=10000)
	public void start() {
		logger.info("-------------------------------------------");
		logger.info("\u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620 \u2620");

		// Reseteamos las listas con cada ejecución
		this.junin = new ArrayList<>();
		this.pergamino = new ArrayList<>();

		// Analizamos el PDF de cada sede
		for (String s : sedes) {

			// Creamos un objeto Sede con todas las cursadas del día por aula
			Sede sede = pdfManager.analizeSedeData(s,simpleDateFormat.format(date));

			// Actualizamos las listas Junin/Pergamino según corresponda
			actualizarListas(sede);

		}

		// Actualizamos la base de datos en Firebase
		actualizarFirebase();
	}

	/**
	 *  Actualiza la lista de cada ciudad
	 *
	 *  @param sede	Un objeto sede con todas las cursadas por aula de una sede
	 */
	private void actualizarListas(Sede sede) {
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
	private void actualizarFirebase() {
		try {

			// Actualizamos campo Junin
			DatabaseReference fieldRef = ref.child("junin");
			Task<Void> task = fieldRef.setValue(junin);
			Tasks.await(task);
			// Actualizamos campo Pergamino
			fieldRef = ref.child("pergamino");
			task = fieldRef.setValue(pergamino);
			Tasks.await(task);
			// Actualizamos campo Fecha
			fieldRef = ref.child("fecha");
			simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY");
			//task = fieldRef.setValue(simpleDateFormat.format(date));
			task = fieldRef.setValue("09092017");
			Tasks.await(task);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void inicializarFirebase() {
		try {
			// Fetch the service account key JSON file contents
			FileInputStream serviceAccount = new FileInputStream("C:/Users/kaotiks/workspace/carteleraDiaria-Polymer/src/main/resources/cartelera-diaria-firebase-adminsdk-y808g-833ac159bd.json");

			// Datos de configuración
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
					.setDatabaseUrl("https://cartelera-diaria.firebaseio.com/")
					.build();

			// Inicializamos Firebase con los datos de nuestra base de datos
			FirebaseApp.initializeApp(options);

			// Referenciamos la base de datos que vamos a usar
			ref = FirebaseDatabase
					.getInstance()
					.getReference("cartelera");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
