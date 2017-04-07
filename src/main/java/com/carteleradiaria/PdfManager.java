package com.carteleradiaria;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

/**
 * Created by kaotiks on 10/08/16.
 */

public class PdfManager {

    // Variables
    private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
    private String patternHourDouble = "\r\n[0-9][0-9]:";
    private String patternHourSimple = "\r\n[0-9]:";
    private FileHandler fileHandler = new FileHandler();
    private PDFParser parser;
    private PDFTextStripper pdfStripper;
    private PDDocument pdDoc ;
    private COSDocument cosDoc ;
    private String Text ;
    private File file;

    // todo: variables Nuevas



    // Constructor
    public PdfManager() {}


    // Esto lee el PDF en un solo String
    private String toText() throws IOException
    {
        this.pdfStripper = null;
        this.pdDoc = null;
        this.cosDoc = null;

        parser = new PDFParser(new FileInputStream(file));

        parser.parse();
        cosDoc = parser.getDocument();
        pdfStripper = new PDFTextStripper();
        pdDoc = new PDDocument(cosDoc);
        pdDoc.getNumberOfPages();
        //pdfStripper.setStartPage(1);
        //pdfStripper.setEndPage(1);

        // if you want to get text from full pdf file use this code
        // pdfStripper.setEndPage(pdDoc.getNumberOfPages());

        Text = pdfStripper.getText(pdDoc);
        return Text;
    }

    public void setFile(File file){
        this.file = file;
    }

    public Sede getAnalizedData(String sedeNombre, String fecha){
        this.file = fileHandler.downloadFile(sedeNombre,fecha,sedeNombre);
        //todo : varibles nuevas;
        Sede sede = new Sede();

        try{
            String file = stripDiacritics(this.toText().toLowerCase());
            // todo: return
            this.cleanSource(file);
            return sede;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private Map<String,String> cleanSource(String source){
        //Map<Integer,String> source1 = analizePdfCartelera(source.substring(source.indexOf("22:00")+5));
        //Map<String,String> result = analizePdfCartelera(source.substring(source.indexOf("22:00")+5));
        try {
          return analizePdfCartelera(source.substring(source.indexOf("22:00")+5));
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }

        // todo
    }

    // Parsea la info del PDF de las carteleras diarias
    private Map<String,String> analizePdfCartelera(String s){
        Sede sede = new Sede();
        String source = "";
        String[] aux1;
        Map<String,String> aulas = new TreeMap<String, String>();
        //Map<Integer, String> aulas = new TreeMap<Integer, String>();

        for (int i=0 ; i < s.length() ; i++) {
            Character c = s.charAt(i);

            try {
                if(c == '\n')
                    continue;

                if (esAula(s,i)) {
                    source += " ";
                    continue;
                }

                if(s.substring(i, i +16).equals("\r\nlaboratorio de")
                        || s.substring(i, i +10).equals("\r\n1er piso")){
                    source += c;
                    continue;
                }

                source += c;
            } catch (Exception e) {
                while(i < s.length()){
                    if(s.charAt(i) == '\n' || s.charAt(i) == '\r') {
                        i += 1;
                        continue;
                    }
                    source += s.charAt(i);
                    i += 1;
                }
            }
        }

        aux1 = source.split("\r");

        String key = null;
        //Integer key = null;
        String value = "";

        for(int i=0 ; i < aux1.length ; i++){
            String s1 = aux1[i].trim();

            if (s1.trim().equals(""))
                continue;
            if (esAula(s1)){
                //key = toInteger(s1);
                if(s1.contains("sala de profesores")){
                    key = s1.substring(0,s1.indexOf(":"));
                    //key = toInteger(s1.substring(0,s1.indexOf(":")+1));
                    value = s1.substring(s1.indexOf(":")+2);
                    aulas.put(key,value);
                } else {
                    if(s1.contains("capacidad")){
                        if(s1.contains("(")){
                            key = s1.substring(0,s1.indexOf("(")).trim();
                        } else {
                            key = s1.substring(0,s1.indexOf("capacidad")).trim();
                        }
                        //key = toInteger(s1);
                        aulas.put(key,"");
                    } else {
                        key = s1.trim();
                        //key = toInteger(s1);
                        aulas.put(key,"");
                    }
                }
            } else {
                try {
                    if(esAula(aux1[i+1].trim()) || (esAula(aux1[i+2].trim())) && aux1[i+1].trim().equals("")){
                        value = value + s1;
                        aulas.put(key,aulas.get(key)+value);
                        key = null;
                        value = "";
                    } else {
                        value = value + s1 + " ";
                    }
                } catch (Exception e){
                    value = value + s1;
                    aulas.put(key,aulas.get(key)+value);
                    value = "";
                }
            }

        }

        // todo
        return aulas;
    }



    // Verifica que los ultimos 5 caracteres sean dígitos
    private Boolean lastDigits(String s){
        Boolean result = true;
        for (int i = s.length()-1 ; i > s.length()-2 ; --i){
            if(!Character.isDigit(s.charAt(i))){
                result = false;
            }
        }
        return result;
    }

    private String[] parseMateriaCodigo(String s){
        return new String[]{s.substring(0,s.length()-5),s.substring(s.length()-5,s.length())};
    }

    private Boolean ignorarLinea(String s){
        return s.contains("universidad")
                || s.contains("impreso")
                || s.contains("asignatura")
                || s.contains("habilitada")
                || s.contains("comisiones")
                || s.contains("cuatrimestre");
    }

    private Boolean esCursada(String s){
        return s.toLowerCase().contains("semanal")
                || s.toLowerCase().contains("quincenal")
                || s.toLowerCase().contains("mensual")
                || s.toLowerCase().contains("junin")
                || s.toLowerCase().contains("pergamino");
    }

    private static String stripDiacritics(String str) {
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
        return str;
    }

    private String cleanComiciones(String s){
        String[] aux = s.split("\r");
        String result = "";

        for(int i=0 ; i < aux.length ; i++){
            result = result + aux[i].substring(aux[i].indexOf("-")+1);

        }

        return null;
    }

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
                || s.substring(i, i +15).equals("\r\ninformatica i");
    }

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
                || s.contains("profesores");
    }

