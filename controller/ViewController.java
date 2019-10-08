/**
 * @author Parth Patel
 */


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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;



public class ViewController {


	@FXML public AnchorPane mainAnchorPane;	
	@FXML private Button cancel;
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
	@FXML ListView <String> listview = new ListView<String>();



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
	 * 
	 * When the save button is pressed this method activates,
	 * it makes new object, inserts the new song in correct order
	 * and then clears the text (input) fields.
	 */
	@FXML
	public void saveButton(ActionEvent event) throws IOException  {

		songObj newSong = new songObj("","","","");
		newSong = makeNewObject(newSong);

		if(newSong == null) {
			return;
		}


		insertNewSong(newSong);

		// Check if the user is editing or adding song.
		// If editing then remove the current song from the listview and add the new one.
		if(counter == 0) {
			removeFromList();
		}

		clearTextFeilds();
	}



	public void insertNewSong(songObj toInsert) {

		int size = songList.size();

		if(size == 0) {
			songList.add(toInsert);
			listview.getItems().add(toInsert.name + " , " + toInsert.artist);
			return;
		}


		int i, temp =0;
		// Adding the new song in alphabetical order.
		mainloop:
			for(i=0; i<size; i++) {

				if(toInsert.name.compareToIgnoreCase(songList.get(i).name) < 0) {
					toInsertHelper(i , toInsert);
					return;
				}

				if ((songList.get(i).name.compareToIgnoreCase(toInsert.name) == 0) && 
						(songList.get(i).artist.compareToIgnoreCase(toInsert.artist) ==0)) {

					if(counter == 0) {
						counter = -1;
						return;
					} else {
						Alert alert = new Alert(AlertType.WARNING ,  "Song already EXISTS", ButtonType.OK);
						alert.showAndWait();
						return;

					}
				}

				// If the name is same then we compare artist
				// If artist is also same then it means that this is a
				// duplicate song and therefore we throw an warning.
				temp = i;
				while( (temp < size) && (songList.get(i).name.compareToIgnoreCase(toInsert.name) == 0)){

					if(songList.get(i).artist.compareToIgnoreCase(toInsert.artist) ==0) {
						Alert alert = new Alert(AlertType.WARNING ,  "Song already EXISTS", ButtonType.OK);
						alert.showAndWait();
						return;
					}

					if(toInsert.artist.compareToIgnoreCase(songList.get(i).artist) < 0) {
						toInsertHelper(temp , toInsert);
						return;
					}
					temp++;
					if(temp == i) {
						break mainloop;
					}
				}
			}


		songList.add(toInsert);
		listview.getItems().add(toInsert.name + " , " + toInsert.artist);
	}




