package com.pescaworks.ned.sales.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.pescaworks.ned.sales.model.Product;
import com.pescaworks.ned.sales.model.TableEntry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MainController implements EventHandler<MouseEvent> {
	@FXML
	private ListView<String> foodList;

	@FXML
	private ListView<String> drinksList;

	@FXML
	private TableView<TableEntry> table;

	@FXML
	private TableColumn itemCol;

	@FXML
	private TableColumn unitPriceCol;

	@FXML
	private TableColumn quantityCol;

	@FXML
	private TableColumn totalPriceCol;

	private ObservableList<TableEntry> tableEntries;

	private ArrayList<Product> productList = new ArrayList<>();

	@FXML
	private void initialize() {
		populateList();

		tableEntries = FXCollections.observableArrayList();

		itemCol.setCellValueFactory(new PropertyValueFactory<Product, String>("item"));
		unitPriceCol.setCellValueFactory(new PropertyValueFactory<Product, String>("unitPrice"));
		quantityCol.setCellValueFactory(new PropertyValueFactory<Product, String>("quantity"));
		totalPriceCol.setCellValueFactory(new PropertyValueFactory<Product, String>("totalPrice"));

		table.setItems(tableEntries);
		foodList.setOnMouseClicked(this);
		drinksList.setOnMouseClicked(this);
	}

	public void populateList() {
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// Setup DB connection
			Class.forName("com.mysql.jdbc.Driver");

			connect = DriverManager.getConnection("jdbc:mysql://localhost/tribu?" + "user=root&password=root");

			statement = connect.createStatement();

			// Query products
			resultSet = statement.executeQuery("select * from tribu.products");

			// Save to array list
			while (resultSet.next()) {
				Product p = new Product();
				p.setCategory(resultSet.getString("CATEGORY"));
				p.setSubcategory(resultSet.getString("SUBCATEGORY"));
				p.setItem(resultSet.getString("ITEM"));
				p.setPrice(resultSet.getInt("PRICE"));
				p.setCost(resultSet.getInt("COST"));

				productList.add(p);
				// System.out.println(p);
			}

		} catch (ClassNotFoundException | SQLException e) {
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

	public TableColumn getItemCol() {
		return itemCol;
	}

	public void setItemCol(TableColumn itemCol) {
		this.itemCol = itemCol;
	}

	public TableColumn getUnitPriceCol() {
		return unitPriceCol;
	}

	public void setUnitPriceCol(TableColumn unitPriceCol) {
		this.unitPriceCol = unitPriceCol;
	}

	public TableColumn getQuantityCol() {
		return quantityCol;
	}

	public void setQuantityCol(TableColumn quantityCol) {
		this.quantityCol = quantityCol;
	}

	public TableColumn getTotalPriceCol() {
		return totalPriceCol;
	}

	public void setTotalPriceCol(TableColumn totalPriceCol) {
		this.totalPriceCol = totalPriceCol;
	}

	public ObservableList<TableEntry> getTableEntries() {
		return tableEntries;
	}

	public void setTableEntries(ObservableList<TableEntry> tableEntries) {
		this.tableEntries = tableEntries;
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
			if (event.getSource() == foodList || event.getSource() == drinksList) {
				String selected = ((ListView<String>) event.getSource()).getSelectionModel().getSelectedItem();
				Product product = getProductFromString(selected);
				TableEntry entry = new TableEntry();

				if (product == null) {
					entry.setItem(selected);
				} else {
					entry.setItem(product.getItem());
					entry.setUnitPrice(product.getPrice());
				}

				tableEntries.add(entry);
			}
		}
	}

	private void updateTable(TableEntry entry) {

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

}