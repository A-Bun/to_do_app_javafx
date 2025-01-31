package com.todo;

import java.util.ArrayList;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SQLController {

    // Database direct connection string
    private static final String CONNECTION_STRING = "mongodb+srv://alexisp30ples:v77s8ZhiG2lQ48KV@to-do-cluster-1.efhkz.mongodb.net/?retryWrites=true&w=majority&appName=To-Do-Cluster-1"; 

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    /* Open a new connection to the database */
    public boolean openConnection() {
        boolean result;
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            MongoDatabase database = mongoClient.getDatabase("To-Do-Tracker");
            collection = database.getCollection("Lists");
            System.out.println("Connection opened successfully");
            result = true;
        } catch (Exception e) {
            System.out.println("Failed to open connection...");
            result = false;
        }
        return result;
    }
 
    /* Close the existing connection to the database */
    public boolean closeConnection() {
        boolean result;
        try {
            mongoClient.close();
            System.out.println("Connection closed successfully");
            result = true;
        } catch (Exception e) {
            System.out.println("Failed to close connection...");
            result = false;
        }
        return result;
    }

    /* Get all lists and their items from the database */
    // public Map<String, ArrayList<ListItem>> getAllLists() {
    //     /* Key: Title; Value: Items
    //      * ex. Christmas: [Buy Gifts, Wrap Gifts, Hide Gifts, Transport Gifts]
    //      */
    //     Map<String, ArrayList<ListItem>> lists = new HashMap<>();
        
    //     FindIterable<Document> docs = collection.find();

    //     for (Document doc : docs) {
    //         String title = doc.get("title").toString();
    //         ArrayList<ListItem> items = doc.get("items", new ArrayList<>());

    //         lists.put(title, items);
    //     }

    //     return lists;
    // }

    /* Get all lists (names only) from the database */
    public ArrayList<String> getAllListNames() {
        ArrayList<String> lists = new ArrayList<>();

        FindIterable<Document> docs = collection.find();

        for(Document doc : docs) {
            String title = doc.get("title").toString();
            lists.add(title);
        }

        return lists;
    }

    /* Get all items from the list with the provided name from the database*/
    public ArrayList<ListItem> getList(String list_name) {
        ArrayList<ListItem> list = new ArrayList<>();

        BsonString str = new BsonString(list_name);
        BsonDocument filter = new BsonDocument("title", str);

        Document doc = collection.find(filter).first();

        if(doc != null) {
            ArrayList<Document> temp = new ArrayList<>();

            temp = doc.get("items", temp);

            for(Document item : temp) {
                String name = item.get("name").toString();
                boolean checked = (boolean)item.get("checked");
                list.add(new ListItem(name, checked));
            }
        }
        else {
            System.err.println("ERROR: List \"" + list_name + "\" does not exist...");
        }

        return list;
    }

    /* Add a new list to the database */
    public void addList(String list_name, ArrayList<ListItem> list_items) {
        Document new_list = new Document();
        ArrayList<Document> items = new ArrayList<>();

        for(int i = 0; i < list_items.size(); i++) {
            Document new_item = new Document();

            new_item.append("name", list_items.get(i).getName());
            new_item.append("checked", list_items.get(i).getStatus());
            items.add(new_item);
        }

        new_list.append("title", list_name);
        new_list.append("items", items);
        
        collection.insertOne(new_list);

        System.out.println("Creation of list \"" + list_name + "\" was successful!");
    }

    /* Update an existing list in the database */
    public void updateList(String list_name, String new_name, ArrayList<ListItem> new_list_items) {
        BsonString current_list_name = new BsonString(list_name);
        BsonDocument filter = new BsonDocument("title", current_list_name);
        BsonDocument updated_list = new BsonDocument();
        
        if(new_name != null && !new_name.equals(list_name)) {
            BsonString updated_list_name = new BsonString(new_name);
            BsonDocument inner_title = new BsonDocument("title", updated_list_name);
            updated_list.append("$set", inner_title);
        }

        if(new_list_items != null) {
            BsonArray updated_list_items = new BsonArray();

            for(ListItem item : new_list_items) {
                BsonDocument new_item = new BsonDocument();
                BsonString item_name = new BsonString(item.getName());
                BsonBoolean item_checked = new BsonBoolean(item.getStatus());

                new_item.put("name", item_name);
                new_item.put("checked", item_checked);
                updated_list_items.add(new_item);
            }
            
            BsonDocument inner_items = new BsonDocument("items", updated_list_items);
            updated_list.append("$set", inner_items);
        }

        if(!updated_list.isEmpty()) {
            collection.updateOne(filter, updated_list);
            System.out.println("Update to list \"" + new_name + "\" was successful!");
        }
        else {
            System.out.println("Nothing changed. Update to list \"" + new_name + "\" was cancelled...");
        }
    }

    /* Delete an existing list from the database */
    public void deleteList(String list_name) {
        BsonString str = new BsonString(list_name);
        BsonDocument filter = new BsonDocument("title", str);
        boolean display_message = false;

        if(Exists(list_name)) {
            display_message = true;
        }

        collection.deleteOne(filter);

        if(display_message) {
            System.out.println("Deletion of list \"" + list_name + "\" was successful!");
        }
    }

    /* Check if a list with the given name already exists in the database */
    public boolean Exists(String list_name) {
        boolean result = false;

        BsonString bson_list_name = new BsonString(list_name);
        BsonDocument bson_list = new BsonDocument("title", bson_list_name);
        
        if(collection.find(bson_list).first() != null) {
            result = true;
        }

        return result;
    }

    /* TO BE DELETED */
    public void doNothing() {}

    /* TO BE DELETED */
    public void RepopulateTestData() {
        String christmas = "Christmas";
        String daily = "Daily";
        String[] christmas_titles = new String[] {"Buy Gifts", "Wrap Gifts", "Hide Gifts", "Transport Gifts"};
        String[] daily_titles = new String[] {"Read Book", "Study Japanese", "Play Forager", "Call Mom", "Go Grocery Shopping",
                                              "Cook Breakfast", "Cook Dinner", "Clean Dishes", "Go to the Gym", "Pay Electric Bill",
                                              "Work", "Clean the Bathroom", "Order Energy Drinks"};
        String item_name;
        boolean item_checked = false;
        ArrayList<Document> christmas_items = new ArrayList<>();
        ArrayList<Document> daily_items = new ArrayList<>();
        Document document = new Document();

        for(int i = 0; i < 4; i++) {
            Document item = new Document();
            item_name = christmas_titles[i];

            item.append("name", item_name);
            item.append("checked", item_checked);
            christmas_items.add(item);
        }

        document.append("title", christmas);
        document.append("items", christmas_items);

        if(!Exists(christmas)) {
            collection.insertOne(document);

            System.out.println("Creation of list \"" + christmas + "\" was successful!");
        }

        for(int i = 0; i < 13; i++) {
            Document item = new Document();
            item_name = daily_titles[i];

            item.append("name", item_name);
            item.append("checked", item_checked);
            daily_items.add(item);
        }

        document.clear();
        document.append("title", daily);
        document.append("items", daily_items);

        if(!Exists(daily)) {
            collection.insertOne(document);

            System.out.println("Creation of list \"" + daily + "\" was successful!");
        }
    }
 }
