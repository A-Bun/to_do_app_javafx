package com.todo;

import java.util.Comparator;

public class ListItem {

    private String id;
    private String name;
    private boolean checked;
    public static Comparator<ListItem> comparator = ((ListItem i1, ListItem i2) -> {
        int cmp = i1.getName().compareToIgnoreCase(i2.getName());

        if(cmp < 0) {
            return -1;
        }
        else if(cmp > 0) {
            return 1;
        }
        else {
            return 0;
        }
    });

    public ListItem(String i_id, String i_name, boolean i_checked) {
        this.id = i_id;
        this.name = i_name;
        this.checked = i_checked;
    }

     public ListItem(ListItem list_item) {
        this.id = list_item.getId();
        this.name = list_item.getName();
        this.checked = list_item.getStatus();
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public boolean getStatus() {
        return checked;
    }

    public void setId(String new_id) {
        id = new_id;
    }

    public void setName(String new_name) {
        name = new_name;
    }

    public void setStatus(boolean new_status) {
        checked = new_status;
    }

    public boolean isEqual(ListItem item_to_compare) {
        boolean result = false;

        if(item_to_compare.getId().equals(id) && item_to_compare.getName().equals(name) && item_to_compare.getStatus() == checked) {
            result = true;
        }

        return result;
    }
    
    // public int compare(ListItem item1, ListItem item2) {
        

    //     return c.compare(item1, item2);
    // }
}
