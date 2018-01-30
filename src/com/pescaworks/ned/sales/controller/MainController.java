package com.pescaworks.ned.sales.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

import com.pescaworks.ned.sales.MainApp;
import com.pescaworks.ned.sales.model.Product;
import com.pescaworks.ned.sales.model.TableEntry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainController implements EventHandler<MouseEvent> {
	@FXML
	private ListView<String> foodList;

	@FXML
	private ListView<String> drinksList;

	@FXML
	private ListView<String> salesList;

	@FXML
	private TableView<TableEntry> table;

	@FXML
	private TableColumn<Product, String> itemCol;

	@FXML
	private TableColumn<Product, String> unitPriceCol;

	@FXML
	private TableColumn<Product, String> quantityCol;

	@FXML
	private TableColumn<Product, String> totalPriceCol;

	@FXML
	private Label totalSalesText;

	@FXML
	private DatePicker datePicker;

	@FXML
	private Button saveBtn;

	@FXML
	private Button clearBtn;

	@FXML
	private Spinner<Integer> orderNoSpinner;

	private ObservableList<TableEntry> tableEntries;

	private ArrayList<Product> productList = new ArrayList<>();

	Connection connect = null;

	@FXML
	private void initialize() {
		tableEntries = FXCollections.observableArrayList();

		// Link columns to objects
		itemCol.setCellValueFactory(new PropertyValueFactory<Product, String>("item"));
		unitPriceCol.setCellValueFactory(new PropertyValueFactory<Product, String>("unitPrice"));
		quantityCol.setCellValueFactory(new PropertyValueFactory<Product, String>("quantity"));
		totalPriceCol.setCellValueFactory(new PropertyValueFactory<Product, String>("totalPrice"));

		table.setItems(tableEntries);
		foodList.setOnMouseClicked(this);
		drinksList.setOnMouseClicked(this);
		salesList.setOnMouseClicked(this);

		saveBtn.setOnMouseClicked(this);
		clearBtn.setOnMouseClicked(this);

		table.setOnMouseClicked(this);

		// Set-up quantity spinner
		SpinnerValueFactory<Integer> quantityValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1);

		orderNoSpinner.setValueFactory(quantityValue);

		// Set-up date picker
		StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, dateFormatter);
				} else {
					return null;
				}
			}
		};

		datePicker.setConverter(converter);
		datePicker.setValue(LocalDate.now());

		// Set-up total sales text
		totalSalesText.setText("0");

		try {
			// Setup DB connection
			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection("jdbc:mysql://localhost/tribu?" + "user=root&password=root");

			populateList();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void populateList() {

		try {
			Statement statement = connect.createStatement();

			// Query products
			ResultSet resultSet = statement.executeQuery("select * from tribu.products");

			// Save to array list
			while (resultSet.next()) {
				Product p = new Product();
				p.setCategory(resultSet.getString("CATEGORY"));
				p.setSubcategory(resultSet.getString("SUBCATEGORY"));
				p.setItem(resultSet.getString("ITEM"));
				p.setPrice(resultSet.getInt("PRICE"));
				p.setCost(resultSet.getInt("COST"));

				productList.add(p);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Product product : productList) {
			String display = product.getSubcategory() + ": " + product.getItem();
			display = display.toUpperCase();

			if (product.getCategory().equals("food")) {
				foodList.getItems().add(display);
			} else if (product.getCategory().equals("drinks")) {
				drinksList.getItems().add(display);
			}
		}

		String[] sales = { "OTHER" };

		for (String sale : sales) {
			salesList.getItems().add(sale);
		}

	}

	@Override
	public void handle(MouseEvent event) {
		Object source = event.getSource();
		if (source == foodList || source == drinksList || source == salesList) {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (event.getClickCount() == 2) {
					String selected = ((ListView<String>) source).getSelectionModel().getSelectedItem();
					Product product = getProductFromString(selected);

					FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/Add.fxml"));
					try {
						Stage stage = new Stage();

						GridPane layout = (GridPane) loader.load();
						Scene scene = new Scene(layout);

						AddController controller = loader.<AddController>getController();
						controller.setup(this, product, selected);

						stage.setScene(scene);
						stage.show();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		} else if (source == saveBtn) {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (connect == null) {
					return;
				}

				try {

					// Check database for same date and order number
					String query = "SELECT * FROM  tribu.sales WHERE DATE = ? AND ORDER_NO = ?";
					PreparedStatement ps = connect.prepareStatement(query);
					
					ps.setDate(1, Date.valueOf(datePicker.getValue()));
					ps.setInt(2,  orderNoSpinner.getValue());

					ResultSet resultSet = ps.executeQuery();

					if (resultSet.isBeforeFirst()) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Existing Record Detected");
						alert.setHeaderText(null);
						alert.setContentText("Record exist with same date and order number. Continue Saving?");

						Optional<ButtonType> result = alert.showAndWait();
						if (result.get() != ButtonType.OK) {
							return;
						}
					}

					String insert = "INSERT INTO tribu.sales (DATE, ORDER_NO, ITEM, QUANTITY,UNIT_PRICE, TOTAL_PRICE) VALUES (?, ?, ?, ?, ?, ?)";
					ps = connect.prepareStatement(insert);

					for (TableEntry entry : tableEntries) {
						ps.setDate(1, Date.valueOf(datePicker.getValue()));
						ps.setInt(2, orderNoSpinner.getValue());
						ps.setString(3, entry.getItem());
						ps.setInt(4, entry.getQuantity());
						ps.setInt(5, entry.getUnitPrice());
						ps.setInt(6, entry.getTotalPrice());

						ps.addBatch();
					}

					ps.executeBatch();
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Success");
					alert.setHeaderText(null);
					alert.setContentText("Order successfully saved!");

					alert.showAndWait();

					orderNoSpinner.increment();
					totalSalesText.setText("0");
					tableEntries.clear();

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if (source == clearBtn) {
			if (event.getButton() == MouseButton.PRIMARY) {
				tableEntries.clear();
			}

		} else if (source == table) {
			if (event.getButton() == MouseButton.PRIMARY) {
				if (event.getClickCount() == 2) {
					TableEntry entry = table.getSelectionModel().getSelectedItem();
					
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Remove Row");
					alert.setHeaderText(null);
					alert.setContentText("Do you wish to remove row?\n" + entry.toString());

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK) {
						int totalSales = Integer.parseInt(totalSalesText.getText());
						totalSales -= entry.getTotalPrice();
						totalSalesText.setText("" + totalSales);
						
						tableEntries.remove(entry);
					}
				}
			}

		}

	}

	private Product getProductFromString(String string) {
		Product product = null;
		for (Product p : productList) {
			String s = p.getSubcategory() + ": " + p.getItem();
			s = s.toUpperCase();
			if (s.equals(string)) {
				product = p;
				break;
			}
		}
		return product;
	}

	public ListView<String> getFoodList() {
		return foodList;
	}

	public void setFoodList(ListView<String> foodList) {
		this.foodList = foodList;
	}

	public ListView<String> getDrinksList() {
		return drinksList;
	}

	public void setDrinksList(ListView<String> drinksList) {
		this.drinksList = drinksList;
	}

	public TableView<TableEntry> getTable() {
		return table;
	}

	public void setTable(TableView<TableEntry> table) {
		this.table = table;
	}

	public ObservableList<TableEntry> getTableEntries() {
		return tableEntries;
	}

	public void setTableEntries(ObservableList<TableEntry> tableEntries) {
		this.tableEntries = tableEntries;
	}

	public ListView<String> getSalesList() {
		return salesList;
	}

	public void setSalesList(ListView<String> salesList) {
		this.salesList = salesList;
	}

	public TableColumn<Product, String> getItemCol() {
		return itemCol;
	}

	public void setItemCol(TableColumn<Product, String> itemCol) {
		this.itemCol = itemCol;
	}

	public TableColumn<Product, String> getUnitPriceCol() {
		return unitPriceCol;
	}

	public void setUnitPriceCol(TableColumn<Product, String> unitPriceCol) {
		this.unitPriceCol = unitPriceCol;
	}

	public TableColumn<Product, String> getQuantityCol() {
		return quantityCol;
	}

	public void setQuantityCol(TableColumn<Product, String> quantityCol) {
		this.quantityCol = quantityCol;
	}

	public TableColumn<Product, String> getTotalPriceCol() {
		return totalPriceCol;
	}

	public void setTotalPriceCol(TableColumn<Product, String> totalPriceCol) {
		this.totalPriceCol = totalPriceCol;
	}

	public Label getTotalSalesText() {
		return totalSalesText;
	}

	public void setTotalSalesText(Label totalSalesText) {
		this.totalSalesText = totalSalesText;
	}

	public DatePicker getDatePicker() {
		return datePicker;
	}

	public void setDatePicker(DatePicker datePicker) {
		this.datePicker = datePicker;
	}

	public Button getSaveBtn() {
		return saveBtn;
	}

	public void setSaveBtn(Button saveBtn) {
		this.saveBtn = saveBtn;
	}

	public Button getClearBtn() {
		return clearBtn;
	}

	public void setClearBtn(Button clearBtn) {
		this.clearBtn = clearBtn;
	}

	public Spinner<Integer> getOrderNoSpinner() {
		return orderNoSpinner;
	}

	public void setOrderNoSpinner(Spinner<Integer> orderNoSpinner) {
		this.orderNoSpinner = orderNoSpinner;
	}

	public ArrayList<Product> getProductList() {
		return productList;
	}

	public void setProductList(ArrayList<Product> productList) {
		this.productList = productList;
	}

	public Connection getConnect() {
		return connect;
	}

	public void setConnect(Connection connect) {
		this.connect = connect;
	}
}