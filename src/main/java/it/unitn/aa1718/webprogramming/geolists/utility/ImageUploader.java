/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unitn.aa1718.webprogramming.geolists.utility;

import it.unitn.aa1718.webprogramming.geolists.database.CrudDao;
import it.unitn.aa1718.webprogramming.geolists.database.ItemDAO;
import it.unitn.aa1718.webprogramming.geolists.database.ProductListDAO;
import it.unitn.aa1718.webprogramming.geolists.database.models.Item;
import it.unitn.aa1718.webprogramming.geolists.database.models.ProductList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to help upload images to the Database
 * @author tommaso
 */
public class ImageUploader {
    
    private static File chooseImage(String name) {
        String userDir = System.getProperty("user.dir");
        String imagesDir = userDir + File.separator + "images" + File.separator;
        File f = new File(imagesDir + name + ".png");
        
        return f;
    }
    
    private static InputStream getInputStreamFromFile(String name) {
        File selectedFile = chooseImage(name);
        InputStream image = null;        
        try {
            image = new FileInputStream(selectedFile);
        } catch (FileNotFoundException ex) {
        }
        return image;
    }    
    
    /**
     * A class that takes an Item and if there is an image I in the images folder
     * such that I == item.getName(), it uploads it to the DB
     * @param Item item
     * @return boolean to tell if the operation succeeds
     */
    private static boolean uploadItemImage(Item item) {
        ItemDAO itemDAO = new ItemDAO();
        boolean res = false;
        InputStream image = getInputStreamFromFile("/items/" + item.getName());
        if (image != null) {
            res = true;
            item.setLogo(image);
            System.out.println("Uploading image " + item.getName() + ".png...");
            itemDAO.update(item.getId(), item);
        }
        return res;
    }
    
    /**
     * A class that takes a List and if there is an image I in the images folder
     * such that I == list.getName(), it uploads it to the DB
     * @param List list
     * @return boolean to tell if the operation succeeds
     */
    private static boolean uploadListImage(ProductList list) {
        ProductListDAO plDAO = new ProductListDAO();
        boolean res = false;
        InputStream image = getInputStreamFromFile("/lists/" + list.getName());
        if (image != null) {
            res = true;
            list.setImage(image);
            System.out.println("Uploading image " + list.getName() + ".png...");
            plDAO.update(list.getId(), list);
        }
        return res;
    }
    
    /**
     * Upload all images in /images to DB
     * @param args 
     */
    public static void main(String[] args) {
        // Items
        System.out.println("====== ITEMS ======");
        ItemDAO itemDAO = new ItemDAO();
        List<Item> allItems = itemDAO.getAll();
        for (Item item : allItems) {
            if (uploadItemImage(item)) {
                System.out.println("Item image " + item.getName() + ".png uploaded to the DB");
            } else {
                System.out.println("Item image " + item.getName() + ".png could not be found");
            }
        }
        
        // Lists
        System.out.println("\n\n====== LISTS ======");
        ProductListDAO plDAO = new ProductListDAO();
        List<ProductList> allLists = plDAO.getAll();
        for (ProductList list : allLists) {
            if (uploadListImage(list)) {
                System.out.println("Item image " + list.getName() + ".png uploaded to the DB");
            } else {
                System.out.println("Item image " + list.getName() + ".png could not be found");
            }
        }
    }
}
