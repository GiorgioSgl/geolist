/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unitn.aa1718.webprogramming.geolists.servlets;

import it.unitn.aa1718.webprogramming.geolists.database.CatProductListDAO;
import it.unitn.aa1718.webprogramming.geolists.database.ComposeDAO;
import it.unitn.aa1718.webprogramming.geolists.database.ItemDAO;
import it.unitn.aa1718.webprogramming.geolists.database.ProductListDAO;
import it.unitn.aa1718.webprogramming.geolists.database.UserDAO;
import it.unitn.aa1718.webprogramming.geolists.database.models.CatList;
import it.unitn.aa1718.webprogramming.geolists.database.models.Compose;
import it.unitn.aa1718.webprogramming.geolists.database.models.Item;
import it.unitn.aa1718.webprogramming.geolists.database.models.ProductList;
import it.unitn.aa1718.webprogramming.geolists.database.models.User;
import it.unitn.aa1718.webprogramming.geolists.utility.UserUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mattia
 */
@WebServlet(name = "PremadeListsServlet", urlPatterns = {"/PremadeLists"})
public class PremadeListsServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //mi ricavo lo user dal coockie
        UserUtil uUtil = new UserUtil();
        Optional<User> userOptional = uUtil.getUserOptional(request);
        boolean isAdmin = false;
        if(userOptional.isPresent()){
            isAdmin = userOptional.get().isAdmin();
        }
        
        //prendo le liste premade
        List<ProductList> lists = new ProductListDAO().getPremade();
        
        //prendo gli item di quella lista
        Map<Long, List<Item>> itemsOfList = new HashMap<>();
        for (ProductList list : lists) {
                long listID = list.getId();

                //item della lista
                List<Compose> relationList = new ComposeDAO().getItemsID(listID);
                List<Item> items = new ArrayList<>();
                for (Compose rel : relationList) {
                    Optional<Item> itemOpt = new ItemDAO().get(rel.getIdItem());
                    if (itemOpt.isPresent()) {
                        items.add(itemOpt.get());
                    }

                }
                itemsOfList.put(listID, items);
            }
        
        //prendo le categorie da mettere al posto dei nomi
        Map<Long, String> mapCatOfLists = new HashMap<>();
        List<CatList> listProductList = new CatProductListDAO().getAll();
        for (CatList elem : listProductList) {
            mapCatOfLists.put(elem.getIdCategory(), elem.getName());
        }
        
        

        request.setAttribute("mapCatOfLists", mapCatOfLists);
        request.setAttribute("lists", lists);
        request.setAttribute("itemsOfList", itemsOfList);
        request.setAttribute("isAdmin", isAdmin);
        request.getRequestDispatcher("/ROOT/lists/PremadeLists.jsp").forward(request, response);

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
