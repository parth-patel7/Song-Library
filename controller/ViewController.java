package controller;



import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;



/**
 * @author Parth Patel
 */
public class ViewController {



	@FXML public AnchorPane mainAnchorPane;	
	@FXML private Label nameDetail;	
	@FXML private Label artistDetail;	
	@FXML private Label albumDetail;	
	@FXML private Label yearDetail;

	@FXML private TextField nameTextField;
	@FXML private TextField artistTextField;
	@FXML private TextField albumTextField;
	@FXML private TextField yearTextField;


	/**
	 * As the save button is common for both adding and editing song,
	 * a global variable counter is made which is turned to 1 when edit 
	 * user is editing a song and then resets to -1.
	 */
	private int counter = -1;

	public static ArrayList <songObj> songList = new ArrayList <songObj>();
	private ObservableList <String> obslist = FXCollections.observableArrayList();
	@FXML
	ListView <String> listview = new ListView<String>();



	/**
	 * @stage links listview with Arraylist of song objects
	 */
	public void start(Stage stage) {  
		listview.setItems(obslist); 
		listview.getSelectionModel().select(0);
	}

	@FXML
	public void initialize() {
		if(databaseAlreadyExists() == true) {	
			retrieveDataFromFile();	
		}	

	}





	/**
	 * @param event
	 * @throws IOException
	 */
	@FXML
	public void saveButton(ActionEvent event) throws IOException  {

		songObj newSong = new songObj("","","","");

		
		// As the name and artist are required we check if the related fields are empty
		// if yes then warning alert and exit. 
		if(artistTextField.getText().isEmpty() || nameTextField.getText().isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING ,  "Name or Artist of the song is MISSING MotherFUcker", ButtonType.OK);
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				clearTextFeilds();
				return;
			}
		}

		// Info requirements are met and thus adding new song to playlsit.
		
		if(!artistTextField.getText().isEmpty() && !nameTextField.getText().isEmpty()) {
			newSong.name = nameTextField.getText();
			newSong.artist = artistTextField.getText();
		}

		if(!albumTextField.getText().isEmpty()) {
			newSong.album = albumTextField.getText();
		}
		if(!yearTextField.getText().isEmpty()) {
			newSong.year = yearTextField.getText();
		}

		insertNewSong(newSong);

		// Check if the user is editing or adding song.
		// If editing then remove the current song from the listview and add the new one.
		if(counter == 0) {
			removeFromList();	
			counter = -1;
		}

		clearTextFeilds();
	}









	/**
	 * @param event
	 */
	@FXML
	void deleteSong(ActionEvent event) {

		if(listview.getSelectionModel().isEmpty()) {
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to DELETE this song?", ButtonType.YES,  ButtonType.NO);
		alert.showAndWait();
		if (alert.getResult() == ButtonType.NO) {
			return;
		}

		deleteSongHelper();
	}


	public void deleteSongHelper() {

		songObj target = getTargetObject();
		songList.remove(target);
		removeFromList();		

		if(listview.getSelectionModel().isEmpty()) {
			nameDetail.setText("");
			artistDetail.setText("");
			albumDetail.setText("");
			yearDetail.setText("");
		} else {
			// After deletion next song should be displayed xx
		}
	}



	//When the use cancels midway the object is still deleted
	@FXML
	void editSong(ActionEvent event) {

		counter = 0;

		if(listview.getSelectionModel().isEmpty()) {
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to EDIT this song?", ButtonType.YES,  ButtonType.NO);
		alert.showAndWait();
		if (alert.getResult() != ButtonType.YES) {
			return;
		}

		String [] words = (listview.getSelectionModel().getSelectedItem().toString()).split("\\s*,\\s*");
		String name = words[0];


		int size = songList.size();

		for(int i =0; i<size; i++) {
			if(name.equals(songList.get(i).name)) {
				if(words.length > 1) {
					if(words[1].toString().equals(songList.get(i).artist)) {
						// Match found in song object array list

						songObj targetObj = songList.get(i);

						nameTextField.setText(targetObj.name);
						artistTextField.setText(targetObj.artist);
						albumTextField.setText(targetObj.album);
						yearTextField.setText(targetObj.year);

						// you remove the obj here but what if someone calls cancel here
						songList.remove(targetObj);

						// get the new data and swap it with older data

						songObj newSong = new songObj("","","","");

						if(!nameTextField.getText().isEmpty()) {
							newSong.name = nameTextField.getText();
						} else {
							// Throw Error
						}

						if(!artistTextField.getText().isEmpty()) {
							newSong.artist = artistTextField.getText();
						} else {
							// Throw Error
						}

						if(albumTextField.getText() != null) {
							newSong.album = albumTextField.getText();
						}

						if(yearTextField.getText() != null) {
							newSong.year = yearTextField.getText();
						}

						break;
					}
				}
			}
		}
	}


	// Finish this	(CHECK FOR SAME SONGS)
	public void insertNewSong(songObj toInsert) {

		int size = songList.size();

		System.out.println("Size = "+ size);

		if(size == 0) {
			songList.add(toInsert);
			listview.getItems().add(toInsert.name + " , " + toInsert.artist);
			return;
		}

		// Adding the new song in alphabetical order.
		/*
		for(int i=0; i<size; i++) {
			if(toInsert.name.compareToIgnoreCase(songList.get(i).name) < 0) {
				toInsertHelper(i , toInsert);
				return;
			}
			else if (songList.get(i).name.compareToIgnoreCase(toInsert.name) == 0) {
				// check next name if it is same
				// if yes then compare artist and insert
				int temp = i;
				while( (temp < size) && (songList.get(i).name.compareToIgnoreCase(toInsert.name) == 0)){
					if(toInsert.artist.compareToIgnoreCase(songList.get(i).artist) < 0) {
						toInsertHelper(temp , toInsert);
						return;
					}
					temp++;
				}
				toInsertHelper(temp , toInsert);
				return;
			}
		}
		 */
		songList.add(toInsert);
		listview.getItems().add(toInsert.name + " , " + toInsert.artist);
	}


	public void toInsertHelper(int i, songObj toInsert) {
		songList.add(i, toInsert);
		listview.getItems().add(i, toInsert.name + " , " + toInsert.artist);
	}
	


	@FXML
	void cancelButton(ActionEvent event) {
		clearTextFeilds();
	}


	
	
	// checks for file
	public static boolean databaseAlreadyExists() {
		File f = new File("DataBase.txt");
		boolean exists = f.exists();
		return exists;
	}


	public songObj getTargetObject() {

		String [] words = (listview.getSelectionModel().getSelectedItem().toString()).split("\\s*,\\s*");
		String name = words[0];

		songObj targetObj = null;

		int size = songList.size();

		for(int i =0; i<size; i++) {
			if(name.equals(songList.get(i).name)) {
				if(words.length > 1) {
					if(words[1].toString().equals(songList.get(i).artist)) {
						targetObj = songList.get(i);
						break;
					}
				}
			}
		}
		return targetObj;
	}


	@FXML
	void viewDetails(MouseEvent event) {

		if(listview.getSelectionModel().isEmpty()) {
			return;
		}

		nameDetail.setText("");
		artistDetail.setText("");
		albumDetail.setText("");
		yearDetail.setText("");
		viewDetailsHelper();
	}


	public void viewDetailsHelper() {

		String [] words = (listview.getSelectionModel().getSelectedItem().toString()).split("\\s*,\\s*");
		String name = words[0];

		int size = songList.size();
		for(int i =0; i<size; i++) {
			if(name.equals(songList.get(i).name)) {
				if(words.length > 1) {
					if(words[1].toString().equals(songList.get(i).artist)) {
						// Match found in array list

						songObj targetObj = songList.get(i);

						nameDetail.setText(targetObj.name);
						artistDetail.setText(targetObj.artist);	
						albumDetail.setText(targetObj.album);
						yearDetail.setText(targetObj.year);
					}
				}
			}
		}

	}


	public void selectFirstSong() {
		listview.getSelectionModel().select(0);
	}


	public void clearTextFeilds() {
		nameTextField.clear();
		artistTextField.clear();
		albumTextField.clear();
		yearTextField.clear();
	}


	public void removeFromList() {

		if(listview.getSelectionModel().isEmpty()) {
			return;
		}
		listview.getItems().remove((listview.getSelectionModel().getSelectedItem().toString()));
	}



	public static void  storeDataToFile () {
		try {
			Writer writer = new FileWriter("DataBase.txt");
			int size = songList.size();
			for(int i =0; i<size; i++) {
				writer.write(songList.get(i).name + "\t" + songList.get(i).artist + "\t" +
						songList.get(i).album + "\t" + songList.get(i).year + "\t"+ "\n");
			}
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}



	// EDIT THIS ASAP
	public void retrieveDataFromFile() {
		try {
			String line;

			FileReader fileReader = new FileReader("DataBase.txt");
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null ) {
				String[] temp = line.split("\\t");

				//  ADD TO ARRAYLIST 
				/*
				if (temp.length == 5) {
					if (temp[4].equals("\n")) {
						songObj tempObj = new songObj(temp[0], temp[1], temp[2], temp[3]);
						songList.add(tempObj);
					}
				}
				 */

			}

			bufferedReader.close();
			fileReader.close();
		}
		catch(IOException i) {
			i.printStackTrace();
		}
	}

}


