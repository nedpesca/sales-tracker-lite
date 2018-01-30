package com.pescaworks.ned.sales.model;

public class TableEntry {
	private String item;
	private int unitPrice;
	private int quantity;
	private int totalPrice;
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public int getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	@Override
	public String toString() {
		String string = "[TableEntry:Item:" + item +
						";UnitPrice:" + unitPrice +
						";Quantity:" + quantity +
						";TotalPrice:" + totalPrice + "]";
		return string;
	}
}
