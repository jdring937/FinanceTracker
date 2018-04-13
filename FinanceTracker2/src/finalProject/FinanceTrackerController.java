package finalProject;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import com.sun.prism.impl.Disposer.Record;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Duration;

public class FinanceTrackerController implements Initializable {

	/**
	 * Groups the pieChart, and the associated labels (lblCaption and lblDate)
	 */
	@FXML
	private Group grpGroup;

	/**
	 * Generates the pieChart that is used for displaying transactions by category
	 */
	@FXML
	private PieChart pieChart;

	/**
	 * Label that displays the percentage value for each category when hovering on individual pie slice
	 */
	@FXML
	private Label lblCaption;

	/**
	 * Label that display "As of + current date" below pie chart
	 */
	@FXML
	private Label lblDate;

	/**
	 * Indicates status for new transactions and whether valid information was entered
	 */
	@FXML
	private Label lblStatus;

	/**
	 * ComboBox that displays the categorys retrieved from RDBMS
	 */
	@FXML
	private ComboBox<String> cboCategory;

	/**
	 * TextField for entering monetary value for new transactions
	 */
	@FXML
	private TextField tfAmount;

	/**
	 * TextField for entering location for new transactions
	 */
	@FXML
	private TextField tfLocation;

	/**
	 * Button used to submit new transactions to RDBMS
	 */
	@FXML
	private Button btnSubmit;

	/**
	 * DatePicker used to pick that date that new transactions took place
	 */
	@FXML
	private DatePicker dpDate;

	/**
	 * Label that goes above tableview. Displays "Transactions for + current month"
	 */
	@FXML
	private Label lblTransactions;

	/**
	 * DatePicker that allows user to specify beginning date when setting a custom date range for data displayed in tableview
	 */
	@FXML
	private DatePicker dpFromDate;

	/**
	 * DatePicker that allows user to specify end date when setting a custom date range for data displayed in tableview
	 */
	@FXML
	private DatePicker dpToDate;

	/**
	 * Sets visiblity of dpFromDate and dpToDate to visible and hides lblTransactions and self
	 * Allows users to access to setting custom date range for tableview items
	 */
	@FXML
	private Button btnEditDate;

	/**
	 * Used to go back to default tableview and cancel update of tableview item
	 */
	@FXML
	private Button btnBack;

	/**
	 * Used to go to default tableview and cancel setting a custom date range
	 */
	@FXML
	private Button btnCancel;

	/**
	 * Submits the custom date range and runs customPopulateTable method to allow custom date range
	 */
	@FXML
	private Button btnSubmitRange;

	/**
	 * Main tableview that displays transaction details from RDBMS monthly_finanances table
	 */
	@FXML
	private TableView<ObservableList> tableview;

	/**
	 * Becomes visible on action and allows user to enter data for RDBMS updates
	 */
	@FXML
	private HBox hbUpdate;

	/**
	 * Displays the transactionID in hbUpdate for selected tableview item
	 */
	@FXML
	private Label lblTransactionID;

	/**
	 * DatePicker used to select new date value when updating selected tableview item
	 */
	@FXML
	private DatePicker dpUpdateDate;

	/**
	 * Used to enter new location when updating selected tableview item
	 */
	@FXML
	private TextField tfUpdateLocation;

	/**
	 * Used to enter new amount when updating selected tableview item
	 */
	@FXML
	private TextField tfUpdateAmount;

	/**
	 * Selects new category when updating selected tableview item
	 */
	@FXML
	private ComboBox<String> cboUpdateCategory;

	/**
	 * Submits update data and runs DBUpdate();
	 */
	@FXML
	private Button btnSubmitChange;

	/**
	 * Returns tableview to default data of current month
	 */
	@FXML
	private Button btnReturn;

	/**
	 * Runs populateAllTime() to display all transactions in monthly_finances table
	 */
	@FXML
	private Button btnAllTime;

	/**
	 * Displays status of updated items and whether valid information was entered
	 */
	@FXML
	private Label lblUpdate;

	/**
	 * Runs filteredData() method on key released action event
	 * Searches ObservableList data based on user input
	 */
	@FXML
	private TextField tfSearch;

	/**
	 * Tableview to display returned search data from filteredData() 
	 */
	@FXML
	private TableView<ObservableList> tvSearch;

