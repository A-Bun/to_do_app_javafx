package com.todo;

public class ListItem {

    private String name;
    private boolean checked;

    public ListItem(String i_name, boolean i_checked) {
        this.name = i_name;
        this.checked = i_checked;
    }
    
    public String getName() {
        return name;
    }

    public boolean getStatus() {
        return checked;
    }

    public void setName(String new_name) {
        name = new_name;
    }

    public void setStatus(boolean new_status) {
        checked = new_status;
    }
}
