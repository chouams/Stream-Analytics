package com.mycompany.app;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class ReadPropertyFile {

    public static void main( String[] args ) throws IOException, URISyntaxException {
        InputStream input=ReadPropertyFile.class.getClassLoader().getResourceAsStream("src/com/data/config.properties");
    System.out.println("ch");
    }
}
