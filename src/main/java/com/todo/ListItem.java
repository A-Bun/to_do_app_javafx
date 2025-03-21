package com.todo;

public class ListItem {

    private String id;
    private String name;
    private boolean checked;

    public ListItem(String i_id, String i_name, boolean i_checked) {
        this.id = i_id;
        this.name = i_name;
        this.checked = i_checked;
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
}
