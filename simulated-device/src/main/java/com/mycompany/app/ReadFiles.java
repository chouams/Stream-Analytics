package com.mycompany.app;

import javafx.beans.property.IntegerProperty;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.*;

public class ReadFiles {

    public static Map<String, String> readfile(String path){
        File file = new File(path);
        Scanner scanner= null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Map<String,String> stringDoubleHashMap =new HashMap<>();
        String[] arrayline=null;

        while (scanner.hasNext()){
            String line=scanner.nextLine();
            arrayline=line.split(":");
            String k=arrayline[0].replaceAll(" ","");

            String v = (arrayline[1]).replaceAll(" ","");
            //int value=Integer.parseInt(v);
            stringDoubleHashMap.put(k,v);
           // System.out.println(line);

        }
        scanner.close();

    return stringDoubleHashMap;}



   /* public static void main(String[] args) throws FileNotFoundException {
String path="C:\\Users\\chouams\\Desktop\\iot-java-get-started12\\simulated-device\\src\\main\\java\\com\\mycompany\\app\\wert.txt";
      Map cchouams=  readfile(path);
      Object c =  cchouams.get("minTemperature");
      System.out.println(cchouams.toString());
     System.out.println( cchouams.get("minTemperature".getClass()));

    }*/
}
