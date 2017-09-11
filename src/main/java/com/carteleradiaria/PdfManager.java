package com.carteleradiaria;

import java.io.IOException;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by kaotiks on 10/08/16.
 */

public class PdfManager {

    // Variables
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    private final String path = "http://www.unnoba.edu.ar/cursadas/archivo/";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // Constructor
    public PdfManager() {}

    /**
     *  Busca el pdf en la URL que pasamos como parametro y lo devuelve como String
     *
     *  @param url   La URL del pdf que queremos convertir a String
     *  @return      Un String con la data del pdf
     */
    private String toText(String url) throws IOException
    {
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            PDDocument pdDoc = PDDocument.load(new URL(url));
            String textResult = pdfStripper.getText(pdDoc);
            pdDoc.close();

            return textResult;
        } catch (Exception e) {
            logger.error("Archivo pdf inexistente");
            return "";
        }
    }

    /**
     *  Desde acá se manda a analizar cada PDF según sede
     *
     *  @param sedeNombre   Nombre de la sede según pdf (anexo, evaperon, etc)
     *  @param fecha        La fecha actual en formato ddMMYYYY (ej.: 10092017)
     *  @return             Un objeto Sede con todas las cursadas por aula
     */
    public Sede analizeSedeData(String sedeNombre, String fecha){
        String pdfText;
        Map<String,String> sedeData = new HashMap<>();
        Sede sede = new Sede(sedeNombre);
        // Logueamos cada sede que analizamos
        logger.info("-------------------------------------------");
        logger.info("SEDE: " + sede.getNombre());
        logger.info("CIUDAD: " + sede.getCiudad());

        try {
            // Pasamos a String el pdf
            pdfText = this.toText(path + sedeNombre + fecha + ".pdf");
            // Limpiamos el string quitando acentos y demases
            pdfText = stripDiacritics(pdfText.toLowerCase());

            if (!pdfText.isEmpty()) {
                // Mandamos a analizar el String
                sedeData = this.analizePdf(pdfText);
                logger.info("OK ;)");
            } else {
                logger.error("No hay datos para analizar");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sede.setCursadas(sedeData);
        return sede;
    }

    /**
     *  Desde acá se manda a analizar cada PDF
     *
     *  @param source   El pdf que convertimos a String
     *  @return         Un Map con todas las cursadas por aula
     */
    private Map<String,String> analizePdf(String source){
        try {
          return analizePdfCartelera(source.substring(source.indexOf("22:00")+5));
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
    }

    /**
     *  Parsea la info del PDF de las carteleras diarias
     *
     *  @param s   El String que se va a parsear
     *  @return    Un Map con todas las cursadas por aula
     */
    private Map<String,String> analizePdfCartelera(String s){
        String source = "";
        String[] aux1;
        Map<String,String> aulas = new TreeMap<String, String>();

        // Empezamos recorriendo el String completo, caracter por caracter
        // La idea es ir pasandolo en limpio a otro String, quitando caracteres que no queremos
        for (int i=0 ; i < s.length() ; i++) {

            Character c = s.charAt(i);

            try {
                // A este no lo queremos
                if(c == '\n')
                    continue;

                // Verifica si a continuación del caracter actual viene el nombre de algún aula
                if (esAula(s,i)) {
                    // Pasamos un espacio antes del nombre del aula al nuevo String
                    source += " ";
                    continue;
                }

                // Esto debe ser necesario para no perder el nombre completo del aula
                if (s.substring(i, i +16).equals("\r\nlaboratorio de")
                        || s.substring(i, i +10).equals("\r\n1er piso")){
                    // Pasamos el caracter al nuevo String
                    source += c;
                    continue;
                }

                // Pasamos el caracter al nuevo String
                source += c;
            } catch (Exception e) {
                // Nos agarro la Exception, estamos al final del String original
                while(i < s.length()){
                    // Estos dos caracteres no los queremos pasar al final del String
                    if(s.charAt(i) == '\n' || s.charAt(i) == '\r') {
                        i += 1;
                        continue;
                    }
                    // Pasamos el caracter al nuevo String
                    source += s.charAt(i);
                    i += 1;
                }
            }

        }

        // Creamos un array spliteando el String que pasamos en limpio
        aux1 = source.split("\r");

        // La 'key' del Map
        String aula = null;
        // El 'value' del Map
        String value = "";

        // Recorremos el array que creamos recién
        // En este punto es donde se empieza a hacer toda el parseo para armar el Map que tenemos que devolver
        for(int i=0 ; i < aux1.length ; i++){

            String s1 = aux1[i].trim();

            // Si el elemento actual es un String vacío, lo ignoramos
            if (s1.trim().equals(""))
                continue;
            // Si es el nombre de un aula, analizamos y agregamos el aula
            if (esAula(s1)){
                if(s1.contains("sala de profesores")){
                    aula = s1.substring(0,s1.indexOf(":"));
                    value = s1.substring(s1.indexOf(":")+2);
                    aulas.put(aula,value);
                } else {
                    if(s1.contains("capacidad")){
                        if(s1.contains("(")){
                            aula = s1.substring(0,s1.indexOf("(")).trim();
                        } else {
                            aula = s1.substring(0,s1.indexOf("capacidad")).trim();
                        }
                        aulas.put(aula,"");
                    } else {
                        aula = s1.trim();
                        aulas.put(aula,"");
                    }
                }
            // Si no es un aula, analizamos y terminamos de cargar los datos de las cursadas
            } else {
                try {
                    if(esAula(aux1[i+1].trim()) || (esAula(aux1[i+2].trim())) && aux1[i+1].trim().equals("")){
                        if (s1.contains(":00") || s1.contains(":30") || s1.contains("hs.")) {
                            value = value + s1 + "&";
                        } else {
                            value = value + s1 + " ";
                        }
                        aulas.put(aula,aulas.get(aula)+value);
                        aula = null;
                        value = "";
                    } else {
                        if (s1.contains(":00") || s1.contains(":30") || s1.contains("hs.")) {
                            value = value + s1 + "&";
                        } else {
                            value = value + s1 + " ";
                        }
                    }
                } catch (Exception e){
                    if (s1.contains(":00") || s1.contains(":30") || s1.contains("hs.")) {
                        value = value + s1 + "&";
                    } else {
                        value = value + s1 + " ";
                    }
                    aulas.put(aula,aulas.get(aula)+value);
                    value = "";
                }
            }

        }

        return aulas;
    }

    /**
     *  Verifica si el String es o forma parte del nombre de un aula
     *
     *  @param s    El String que se va a analizar
     *  @param i    El índice actual dentro de la iteración. Lo usamos para hacer algunas verificaciones
     *  @return     true/false
     */
    private Boolean esAula(String s,int i){
        return s.substring(i, i + 6).equals("\r\nplan")
                || (s.substring(i, i + 6).equals("\r\n1er.") && (Character.isDigit(s.charAt(i-1)) || Character.isDigit(s.charAt(i-2))))
                || (s.substring(i, i + 5).equals("\r\n1er") && (Character.isDigit(s.charAt(i-1)) || Character.isDigit(s.charAt(i-2))))
                || s.substring(i, i + 6).equals("\r\n2do.")
                || s.substring(i, i + 5).equals("\r\n2do")
                || s.substring(i, i + 6).equals("\r\n3er.")
                || s.substring(i, i + 5).equals("\r\n3er")
                || s.substring(i, i + 6).equals("\r\nvide")
                || s.substring(i, i + 6).equals("\r\nsala")
                || (s.substring(i, i + 13).equals("\r\nlaboratorio") && (Character.isDigit(s.charAt(i-1)) || Character.isDigit(s.charAt(i-2)) || s.substring(i-5,i).equals("piso ") || s.substring(i-5,i).equals("baja ")))
                || s.substring(i, i + 12).equals("\r\n(capacidad")
                || s.substring(i, i + 11).equals("\r\ncapacidad")
                || s.substring(i, i + 9).equals("\r\nalumnos")
                || s.substring(i, i + 15).equals("\r\nfisicoquimica")
                || s.substring(i, i + 13).equals("\r\ninformatico")
                || s.substring(i, i + 7).equals("\r\n“raul")
                || s.substring(i, i + 9).equals("\r\nsala de")
                || s.substring(i, i +15).equals("\r\ninformatica i")
                || s.substring(i, i +18).equals("\r\narquitectura y") && s.substring(i-12).equals("laboratorio ")
                || s.substring(i, i +21).equals("\r\nelectronica digital");
    }

    /**
     *  Verifica si el String es o forma parte del nombre de un aula
     *
     *  @param s    El String que se va a analizar
     *  @return     true/false
     */
    private Boolean esAula(String s){
        return s.contains("planta")
                || s.contains("1er.")
                || s.contains("1er")
                || s.contains("2do.")
                || s.contains("2do")
                || s.contains("3er.")
                || s.contains("3er")
                || s.contains("video")
                || s.contains("sala")
                || s.contains("laboratorio")
                || s.contains("capacidad")
                || s.contains("alumnos")
                || s.contains("fisicoquímica")
                || s.contains("informático")
                || s.contains("alfonsin")
                || s.contains("profesores")
                || s.contains("arquitectura y")
                || s.contains("electronica digital");
    }

    /**
     *  Limpia un String de acentos y ese tipo de cosas
     *
     *  @param str  El String que se va a limpiar
     *  @return     El mismo String, pero limpio
     */
    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }

}