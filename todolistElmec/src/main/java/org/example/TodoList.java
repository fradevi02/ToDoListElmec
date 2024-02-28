package org.example;

import static spark.Spark.*;
import java.sql.*;

public class TodoList {
    public static void main(String[] args) {

        final String DB_URL = "jdbc:sqlite:todo.db";

        //vengono creati database e tabella qualora non esistessero
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Il driver è " + meta.getDriverName());
            System.out.println("database creato con successo");

            String sql = "CREATE TABLE IF NOT EXISTS todolist (titolo TEXT, descrizione TEXT)";
            stmt.execute(sql);

            //nel caso ci sia una sqlexception stampa messaggio di errore in console
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //permette al client HTTP di inserire il titolo e la descrizione delle cose da fare
        post("/inserisci", (request, response) -> {
            //ottenere il valore dei parametri della richiesta HTTP
            String titolo = request.queryParams("titolo");
            String descrizione = request.queryParams("descrizione");
            // si utilizzano ? come segnaposto
            String sql = "INSERT INTO todolist(titolo, descrizione) VALUES(?,?)";
            //inserimento valori nel database nei segnaposto
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, titolo);
                pstmt.setString(2, descrizione);
                pstmt.executeUpdate();

                //nel caso ci sia una sqlexception stampa messaggio di errore in console
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            //da la conferma dell'inserimento al database
            return "ToDo aggiunto!";
        });

        //metodo per gestire le richieste GET
        get("/visualizza", (request, response) -> {
            String sql = "SELECT titolo, descrizione FROM todolist";
            //uso stringbuilder in modo che in seguito basta modificare il contenuto di essa
            StringBuilder todolist = new StringBuilder();

            try (Connection conn = DriverManager.getConnection(DB_URL);
                 Statement stmt  = conn.createStatement();
                 ResultSet rs    = stmt.executeQuery(sql)){
                todolist.append("<strong> TODO LIST: <br> </strong>");
                int i= 1;

                while (rs.next()) {

                    String num = Integer.toString(i++);

                    //ho usato br al posto di n in modo che html facesse il ritorno a capo e hr per separare
                    //per rendere più chiaro titolo e descrizione li ho messi in grassetto con strong e colorati
                    todolist.append("<p><strong style='color: rgb(0, 50, 153);'>").append(num).append(".").append("  Titolo: </strong>").append(rs.getString("titolo")).append("<br><strong style='color: rgb(0, 160, 153);'>Descrizione: </strong>").append(rs.getString("descrizione")).append("<br><hr><br>");

                }

                //nel caso ci sia una sqlexception stampa messaggio di errore in console
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            //ritorna la stringa convertendo todolist che era strinbuilder in string
            return todolist.toString();
        });
    }
}