	/**
	 * Hides tvSearch and associated data by setting visibility to false
	 */
	@FXML
	private Button btnHideResults;

	/**
	 * Refreshes tableviews and piechart with default data for current month
	 */
	@FXML
	private Button btnRefresh;

	/**
	 * Establishes connection with RDBMS
	 */
	Connection conn;
	/**
	 * Stores the category name to return the name as an int value corresponding to RDBMS tCategory categoryID value
	 */
	int i;
	/**
	 * ObservableList that stores table data retrieved from RDBMS to be used for tableview
	 */
	private ObservableList<ObservableList> data;
	/**
	 * ObservableList that stores table data retrieved from RDBMS to be used for tvSearch
	 */
	private ObservableList<ObservableList> searchData;
	/**
	 * Sets date format to return as month name
	 */
	DateFormat month = new SimpleDateFormat("MMMM");
	/**
	 * Stores DateFormat month value to be used in lblTransactions
	 */
	String s = month.format(new Date());
	/**
	 * ObservableList that stores table data retrieved from RDBMS to be used for pieChart
	 */
	private ObservableList<PieChart.Data> pieChartData;
	/**
	 * ContextMenu that is displayed when tableview record is right clicked
	 */
	ContextMenu menu = new ContextMenu();
	/**
	 * ContextMenu item that deletes record from RDBMS
	 */
	MenuItem delete = new MenuItem("Delete");
	/**
	 * ContextMenu item that displays HBox with fields to update RDBMS record
	 */
	MenuItem update = new MenuItem("Update");

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		DateFormat todaysDate = new SimpleDateFormat("MM/dd/YYYY");
		Date today = new Date();
		dpDate.setPromptText(todaysDate.format(today));
		cboCategory.setValue("Select Category");
		loadCombo();
		populateTable();
		filteredData();
		populatePieChart();
		lblStatus.setMouseTransparent(true);
		menu.getItems().addAll(delete, update);
		lblCaption.setStyle("-fx-font-size: 12px; -fx-text-fill: #000000;");
		lblCaption.setVisible(false);
		btnSubmitChange.setStyle("-fx-border-radius: 50%;");

		// Refresh default data for tableviews and piechart by clearing and
		// repopulating
		btnRefresh.setOnAction(e -> {
			populateTable();
			filteredData();
			pieChart.getData().clear();
			populatePieChart();
			lblTransactions.setText("Transactions for " + s);
			btnBack.setVisible(false);
		});

		// Submit addition to DB and catch incorrect input
		btnSubmit.setOnAction(e -> {
			try {
				DBInsert();
				lblTransactions.setText("Transactions for " + s);
				cboCategory.setValue("Select Category");
				btnBack.setVisible(false);
			} catch (NumberFormatException | NullPointerException e1) {
				lblStatus.setVisible(true);
				lblStatus.setText("Invalid Input - Please fill out all fields");
				PauseTransition delay = new PauseTransition(Duration.seconds(5));
				delay.setOnFinished(event -> lblStatus.setVisible(false));
				delay.play();
			}
		});

		// Set custom data range for tableview
		btnEditDate.setOnAction(e -> {
			btnCancel.setVisible(true);
			btnEditDate.setVisible(false);
			dpFromDate.setVisible(true);
			dpToDate.setVisible(true);
			btnSubmitRange.setVisible(true);
			lblTransactions.setVisible(false);
			btnAllTime.setVisible(true);
		});

		// Return to default tableview and cancel custom date input
		btnCancel.setOnAction(e -> {
			btnCancel.setVisible(false);
			btnEditDate.setVisible(true);
			lblTransactions.setVisible(true);
			dpFromDate.setVisible(false);
			dpToDate.setVisible(false);
			btnSubmitRange.setVisible(false);
			btnBack.setVisible(true);
			dpFromDate.setValue(null);
			dpToDate.setValue(null);
			btnBack.setVisible(false);
			btnEditDate.setText("Custom");
			tableview.getColumns().clear();
			tvSearch.getColumns().clear();
			btnAllTime.setVisible(false);
			populateTable();

		});

