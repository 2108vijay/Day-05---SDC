package org.example.employee;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    private static final String URI = "mongodb://localhost:27017"; // Change if using Atlas
    private static MongoClient client = null;

    public static MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(URI);
        }
        return client.getDatabase("EmployeeDB");
    }
}