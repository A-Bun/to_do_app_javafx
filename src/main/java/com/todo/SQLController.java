package com.todo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SQLController {

    //Put you connection string with pw, user, ... here
    private static final String CONNECTION_STRING = "mongodb+srv://alexisp30ples:v77s8ZhiG2lQ48KV@to-do-cluster-1.efhkz.mongodb.net/?retryWrites=true&w=majority&appName=To-Do-Cluster-1"; 

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

 
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