		// populate tableview with all transactions recorded in DB
		btnAllTime.setOnAction(e -> {
			tableview.getColumns().clear();
			tvSearch.getColumns().clear();
			populateAllTime();
			filteredData();
			populatePieChartAllTime();
			btnAllTime.setVisible(false);
			lblTransactions.setVisible(true);
			lblTransactions.setText("Transactions for all time");
			btnBack.setVisible(true);
			btnEditDate.setVisible(true);
			btnSubmitRange.setVisible(false);
			dpFromDate.setVisible(false);
			dpToDate.setVisible(false);
			btnCancel.setVisible(false);
			lblCaption.setVisible(false);
		});

		// Submit query for custom date range and populate tableview with
		// transactions between those dates
		btnSubmitRange.setOnAction(e -> {
			lblTransactions.setVisible(true);
			dpFromDate.setVisible(false);
			dpToDate.setVisible(false);
			btnSubmitRange.setVisible(false);
			btnBack.setVisible(true);
			btnCancel.setVisible(false);
			btnEditDate.setVisible(true);
			lblTransactions.setText(
					"Transactions from " + dpFromDate.getValue().toString() + " to " + dpToDate.getValue().toString());
			tableview.getColumns().clear();
			tvSearch.getColumns().clear();
			customPopulateTable();
			filteredData();
			customPopulatePieChart();
			dpFromDate.setValue(null);
			dpToDate.setValue(null);
			lblCaption.setText("");
			btnAllTime.setVisible(false);
		});

		// Return to default tableview data
		btnBack.setOnAction(e -> {
			tableview.getColumns().clear();
			tvSearch.getColumns().clear();
			populateTable();
			filteredData();
			populatePieChart();
			btnBack.setVisible(false);
			lblTransactions.setText("Transactions for " + s);
			lblCaption.setVisible(false);

		});

		// Delete tableview items using menu items, refresh tableview with
		// correct info
		delete.setOnAction(e -> {
			DBDelete();
			hbUpdate.setVisible(false);
			filteredData();
			populatePieChart();
			if (tableview.getItems().isEmpty()) {
				pieChart.getData().clear();
				lblCaption.setVisible(false);
			}
		});

		// Update tableview items using menu items, refresh tableview with
		// correct info
		update.setOnAction(e -> populateHBox());
		btnSubmitChange.setOnAction(e -> {
			try {
				cboUpdateCategory.getValue();
				DBUpdate();
				tableview.getColumns().clear();
				tvSearch.getColumns().clear();
				populateTable();
				filteredData();
				populatePieChart();
				lblUpdate.setVisible(false);
				lblCaption.setVisible(false);
			} catch (NullPointerException f) {
				lblUpdate.setVisible(true);
				lblUpdate.setText("*Must have a value in all fields*");
				PauseTransition delay = new PauseTransition(Duration.seconds(5));
				delay.setOnFinished(event -> lblUpdate.setVisible(false));
				delay.play();
			}
		});

		// Cancel update, hide hbox containing update fields, return to default
		// view
		btnReturn.setOnAction(e -> {
			hbUpdate.setVisible(false);
			lblTransactions.setVisible(true);
			btnEditDate.setVisible(true);
			btnBack.setVisible(true);
		});

		// Hide search results
		btnHideResults.setOnAction(e -> {
			tvSearch.setVisible(false);
			btnHideResults.setVisible(false);
		});

