package com.todo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.BsonArray;
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
    public Map<String, ArrayList<String>> getAllLists() {
        /* Key: Title; Value: Items
         * ex. Christmas: [Buy Gifts, Wrap Gifts, Hide Gifts, Transport Gifts]
         */
        Map<String, ArrayList<String>> lists = new HashMap<>();
        
        FindIterable<Document> docs = collection.find();

        for (Document doc : docs) {
            String title = doc.get("title").toString();
            ArrayList<String> items = doc.get("items", new ArrayList<>());

            lists.put(title, items);
        }

        return lists;
    }

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
    public ArrayList<String> getList(String list_name) {
        ArrayList<String> list = new ArrayList<>();

        BsonString str = new BsonString(list_name);
        BsonDocument filter = new BsonDocument("title", str);

        Document doc = collection.find(filter).first();

        if(doc != null) {
            list = doc.get("items", list);
        }
        else {
            System.err.println("ERROR: List \"" + list_name + "\" does not exist...");
        }

        return list;
    }

    /* Add a new list to the database */
    public void addList(String list_name, ArrayList<String> list_items) {
        Document new_list = new Document();
        new_list.append("title", list_name);
        new_list.append("items", list_items);
        
        collection.insertOne(new_list);

        System.out.println("Creation of list \"" + list_name + "\" was successful!");
    }

    /* Update an existing list in the database */
    public void updateList(String list_name, String new_name, ArrayList<String> new_list_items) {
        BsonString current_list_name = new BsonString(list_name);
        BsonDocument filter = new BsonDocument("title", current_list_name);
        BsonDocument updated_list = new BsonDocument();
        
        if(new_name != null && !new_name.equals(list_name)) {
            BsonString updated_list_name = new BsonString(new_name);
            BsonDocument inner = new BsonDocument("title", updated_list_name);
            updated_list.append("$set", inner);
        }

        if(new_list_items != null) {
            BsonArray updated_list_items = new BsonArray();

            for(String item : new_list_items) {
                BsonString temp = new BsonString(item);
                updated_list_items.add(temp);
            }
            
            BsonDocument inner = new BsonDocument("items", updated_list_items);
            updated_list.append("$set", inner);
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
        ArrayList<String> christmas_items = new ArrayList<>();
        ArrayList<String> daily_items = new ArrayList<>();
        Document document = new Document();

        christmas_items.add("Buy Gifts");
        christmas_items.add("Wrap Gifts");
        christmas_items.add("Hide Gifts");
        christmas_items.add("Transport Gifts");

        document.append("title", christmas);
        document.append("items", christmas_items);

        if(!Exists(christmas)) {
            collection.insertOne(document);

            System.out.println("Creation of list \"" + christmas + "\" was successful!");
        }

        daily_items.add("Read Book");
        daily_items.add("Study Japanese");
        daily_items.add("Play Forager");
        daily_items.add("Call Mom");
        daily_items.add("Go Grocery Shopping");
        daily_items.add("Cook Breakfast");
        daily_items.add("Cook Dinner");
        daily_items.add("Clean Dishes");
        daily_items.add("Go to the Gym");
        daily_items.add("Pay Electric Bill");
        daily_items.add("Work");
        daily_items.add("Clean the Bathroom");
        daily_items.add("Order Energy Drinks");

        document.clear();
        document.append("title", daily);
        document.append("items", daily_items);

        if(!Exists(daily)) {
            collection.insertOne(document);

            System.out.println("Creation of list \"" + daily + "\" was successful!");
        }
    }
 
    /* TO BE DELETED */
    public String example(){
        String result;
        ArrayList<String> items;
        Document doc = collection.find().first();

        if (doc != null) {
            result = "ID: " + doc.get("_id").toString();
            result += " Title: " + doc.get("title").toString();
            result += " Items: ";
            
            items = doc.get("items", new ArrayList<>());

            for (int i = 0; i < items.size(); i++) {
                result += items.get(i);

                if(i < items.size()-1) {
                    result += ", ";
                }
            }
        } else {
            result = "No matching documents found.";
        }
        
        System.out.println(result);
        
        return result;
    }
 }