	/**
	 * @param toMake object
	 * @return object
	 * 
	 * This method takes all the info about the song from the text fields
	 * and makes an song object out of it.
	 */
	public songObj makeNewObject(songObj toMake) {

		if(artistTextField.getText().isEmpty() || nameTextField.getText().isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING ,  "Name or Artist of the song is MISSING", ButtonType.OK);
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				clearTextFeilds();
				return null;
			}
		}

		if(!artistTextField.getText().isEmpty() && !nameTextField.getText().isEmpty()) {
			toMake.name = nameTextField.getText();
			toMake.artist = artistTextField.getText();
		}

		if(!albumTextField.getText().isEmpty()) {
			toMake.album = albumTextField.getText();
		}
		if(!yearTextField.getText().isEmpty()) {
			toMake.year = yearTextField.getText();
		}
		return toMake;
	}


	/**
	 * @param event
	 * 
	 * This method deletes the song (Confirms with the user before deleting)  
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

	// helper method for delete
	public void deleteSongHelper() {

		songObj target = getTargetObject();
		int i = songList.indexOf(target);
		songList.remove(target);
		removeFromList();		

		if(listview.getSelectionModel().isEmpty()) {
			nameDetail.setText("");
			artistDetail.setText("");
			albumDetail.setText("");
			yearDetail.setText("");
		} else {
			if(i < songList.size()) {
				listview.getSelectionModel().select(i);
				viewDetailsHelper();
			}
			if(i == songList.size()) {
				if(i-1 >= 0) {
					listview.getSelectionModel().select(i-1);
					viewDetailsHelper();
				}
			}
		}
	}



	
	/**
	 * @param event
	 * 
	 * This method runs when the user presses the EDIT button (Confirms with the user if they are
	 * sure they want to edit a song)
	 */
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

		songObj targetObj = getTargetObject();

		nameTextField.setText(targetObj.name);
		artistTextField.setText(targetObj.artist);
		albumTextField.setText(targetObj.album);
		yearTextField.setText(targetObj.year);

		songObj newSong = new songObj("","","","");
		newSong = makeNewObject(newSong);


		insertNewSong(newSong);
	}


	
	/**
	 * @param int i
	 * @param  songObj toInsert
	 * 
	 * This method inserts the songObj in correct orders in arraylist
	 * and listview
	 */
	public void toInsertHelper(int i, songObj toInsert) {
		songList.add(i, toInsert);
		listview.getItems().add(i, toInsert.name + " , " + toInsert.artist);
	}


	
	/**
	 * @param event
	 * 
	 * This method runs when the user presses the cancel button (To exit any event midway)
	 */
	@FXML
	void cancelButton(ActionEvent event) {
		clearTextFeilds();
	}





	/**
	 * @return song Object
	 * 
	 * Gets the target object that has to be either edited or deleted
	 */
	public songObj getTargetObject() {
		String [] words = (listview.getSelectionModel().getSelectedItem().toString()).split("\\s*,\\s*");
		String name = words[0];
		String artist = words[1];

		songObj targetObj = null;

		int size = songList.size();

		for(int i =0; i<size; i++) {
			if((name.equals(songList.get(i).name)) && (artist.equals(songList.get(i).artist)) ) {
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


	
	/**
	 * @param event
	 * 
	 * This method views all the details about the song
	 */
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


	/**
	 * This method removes the selected song from the listview
	 */
	public void removeFromList() {

		if(listview.getSelectionModel().isEmpty()) {
			return;
		}
		listview.getItems().remove((listview.getSelectionModel().getSelectedItem().toString()));
	}




	
	/**
	 * @return
	 * 
	 * This method checks if the database already exists. 
	 * (File where we store the all the song info) 
	 */
	public static boolean databaseAlreadyExists() {
		File f = new File("DataBase.txt");
		boolean exists = f.exists();
		return exists;
	}


	
	
	/**
	 *  When the user closes the app
	 *  we call this method to store the data in to Database.txt file
	 *  
	 * (As our text cases will be smaller we just made the file again)
	 */
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


	
	/**
	 * This method fills the arraylist of objects with all the data
	 * from the database.txt when the app runs again after closing.
	 */
	public void retrieveDataFromFile() {
		try {
			String line;

			FileReader file = new FileReader("DataBase.txt");
			BufferedReader bufferedReader = new BufferedReader(file);

			while ((line = bufferedReader.readLine()) != null ) {
				String[] temp = line.split("\\t");

				//  ADD TO ARRAYLIST 
				if(temp.length == 2) {
					songObj tempObj = new songObj(temp[0], temp[1], "", "");
					songList.add(tempObj);
					listview.getItems().add(tempObj.name + " , " + tempObj.artist);
				}

				if(temp.length >= 3) {
					songObj tempObj = new songObj(temp[0], temp[1], temp[2], temp[3]);
					songList.add(tempObj);
					listview.getItems().add(tempObj.name + " , " + tempObj.artist);
				}	
			}
			bufferedReader.close();
			file.close();
			if(!songList.isEmpty()) {
				selectFirstSong();
				viewDetailsHelper();
			}

		}
		catch(IOException i) {
			i.printStackTrace();
		}
	}


}


