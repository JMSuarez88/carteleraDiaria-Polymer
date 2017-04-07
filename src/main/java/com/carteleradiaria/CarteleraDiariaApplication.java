package com.carteleradiaria;

import com.sun.deploy.net.HttpUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.commons.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class CarteleraDiariaApplication {

	private String currentDate;
	private String url;
	private Map<String,String[]> sedes = new HashMap();
	private UploadObject uploadObject = new UploadObject();
	private PdfManager pdfManager = new PdfManager();

	public static void main(String[] args) {
		SpringApplication.run(CarteleraDiariaApplication.class, args);
	}

	public String getCurrentDate() {
		// todo
		return "";
	}

	public void setupUploadObject() {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
		this.currentDate = sdf.format(dt);
		// todo: esto me puede llegar a traer problemas con nullPointerException!!
		Ciudad ciudad = null;

		for (Map.Entry<String, String[]> entry : this.sedes.entrySet()) {
			ciudad = new Ciudad(entry.getKey());
			List<Sede> sedes = new ArrayList<>();
	    for (String sede : entry.getValue()) {
				if (this.isCarteleraAvailable(sede)) {
					try {
						//sedes.add(this.pdfManager.getAnalizedData(sede,this.currentDate));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			ciudad.setSedes(sedes);
		}
	}

	public boolean isCarteleraAvailable(String sede) {
		// todo
		return true;
	}

	public void analizeSede(String sede) {

	}

	private void updateFirebase() {

	}

	@org.junit.Test
	public void test() {
		this.pdfManager.getAnalizedData("anexo","10032017");

		/*File f;
		try {
			URLConnection con = new URL("https://www.unnoba.edu.ar/cursadas/archivo/anexo09032017.pdf").openConnection();
			InputStream in = con.getInputStream();
			String encoding = con.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			String body = IOUtils.toString(in, encoding);
			System.out.println(body);
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	}
}
