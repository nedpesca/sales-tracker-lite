package com.pescaworks.ned.sales.controller;

import com.pescaworks.ned.sales.model.Product;
import com.pescaworks.ned.sales.model.TableEntry;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AddController implements EventHandler<MouseEvent> {

	@FXML
	private TextField itemText;

	@FXML
	private TextField priceText;

	@FXML
	private Spinner<Integer> quantitySpin;

	@FXML
	private Button addButton;

	private MainController caller;

	@FXML
	private void initialize() {
		// Populate quantity spinner
		SpinnerValueFactory<Integer> quantityValue = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);

		quantitySpin.setValueFactory(quantityValue);

		// Set button event handler
		addButton.setOnMouseClicked(this);

	}

	public void setup(MainController caller, Product product, String selected) {
		if (caller == null) {
			return;
		}

		this.caller = caller;

		if (selected == null) {
			return;
		} else if (product == null) {
			if (!selected.equals("OTHER")) {
				itemText.setText(selected);
			}
		} else {
			itemText.setText(product.getItem());
			priceText.setText("" + product.getPrice());

			itemText.setDisable(true);
			priceText.setDisable(true);
		}
	}

	@Override
	public void handle(MouseEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();

		if (caller == null) {
			stage.close();
			return;
		}

		String name = itemText.getText();
		String price = priceText.getText();
		int quantity = quantitySpin.getValue();

		int intPrice = 0;

		itemText.setStyle(null);
		priceText.setStyle(null);

		if (name.isEmpty()) {
			itemText.setStyle("-fx-text-box-border: red");
			event.consume();
		}

		if (price.isEmpty()) {
			priceText.setStyle("-fx-text-box-border: red");
			event.consume();
		} else {
			try {
				intPrice = Integer.parseInt(price);
			} catch (NumberFormatException e) {
				priceText.setStyle("-fx-text-box-border: red");
				event.consume();
			}

		}

		if (event.isConsumed()) {
			return;
		}

		ObservableList<TableEntry> table = caller.getTableEntries();
		TableEntry entry = new TableEntry();

		entry.setItem(name);
		entry.setUnitPrice(intPrice);
		entry.setQuantity(quantity);
		
		int totalPrice = intPrice * quantity;
		entry.setTotalPrice(totalPrice);

		table.add(entry);

		int totalSales = Integer.parseInt(caller.getTotalSalesText().getText());
		totalSales += totalPrice;
		caller.getTotalSalesText().setText("" + totalSales);
		
		stage.close();

	}

}
