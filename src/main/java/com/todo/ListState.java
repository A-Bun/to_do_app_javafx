package com.todo;

import java.util.ArrayList;

public class ListState {
    private String name;
    private ArrayList<ListItem> items;

    public ListState(String l_name, ArrayList<ListItem> l_items) {
        name = l_name;
        items = l_items;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ListItem> getItems() {
        return items;
    }

    public void setName(String new_name) {
        name = new_name;
    }

    public void setItems(ArrayList<ListItem> new_items) {
        items = new_items;
    }

    public void printState() {
        System.out.println("{ STATE: ");
        System.out.println("    name = \"" + name + "\"");
        System.out.println("    items = [ ");

        for (ListItem item : items) {
            System.out.print("          \"" + item.getName());

            if(item.getStatus()) {
                System.out.println("\" *checked*");
            }
            else {
                System.out.println("\" *unchecked*");
            }
        }

        System.out.println("    ]");
        System.out.println("}");
    }
}
