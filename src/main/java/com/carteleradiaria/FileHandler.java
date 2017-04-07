package com.carteleradiaria;

import com.itextpdf.kernel.pdf.PdfReader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kaotiks on 11/08/16.
 */

public class FileHandler {


    private String path = "http://www.unnoba.edu.ar/cursadas/archivo/";
    private String folder = "temp/";
    private InputStream inputStreamObject;
    private PdfReader pdfReader;

    public FileHandler() {
    }

    public File downloadFile(String sede, String fecha, String namefile){
        //Creamos la carpeta en C:/pdfcursadas/
        createFolder();
        File file = null;
        InputStream in = null;
        OutputStream out = null;
        // Iniciamos la descarga del archivo
        try{
            URLConnection conn = new URL(path+sede+fecha+".pdf").openConnection();
            conn.connect();
            file = new File(folder + namefile+".pdf");
            in = conn.getInputStream();
            this.pdfReader = new PdfReader(in);
            this.inputStreamObject = in;
            out = new FileOutputStream(file);
            int b = 0;
            while (b != -1) {
                b = in.read();
                if (b != -1)
                    out.write(b);
            }
        }catch (Exception e){
            System.out.println("Error al encontrar archivo");
        }finally {
            if(out != null && in != null) {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }
    private void createFolder(){
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    System.out.println("Error");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public InputStream getInputStreamObject() {
        return inputStreamObject;
    }

    public PdfReader getPdfReader() {
        return pdfReader;
    }
}