    private Integer toInteger(String s){

        switch (s){
            case "aula parlante  “raul alfonsin”  planta baja":
                return 100;
            case "sala de profesores:":
                return 101;
            case "laboratorio de informatica i planta baja":
                return 102;
            case "laboratorio de informatica ii planta  baja":
                return 103;
            case "1er piso videoconferencia sala 1":
                return 104;
            case "1er piso videoconferencia sala 2":
                return 105;
        }

        try {
            return Integer.valueOf(s.substring(0,2));
        } catch (Exception e){
            return Integer.valueOf(s.substring(0,1));
        }
    }

    private String[] parseMateriaHorario(String s){
        String[] aux = s.split("hs.");
        Map<String,String> mH = new HashMap<>();

        for (int i=0 ; i < aux.length ; i++){
            String a = aux[i].substring(0,aux[i].indexOf("hs.")).trim();
            //String b = aux[i].substring()
        }

        return null;
    }

}

/* todo: esto de las aulas mas vale dejarlo para testing
private final int[] AULAS_RIVADAVIA = {8,9,11,13,15,18,19,20,21,31,32,33,34,35,36,37,38}; // Falta aula parlante
private final int[] AULAS_SARMIENTO = {1,2,3,4,5,6,7,8,10,11,12,13,15,16,17,18,19};
private final int[] AULAS_MONTEAGUDO = {1,2,3,4,5,6,7,8,9,10,11}; // faltan aulas sin numero
private final int[] AULAS_ECANA = {};
private final int[] AULAS_INTA = {};*/