		// set label content
		DateFormat pieLabel = new SimpleDateFormat("EEEE, dd MMMM yyyy");
		Date date = new Date();
		String strDate = pieLabel.format(date);
		lblDate.setText("As of " + strDate);
		lblTransactions.setText("Transactions for " + s);
	}

	/**
	 * Delete record from monthly_finances table where TransactionID = item in tableview row
	 */
	private void DBDelete() {
		TablePosition pos = tableview.getSelectionModel().getSelectedCells().get(0);
		int row = pos.getRow();
		ObservableList item = tableview.getItems().get(row);
		String SQL = "DELETE FROM `monthly_finances` WHERE `monthly_finances`.`TransactionID` = " + item.get(0);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			int rs = conn.createStatement().executeUpdate(SQL);
		} catch (SQLException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		data.remove(item);
	}

	/**
	 * Populates HBox used to update a transaction item and sets HBox visibility to true
	 */
	private void populateHBox() {
		TablePosition pos = tableview.getSelectionModel().getSelectedCells().get(0);
		int row = pos.getRow();
		ObservableList item = tableview.getItems().get(row);

		loadCombo();
		hbUpdate.setVisible(true);
		lblTransactions.setVisible(false);
		btnEditDate.setVisible(false);
		btnBack.setVisible(false);
		cboUpdateCategory.setValue((String) item.get(4));
		tfUpdateLocation.setText((String) item.get(2));
		tfUpdateAmount.setText((String) item.get(3));
		lblTransactionID.setText((String) item.get(0));
		dpUpdateDate.equals(item.get(1));
	}

	/**
	 * Updates the selected database item with new values
	 */
	private void DBUpdate() {
		LocalDate Date;
		Date = dpUpdateDate.getValue();
		Double Amount = Double.parseDouble(tfUpdateAmount.getText().trim());
		String Location = tfUpdateLocation.getText().trim();
		String TransactionID = lblTransactionID.getText();
		cboUpdateCategory.getValue();
		if (cboUpdateCategory.getValue().equals("Select Category")) {
			lblStatus.setText("Invalid");
		} else if (cboUpdateCategory.getValue().equals("Groceries")) {
			i = 0;
		} else if (cboUpdateCategory.getValue().equals("Restaurants & Dining")) {
			i = 1;
		} else if (cboUpdateCategory.getValue().equals("Shopping & Entertainment")) {
			i = 2;
		} else if (cboUpdateCategory.getValue().equals("Cash, Checks & Misc.")) {
			i = 3;
		} else if (cboUpdateCategory.getValue().equals("Finance")) {
			i = 4;
		} else if (cboUpdateCategory.getValue().equals("Home & Utilities")) {
			i = 5;
		} else if (cboUpdateCategory.getValue().equals("Personal & Family")) {
			i = 6;
		} else if (cboUpdateCategory.getValue().equals("Health")) {
			i = 7;
		} else if (cboUpdateCategory.getValue().equals("Travel")) {
			i = 8;
		} else if (cboUpdateCategory.getValue().equals("Giving & Charity")) {
			i = 9;
		} else if (cboUpdateCategory.getValue().equals("Business Expenses")) {
			i = 10;
		} else if (cboUpdateCategory.getValue().equals("Education")) {
			i = 11;
		} else if (cboUpdateCategory.getValue().equals("Transportation")) {
			i = 12;
		} else if (cboUpdateCategory.getValue().equals("Uncategorized")) {
			i = 13;
		}

		try {

			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String sqlUpdate = "UPDATE `monthly_finances` SET `Date` = '" + Date + "', `Amount` = '" + Amount
					+ "', `Location` = '" + Location + "', `CategoryID` = '" + i
					+ "' WHERE `monthly_finances`.`TransactionID` =" + TransactionID + ";";
			PreparedStatement prest = (PreparedStatement) conn.prepareStatement(sqlUpdate);
			prest.executeUpdate();
			dpUpdateDate.setValue(null);
			tfUpdateAmount.clear();
			tfUpdateLocation.clear();
			lblTransactionID.setText("");
			cboCategory.setValue("Select Category");
			hbUpdate.setVisible(false);
			lblTransactions.setVisible(true);
			btnEditDate.setVisible(true);

		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			lblUpdate.setText("Must have a value in all fields");
			PauseTransition delay = new PauseTransition(Duration.seconds(5));
			delay.setOnFinished(event -> lblUpdate.setVisible(false));
			delay.play();
		}

		// } // end if
	}// end DBUpdate

	/**
	 * Inserts a new record into the database from "New Transaction" fields
	 */
	private void DBInsert() {
		LocalDate Date;
		Date = dpDate.getValue();
		Double Amount = Double.parseDouble(tfAmount.getText().trim());
		String Location = tfLocation.getText().trim();
		cboCategory.getValue();
		if (cboCategory.getValue().equals("Select Category")) {
			lblStatus.setText("Invalid");
		} else if (cboCategory.getValue().equals("Groceries")) {
			i = 0;
		} else if (cboCategory.getValue().equals("Restaurants & Dining")) {
			i = 1;
		} else if (cboCategory.getValue().equals("Shopping & Entertainment")) {
			i = 2;
		} else if (cboCategory.getValue().equals("Cash, Checks & Misc.")) {
			i = 3;
		} else if (cboCategory.getValue().equals("Finance")) {
			i = 4;
		} else if (cboCategory.getValue().equals("Home & Utilities")) {
			i = 5;
		} else if (cboCategory.getValue().equals("Personal & Family")) {
			i = 6;
		} else if (cboCategory.getValue().equals("Health")) {
			i = 7;
		} else if (cboCategory.getValue().equals("Travel")) {
			i = 8;
		} else if (cboCategory.getValue().equals("Giving & Charity")) {
			i = 9;
		} else if (cboCategory.getValue().equals("Business Expenses")) {
			i = 10;
		} else if (cboCategory.getValue().equals("Education")) {
			i = 11;
		} else if (cboCategory.getValue().equals("Transportation")) {
			i = 12;
		} else if (cboCategory.getValue().equals("Uncategorized")) {
			i = 13;
		} else {
			lblStatus.setText("Invalid Input");
		}

		if (tfLocation.getText().isEmpty() || Date.equals(null) || cboCategory.getValue().equals("Select Category")) {
			/*
			 * if (tfAmount.getText().isEmpty() ||
			 * tfLocation.getText().isEmpty() || Date.equals(null) ||
			 * cboCategory.getValue().equals("Select Category")) {
			 */
			lblStatus.setText("Invalid Input - must have a value in each field");
		} else /* if (Amount != null && Location != null && Date != null) */ {
			try {

				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
				String sqlInsert = "Insert into monthly_finances(Date, Amount, Location, CategoryID) values(?, ?, ?, ?)";
				PreparedStatement prest = (PreparedStatement) conn.prepareStatement(sqlInsert);

				prest.setDouble(2, Amount);
				prest.setString(3, Location);
				prest.setInt(4, i);
				prest.setObject(1, Date);

				prest.executeUpdate();
				dpDate.setValue(null);
				tfAmount.clear();
				tfLocation.clear();
				lblStatus.setVisible(true);
				lblStatus.setText(
						"Record " + Date + " " + Amount + " " + Location + " " + cboCategory.getValue() + " inserted");
				cboCategory.setValue("Select Category");
				PauseTransition delay = new PauseTransition(Duration.seconds(5));
				delay.setOnFinished(event -> lblStatus.setVisible(false));
				delay.play();
				tableview.getColumns().clear();
				tvSearch.getColumns().clear();
				populateTable();
				filteredData();
				populatePieChart();

			} catch (SQLException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		} // end if
	}// end DBInsert

	/**
	 * Loads the combo box with listed categories from the database table tCategory
	 */
	private void loadCombo() {

		ObservableList<String> lstItems = FXCollections.observableArrayList();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("Select * from tcategory order by category ASC;");
			while (rs.next()) {
				lstItems.add(rs.getString(2));
			}
			cboCategory.setItems(lstItems);
			cboUpdateCategory.setItems(lstItems);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} // end Load Combo
	}

	/**
	 * Populates the tableview with default values for the current month
	 */
	private void populateTable() {
		try {
			data = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "SELECT transactionID, date, location, amount, category  FROM `monthly_finances` INNER JOIN tcategory ON monthly_finances.CategoryID = tcategory.CategoryID WHERE Year(Date) = Year(CURRENT_TIMESTAMP) AND Month(Date) = Month(CURRENT_TIMESTAMP) ORDER BY date ASC";
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				final int j = i;
				TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				col.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});
				TableColumn searchCol = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				searchCol.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});

				tableview.getColumns().addAll(col);
				tvSearch.getColumns().addAll(searchCol);

				col.prefWidthProperty().bind(tableview.widthProperty().multiply(.16));
			}

			while (rs.next()) {
				// Iterate Row
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// Iterate Column
					row.add(rs.getString(i));

				}
				data.add(row);
			}

			// CREATE DELETE ACTION COLUMN
			TableColumn col_delete = new TableColumn<>("Action");
			tableview.getColumns().add(col_delete);

			col_delete.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_delete.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellDelete();
				}

			});
			// CREATE UPDATE ACTION COLUMN
			TableColumn col_update = new TableColumn<>("Action");
			tableview.getColumns().add(col_update);

			col_update.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_update.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellUpdate();
				}

			});
			tableview.setItems(data);
			searchData = data;
			tvSearch.setItems(searchData);
			tableview.setContextMenu(menu);

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}// end populateTable

	/**
	 * populates the tableview after clicking "All Time" button. Shows all
	 * transactions in RDBMS
	 */
	private void populateAllTime() {
		try {
			data = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "SELECT transactionID, date, location, amount, category  FROM `monthly_finances` INNER JOIN tcategory ON monthly_finances.CategoryID = tcategory.CategoryID ORDER BY date ASC";
			ResultSet rs = conn.createStatement().executeQuery(SQL);

			for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				final int j = i;
				TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				col.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});
				TableColumn searchCol = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				searchCol.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});

				tableview.getColumns().addAll(col);
				tvSearch.getColumns().addAll(searchCol);

				col.prefWidthProperty().bind(tableview.widthProperty().multiply(.175));
			}
			while (rs.next()) {
				// Iterate Row
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// Iterate Column
					row.add(rs.getString(i));
				}
				data.add(row);

			}

			// CREATE ACTION COLUMN
			TableColumn col_action = new TableColumn<>("Action");
			tableview.getColumns().add(col_action);

			col_action.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_action.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellDelete();
				}

			});
			// CREATE UPDATE ACTION COLUMN
			TableColumn col_update = new TableColumn<>("Action");
			tableview.getColumns().add(col_update);

			col_update.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_update.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellUpdate();
				}

			});
			tableview.setItems(data);
			searchData = data;
			tvSearch.setItems(searchData);
			tableview.setContextMenu(menu);

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}// end populateTable

	/**
	 * Populates the tableview with custom date ranges
	 */
	private void customPopulateTable() {
		try {
			LocalDate FromDate;
			FromDate = dpFromDate.getValue();
			LocalDate ToDate;
			ToDate = dpToDate.getValue();
			LocalDate ToDatePlus1 = ToDate.plusDays(1);
			data = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "SELECT transactionID, date, location, amount, category  FROM `monthly_finances` INNER JOIN tcategory ON monthly_finances.CategoryID = tcategory.CategoryID WHERE date >= '"
					+ FromDate + "' AND date < '" + ToDatePlus1 + "' ORDER BY date ASC";
			ResultSet rs = conn.createStatement().executeQuery(SQL);

			for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
				final int j = i;
				TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				col.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});
				TableColumn searchCol = new TableColumn(rs.getMetaData().getColumnName(i + 1));
				searchCol.setCellValueFactory(
						new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
								return new SimpleStringProperty(param.getValue().get(j).toString());
							}
						});

				tableview.getColumns().addAll(col);
				tvSearch.getColumns().addAll(searchCol);
				col.prefWidthProperty().bind(tableview.widthProperty().multiply(.175));

			}
			while (rs.next()) {
				// Iterate Row
				ObservableList<String> row = FXCollections.observableArrayList();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// Iterate Column
					row.add(rs.getString(i));
				}
				data.add(row);
			}

			TableColumn col_action = new TableColumn<>("Action");
			tableview.getColumns().add(col_action);

			col_action.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_action.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellDelete();
				}

			});
			// CREATE UPDATE ACTION COLUMN
			TableColumn col_update = new TableColumn<>("Action");
			tableview.getColumns().add(col_update);

			col_update.setCellValueFactory(
					new Callback<TableColumn.CellDataFeatures<Record, Boolean>, ObservableValue<Boolean>>() {

						@Override
						public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Record, Boolean> p) {
							return new SimpleBooleanProperty(p.getValue() != null);
						}
					});

			// Adding the Button to the cell
			col_update.setCellFactory(new Callback<TableColumn<Record, Boolean>, TableCell<Record, Boolean>>() {

				@Override
				public TableCell<Record, Boolean> call(TableColumn<Record, Boolean> p) {
					return new ButtonCellUpdate();
				}

			});
			tableview.setItems(data);
			searchData = data;
			tvSearch.setItems(searchData);
			tableview.setContextMenu(menu);

		} catch (SQLException | ClassNotFoundException | NullPointerException e) {
			e.printStackTrace();
		}
	}// end customPopulateTable

	/**
	 * Method used to filter and search observable list data
	 */
	private void filteredData() {
		FilteredList<ObservableList> filteredData = new FilteredList<>(data, e -> true);
		tfSearch.setOnKeyReleased(e -> {
			tfSearch.textProperty().addListener((observableValue, oldValue, newValue) -> {
				if (tfSearch.getText().isEmpty()) {
					tvSearch.setVisible(false);
					btnHideResults.setVisible(false);
				} else {

					tvSearch.setVisible(true);
					btnHideResults.setVisible(true);
					filteredData.setPredicate((Predicate<? super ObservableList>) ObservableList -> {
						if (newValue == null || newValue.isEmpty()) {
							return true;
						}
						String lowerCaseFilter = newValue.toLowerCase();
						if (((String) ObservableList.get(0)).contains(newValue)) {
							return true;
						} else if (ObservableList.get(1).toString().toLowerCase().contains(lowerCaseFilter)) {
							return true;
						} else if (ObservableList.get(2).toString().toLowerCase().contains(lowerCaseFilter)) {
							return true;
						} else if (ObservableList.get(3).toString().toLowerCase().contains(lowerCaseFilter)) {
							return true;
						} else if (ObservableList.get(4).toString().toLowerCase().contains(lowerCaseFilter)) {
							return true;
						}
						return false;
					});
				}
			});

			SortedList<ObservableList> sortedData = new SortedList<>(filteredData);
			sortedData.comparatorProperty().bind(tvSearch.comparatorProperty());
			tvSearch.setItems(sortedData);

		});// end setOnKeyReleased event

	}// end filtered data

	/**
	 * Populates the piechart with default data from the current month
	 */
	private void populatePieChart() {
		try {
			pieChartData = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "select sum(amount), Category from monthly_finances inner join tcategory on monthly_finances.CategoryID = tcategory.CategoryID WHERE tcategory.CategoryID IN ('0', '1','2','3','4','5', '6','7', '8','9', '10','11', '12','13') AND Year(Date) = Year(CURRENT_TIMESTAMP) AND Month(Date) = Month(CURRENT_TIMESTAMP) GROUP BY tcategory.CategoryID";

			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				// adding data on piechart data
				pieChartData.add(new PieChart.Data(rs.getString(2), rs.getDouble(1)));
				pieChart.setData(pieChartData);

			}

			for (final PieChart.Data data : pieChart.getData()) {

				data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						Point2D locationInScene = new Point2D(e.getSceneX(), e.getSceneY());
						Point2D locationInParent = grpGroup.sceneToLocal(locationInScene);
						double total = 0;
						String s = null;
						for (PieChart.Data d : pieChart.getData()) {
							total += d.getPieValue();
						}
						lblCaption.setVisible(true);
						lblCaption.relocate(locationInParent.getX(), locationInParent.getY());
						String text = String.format("%.1f%%", 100 * data.getPieValue() / total);
						s = data.getName();
						lblCaption.setText(text + " " + s);
						lblCaption.getStyleClass().add("custom-label");
					}
				});
			} // end for
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}// end populatePieChart

	/**
	 * populates piechart with values from all transactions in RDBMS
	 */
	private void populatePieChartAllTime() {
		try {
			pieChartData = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "select sum(amount), Category from monthly_finances inner join tcategory on monthly_finances.CategoryID = tcategory.CategoryID WHERE tcategory.CategoryID IN ('0', '1','2','3','4','5', '6','7', '8','9', '10','11', '12','13') GROUP BY tcategory.CategoryID";

			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				// adding data on piechart data
				pieChartData.add(new PieChart.Data(rs.getString(2), rs.getDouble(1)));
				pieChart.setData(pieChartData);

			}

			for (final PieChart.Data data : pieChart.getData()) {
				data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						Point2D locationInScene = new Point2D(e.getSceneX(), e.getSceneY());
						Point2D locationInParent = grpGroup.sceneToLocal(locationInScene);

						double total = 0;
						String s = null;
						for (PieChart.Data d : pieChart.getData()) {
							total += d.getPieValue();
						}
						lblCaption.setVisible(true);
						lblCaption.relocate(locationInParent.getX(), locationInParent.getY());
						String text = String.format("%.1f%%", 100 * data.getPieValue() / total);
						s = data.getName();
						lblCaption.setText(text + " " + s);
						lblCaption.getStyleClass().add("custom-label");

					}
				});
			} // end for

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}// end populatePieChart

	/**
	 * Populates piechart with transactions occurring in custom date range
	 */
	private void customPopulatePieChart() {
		LocalDate FromDate;
		FromDate = dpFromDate.getValue();
		LocalDate ToDate;
		ToDate = dpToDate.getValue();
		LocalDate ToDatePlus1 = ToDate.plusDays(1);
		try {
			pieChartData = FXCollections.observableArrayList();
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root", "");
			String SQL = "select sum(amount), Category from monthly_finances inner join tcategory on monthly_finances.CategoryID = tcategory.CategoryID WHERE tcategory.CategoryID IN ('0','1','2','3','4','5', '6','7', '8','9', '10','11', '12','13') AND Date >= '"
					+ FromDate + "' AND Date < '" + ToDatePlus1 + "' GROUP BY tcategory.CategoryID";
			ResultSet rs = conn.createStatement().executeQuery(SQL);
			while (rs.next()) {
				pieChartData.add(new PieChart.Data(rs.getString(2), rs.getDouble(1)));
				pieChart.setData(pieChartData);

			}

			for (final PieChart.Data data : pieChart.getData()) {
				data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						Point2D locationInScene = new Point2D(e.getSceneX(), e.getSceneY());
						Point2D locationInParent = grpGroup.sceneToLocal(locationInScene);
						double total = 0;
						String s = null;
						for (PieChart.Data d : pieChart.getData()) {
							total += d.getPieValue();
						}
						lblCaption.setVisible(true);
						lblCaption.relocate(locationInParent.getX(), locationInParent.getY());
						String text = String.format("%.1f%%", 100 * data.getPieValue() / total);
						s = data.getName();
						lblCaption.setText(text + " " + s);
					}
				});
			} // end for
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}// end customPopulatePieChart

	/**
	 * Define the button cell for tableview delete function
	 *
	 */
	private class ButtonCellDelete extends TableCell<Record, Boolean> {
		Button btnDelete = new Button("Delete");

		ButtonCellDelete() {
			btnDelete.setStyle("-fx-padding: 2 5 2 5;");
			btnDelete.setFocusTraversable(false);
			// Action when the button is pressed
			btnDelete.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					// get Selected Item
					ObservableList record = (ObservableList) ButtonCellDelete.this.getTableView().getItems()
							.get(ButtonCellDelete.this.getIndex());
					String SQL = "DELETE FROM `monthly_finances` WHERE `monthly_finances`.`TransactionID` = "
							+ record.get(0);
					try {
						Class.forName("com.mysql.jdbc.Driver");
						Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/finances", "root",
								"");
						int rs = conn.createStatement().executeUpdate(SQL);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					data.remove(record);
					populatePieChart();
					if (tableview.getItems().isEmpty()) {
						pieChart.getData().clear();
						lblCaption.setVisible(false);
					}
				}
			});
		}

		/**
		 * Displays delete button if the row is not empty
		 */
		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(btnDelete);
			} else {
				setGraphic(null);
			}
		}

	}// end class ButtonCellDelete

	/**
	 * Define the button cell for tableview update function
	 *
	 */
	private class ButtonCellUpdate extends TableCell<Record, Boolean> {
		Button btnUpdate = new Button("Update");

		ButtonCellUpdate() {
			btnUpdate.setStyle("-fx-padding: 2 5 2 5;");
			btnUpdate.setFocusTraversable(false);
			btnUpdate.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e1) {
					// get Selected Item
					ObservableList record = (ObservableList) ButtonCellUpdate.this.getTableView().getItems()
							.get(ButtonCellUpdate.this.getIndex());
					populateHBox();
					System.out.println(record);
				}
			});
		}

		/**
		 * Displays update button if the row is not empty
		 */
		@Override
		protected void updateItem(Boolean t, boolean empty) {
			super.updateItem(t, empty);
			if (!empty) {
				setGraphic(btnUpdate);
			} else {
				setGraphic(null);
			}
		}

		/**
		 * Populates HBox to update records when using menu items
		 */
		private void populateHBox() {
			ObservableList record = (ObservableList) ButtonCellUpdate.this.getTableView().getItems()
					.get(ButtonCellUpdate.this.getIndex());

			loadCombo();
			hbUpdate.setVisible(true);
			lblTransactions.setVisible(false);
			btnEditDate.setVisible(false);
			btnBack.setVisible(false);
			cboUpdateCategory.setValue((String) record.get(4));
			tfUpdateLocation.setText((String) record.get(2));
			tfUpdateAmount.setText((String) record.get(3));
			lblTransactionID.setText((String) record.get(0));
			dpUpdateDate.equals(record.get(1));
		}
	}// end class ButtonCellUpdate

}// end class
