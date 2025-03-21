package com.todo;

import java.util.ArrayList;

public class ListState {
    private String name;
    private ArrayList<ListItem> items;

    public ListState(String l_name, ArrayList<ListItem> l_items) {
        ArrayList<ListItem> state_items = new ArrayList<>();

        for (ListItem item : l_items) {
            state_items.add(new ListItem(item.getId(), item.getName(), item.getStatus()));
        }

        name = l_name;
        items = state_items;
    }

    // public ListState(ListState other_state) {
    //     ArrayList<ListItem> state_items = new ArrayList<>();

    //     for (ListItem item : other_state.getItems()) {
    //         state_items.add(new ListItem(item.getId(), item.getName(), item.getStatus()));
    //     }

    //     name = other_state.getName();
    //     items = state_items;
    // }

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

    public boolean isEqual(ListState state_to_compare) {
        boolean result = true;

        if(state_to_compare.getName().equals(name) && state_to_compare.getItems().size() == items.size()) {
            for(int i = 0; i < items.size(); i++) {
                if(!state_to_compare.getItems().get(i).isEqual(items.get(i))) {
                    result = false;
                    break;
                }
            }
        }
        else {
            result = false;
        }

        return result;
    }

    /* DEBUGGING USE ONLY - TO BE DELETED */
    public void printState() {
        System.out.println("{ STATE: ");
        System.out.println("    name = \"" + name + "\"");
        System.out.println("    items = [ ");

        for (ListItem item : items) {
            System.out.print("          \"" + item.getId() + " - " + item.getName());

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
