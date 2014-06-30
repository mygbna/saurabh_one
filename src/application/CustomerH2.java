/*
 ***********************************************************************************************************************************
This JavaFx project makes a desktop application with BackEnd on H2 database.
This application shows the name of all the customer who are registered in "CUSTOMER" database;For this you have to click on "SEARCH"
button on application.
The "CLEAR SCREEN"button clears all data on application window.
However the project could have been expanded for more column entries rather "name" only but i was having problem in extracting data from 
TableView and also i had limited time,so i kept it simple by using list view.   
*************************************************************************************************************************************
  
  ======================================>>>>>>>>>>>>>>SAURABH SINGH<<<<<<<<<<<<<==============================================================
  
*/



package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class CustomerH2 extends Application {
  private static final Logger logger = Logger.getLogger(CustomerH2.class.getName());
  private static final String[] Jugado_data = { "saurabh", "manas", "rohit", "praveen" }; //this data is used in case if table customer 
  //is not created in H2 database
  
  public static void main(String[] args) { launch(args); }

  @Override public void start(Stage stage) {
    final ListView<String> nameView = new ListView();

    final Button fetchNames = new Button("SEARCH");
    fetchNames.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent event) {
        GetNameFromDatabase(nameView);
      }
    });
    
    final Button clearNameList = new Button("CLEARSCREEN");
    clearNameList.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent event) {
        nameView.getItems().clear();
      }
    });

    VBox layout = new VBox(10);
    layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 15;");
    layout.getChildren().setAll(
      HBoxBuilder.create().spacing(10).children(
        fetchNames, 
        clearNameList    
      ).build(),      
      nameView
    );
    layout.setPrefHeight(400);
    
    stage.setScene(new Scene(layout));
    stage.show();
  }
  
  private void GetNameFromDatabase(ListView listView) {    //method used to get data(names) from database and gives it to List view.
    try (Connection con = getConnection()) {               // checks the connection of JDBC.
      if (!schemaExists(con)) {                            //checks whether schema of customer is already there in H2 database.
        createSchema(con);                                 //if not then create the schema using this method.
        fillDatabase(con);                                //database is populated with default data referred by jugado_data.
      }
      listView.setItems(fetchNames(con));                  //if customer schema is already there then fetch name and give it to list.
    } catch (SQLException | ClassNotFoundException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
  }

  private Connection getConnection() throws ClassNotFoundException, SQLException { //method for connecting the java application to database.
    logger.info("Getting a database connection");
    Class.forName("org.h2.Driver");
    
    return DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "saurabh", "mygbna");    
  }

  private void createSchema(Connection con) throws SQLException { //method for creating schema if it is not present in database. 
    logger.info("Creating schema");
    Statement st = con.createStatement();
    String table = "create table customer(id integer, name varchar(64))";
    st.executeUpdate(table);
    logger.info("Created schema");
  }

  private void fillDatabase(Connection con) throws SQLException { //fill database with default data.
    logger.info("Populating database");      
    Statement st = con.createStatement();      
    for (String name: Jugado_data) {
      st.executeUpdate("insert into customer values(1,'" + name + "')");
    }
    logger.info("Populated database");
  }
  
  private boolean schemaExists(Connection con) {
    logger.info("Checking for Schema existence");      
    try {
      Statement st = con.createStatement();      
      st.executeQuery("select count(*) from customer");
      logger.info("Schema exists");      
    } catch (SQLException ex) {
      logger.info("Existing DB not found will create a new one");
      return false;
    }
    
    return true;
  }

  private ObservableList<String> fetchNames(Connection con) throws SQLException {
    logger.info("Fetching names from database");
    ObservableList<String> names = FXCollections.observableArrayList();
    
    Statement st = con.createStatement();      
    ResultSet rs = st.executeQuery("select name from customer");
    while (rs.next()) {
      names.add(rs.getString("name"));
    }

    logger.info("Found " + names.size() + " names");
    
    return names;
  }
}
