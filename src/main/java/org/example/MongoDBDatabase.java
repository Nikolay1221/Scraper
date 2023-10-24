package org.example;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBDatabase {
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBDatabase(String connectionString, String dbName) {
        MongoClientURI uri = new MongoClientURI(connectionString);
        this.mongoClient = new MongoClient(uri);
        this.database = mongoClient.getDatabase(dbName);
    }

    public void insertAd(Ad ad, String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document adDocument = ad.toDocument();
        collection.insertOne(adDocument);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}