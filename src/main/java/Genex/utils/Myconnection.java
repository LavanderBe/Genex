package Genex.utils;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Myconnection {
    private static Myconnection Instance;

    private String url="jdbc:mysql://localhost:3306/forum";
    private String login="root";
    private String pwd="php123";

    private  Connection cnx;
    public Connection getCnx() {
        return cnx;
    }

    private Myconnection(){
        try {
            cnx=DriverManager.getConnection(url,login,pwd);
            System.out.println("connection établie !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static Myconnection getInstance (){
        if (Instance==null)
        {
            Instance=new Myconnection();
        }
        return Instance;
    }
}
