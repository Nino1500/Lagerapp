package com.example.lagerapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Controller for controlling database access
 */
public class DBController {

    private String user;
    private String pass;
    private String host;
    private int port;
    private String database;
    private Context context;
    private ResultSet resultSet;

    private Connection connection;



    public DBController(String user, String pass, String host, int port, String database, Context context) throws SQLException, ClassNotFoundException {
        this.user = user;
        this.pass = pass;
        this.host = host;
        this.port = port;
        this.database = database;
        this.context = context;
        openConnection();
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
        }
        Class.forName("com.mysql.jdbc.Driver");

        Properties properties = new Properties();
        properties.setProperty("user", this.user);
        properties.setProperty("password", this.pass);
        properties.setProperty("MaxPooledStatements", "200");

        connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database+"?autoReconnect=true&useSSL=false", properties);

    }
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
        if(resultSet != null){
            resultSet.close();
            resultSet = null;
        }
    }
    public Statement getStatement() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                openConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return connection.createStatement();
    }
    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                openConnection();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return connection.prepareStatement(sql);
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTable(){ //TODO: ARTICLE is not necc now because fo resolve method so delete it ffs
        String query = "CREATE TABLE IF NOT EXISTS entities (ean VARCHAR(20), article VARCHAR(40), name VARCHAR(40), amount INTEGER, location VARCHAR(40))";
        try {
            openConnection();

            PreparedStatement preparedStatement = getPreparedStatement(query);
            preparedStatement.execute();
            preparedStatement.close();

            /*query = "CREATE TABLE IF NOT EXISTS resolve (ean VARCHAR(40), article VARCHAR(40), name VARCHAR(40))";

            openConnection();

            //statement = connection.createStatement();
            preparedStatement = getPreparedStatement(query);
            preparedStatement.execute();
            preparedStatement.close();*/

            query = "CREATE TABLE IF NOT EXISTS locations (location VARCHAR(40))";

            openConnection();

            preparedStatement = getPreparedStatement(query);
            preparedStatement.execute();
            preparedStatement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean insertLocation(String location){
        ArrayList<String> list = new ArrayList<>();
        list = getLocations();

        if(list.contains(location)){
            return false;
        }

        String query = "INSERT INTO locations(location) VALUES(?)";

        try {
            openConnection();

            PreparedStatement preparedStatement = getPreparedStatement(query);
            preparedStatement.setString(1, location);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<String> getLocations(){
        String query = "SELECT * FROM locations";

        ArrayList<String> list = new ArrayList<>();
        try {
            openConnection();

            PreparedStatement preparedStatement = getPreparedStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                list.add(resultSet.getString("location"));
            }

            preparedStatement.close();
            return list;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean deleteLoc(String location){
        try {
            deleteEntities(location);
            openConnection();
            String query = "delete from locations where location = ?";
            PreparedStatement preparedStatement = getPreparedStatement(query);
            preparedStatement.setString(1, location);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertEntity(LEntity entity, String location){ //Object into database
        ArrayList<LEntity> list = new ArrayList<>();

        String query = "select * from entities";

        boolean hasArt = false;
        PreparedStatement preparedStatement = null;
        try {
            openConnection();
            preparedStatement = getPreparedStatement(query);
            preparedStatement.executeQuery();
            resultSet = preparedStatement.getResultSet();
            while (resultSet.next()){
                if(resultSet.getString("ean").equalsIgnoreCase(entity.getEan()) && !resultSet.getString("article").equalsIgnoreCase("")){
                    hasArt=true;
                    break;
                }
            }
            preparedStatement.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(!hasArt){
            try {
                BufferedReader reader = null;
                reader = new BufferedReader(new InputStreamReader(context.getAssets().open("EAN.TXT"), StandardCharsets.UTF_8));
                String mLine;
                while((mLine = reader.readLine()) != null){
                    String[] splits = mLine.split("    ");
                    if(splits.length>1){
                        if(splits[0].equalsIgnoreCase(entity.getEan())){
                            entity.setArticle(splits[1]);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        list = getEntities(location);

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getEan().equalsIgnoreCase(entity.getEan()) && list.get(i).getLocation().equalsIgnoreCase(location)){
                query = "update entities set amount = ? where ean = ? AND location = ?";
                try {
                    openConnection();
                    preparedStatement = getPreparedStatement(query);
                    int am = list.get(i).getAmount();
                    am++;
                    preparedStatement.setInt(1, am);
                    preparedStatement.setString(2, list.get(i).getEan());
                    preparedStatement.setString(3, location);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        query = "INSERT INTO entities (ean, article, name, amount, location) VALUES (?, ?, ?, ?, ?)";
        try {
            openConnection();

            preparedStatement = getPreparedStatement(query);
            preparedStatement.setString(1, entity.getEan());
            preparedStatement.setString(2, entity.getArticle());
            preparedStatement.setString(3, entity.getName());
            preparedStatement.setInt(4, entity.getAmount());
            preparedStatement.setString(5, entity.getLocation());
            preparedStatement.executeUpdate();
            preparedStatement.close();

            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteEntity(LEntity entity, String location){
        String query = "";
        ArrayList<LEntity> list = getEntities(location);
        int am = 0;
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getEan().equalsIgnoreCase(entity.getEan()) && list.get(i).getLocation().equalsIgnoreCase(location)){
                am = list.get(i).getAmount();
                if(am>=2){
                    am--;
                    query = "update entities set amount = ? where ean = ? AND location = ?";
                    try {
                        openConnection();
                        PreparedStatement preparedStatement = getPreparedStatement(query);
                        preparedStatement.setInt(1, am);
                        preparedStatement.setString(2, list.get(i).getEan());
                        preparedStatement.setString(3, location);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        return true;
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if(am==1){
                    query = "update entities set amount = ? where ean = ? AND location = ?";
                    try {
                        openConnection();
                        PreparedStatement preparedStatement = getPreparedStatement(query);
                        preparedStatement.setInt(1, 0);
                        preparedStatement.setString(2, list.get(i).getEan());
                        preparedStatement.setString(3, location);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        return true;
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        return false;
    }
    public boolean deleteEntities(String location){ //Delete all from one location
        ArrayList<LEntity> list = getEntities(location);
        String query = "";
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).getLocation().equalsIgnoreCase(location)){
                query = "delete from entities where location = ?";
                try {
                    openConnection();

                    PreparedStatement preparedStatement = getPreparedStatement(query);
                    preparedStatement.setString(1, location);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public ArrayList<LEntity> getEntities(){ //get all entities to display in list
        ArrayList<LEntity> list = new ArrayList<>();
        String query = "select * from entities";

        try {
            openConnection();

            PreparedStatement preparedStatement = getPreparedStatement(query);
            resultSet = preparedStatement.executeQuery(query);

            while(resultSet.next()){

                list.add(new LEntity(resultSet.getString("ean"), resultSet.getString("article"), resultSet.getString("name"), resultSet.getInt("amount"), resultSet.getString("location")));

            }

            closeConnection();

            preparedStatement.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }
    public ArrayList<LEntity> getEntities(String location){

        ArrayList<LEntity> list = new ArrayList<>();
        String query = "select * from entities";

        try {
            openConnection();

            PreparedStatement preparedStatement = getPreparedStatement(query);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){

                if(resultSet.getString("location").equalsIgnoreCase(location)){

                    list.add(new LEntity(resultSet.getString("ean"), resultSet.getString("article"), resultSet.getString("name"), resultSet.getInt("amount"), resultSet.getString("location")));

                }
            }

            closeConnection();
            preparedStatement.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return list;
    }

}
