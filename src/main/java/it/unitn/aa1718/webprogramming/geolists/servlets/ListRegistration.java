/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unitn.aa1718.webprogramming.geolists.servlets;

import it.unitn.aa1718.webprogramming.geolists.database.AccessDAO;
import it.unitn.aa1718.webprogramming.geolists.database.CatProductListDAO;
import it.unitn.aa1718.webprogramming.geolists.database.IsFriendDAO;
import it.unitn.aa1718.webprogramming.geolists.database.ProductListDAO;
import it.unitn.aa1718.webprogramming.geolists.database.models.Access;
import it.unitn.aa1718.webprogramming.geolists.database.models.CatList;
import it.unitn.aa1718.webprogramming.geolists.database.models.ProductList;
import it.unitn.aa1718.webprogramming.geolists.database.models.User;
import it.unitn.aa1718.webprogramming.geolists.database.models.UserAnonimous;
import it.unitn.aa1718.webprogramming.geolists.utility.UserUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 *
 * @author tommaso
 */
@WebServlet(
        name = "ListRegistration",
        urlPatterns = {"/ListRegistration"}
)
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class ListRegistration extends HttpServlet {

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

        UserUtil util = new UserUtil();

        // Prendo l'utente dalla sessione
        HttpSession session = request.getSession();
        Optional<User> userOpt
                = (Optional<User>) util.getUserOptional(request);
        Optional<UserAnonimous> userAnonOpt
                = (Optional<UserAnonimous>) util.getUserAnonymousOptional(request);

        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "addList":
                    Pair<Boolean, String> success = addList(request, response, userOpt, userAnonOpt);
                    if (success.getKey()) {
                        response.sendRedirect("/");
                    } else {
                        response.setContentType("text/html");
                        request.setAttribute("formError", success.getValue());
                        request.setAttribute("action", "viewForm");
                        request.getRequestDispatcher("/ROOT/AddList.jsp").forward(request, response);
                    }
                    break;
                case "removeList":
                    removeList(request, response, userOpt, userAnonOpt);
                    response.sendRedirect("/");
                    break;
                case "viewForm":
                default:
                    viewForm(request, response, userOpt, userAnonOpt);
                    request.getRequestDispatcher("ROOT/AddList.jsp").forward(request, response);
            }
        } else {
            response.setContentType("text/html;charset=UTF-8");
            request.setAttribute("error", "BAD REQUEST");
            request.getRequestDispatcher("/ROOT/error/Error.jsp").forward(request, response);
        }
    }

    private void viewForm(HttpServletRequest request, HttpServletResponse response,
            Optional<User> userOpt, Optional<UserAnonimous> userAnonOpt)
            throws ServletException, IOException {

        UserUtil util = new UserUtil();
        Optional<Long> userID = util.getUserOptionalID(request);

        if (userID.isPresent()) {

            request.getSession().setAttribute("isAnon", false);
            request.getSession().setAttribute("id", userID.get());

            //mostra gli amici tra cui sciegliere
            IsFriendDAO friendDAO = new IsFriendDAO();
            List<User> friends = friendDAO.getFriends(userID.get());
            request.getSession().setAttribute("friends", friends);

        } else if (userAnonOpt.isPresent()) {
            request.getSession().setAttribute("isAnon", true);
        }

        List<CatList> possibleCategories = getListCategories();
        request.getSession().setAttribute("categories", possibleCategories);

    }

    private Pair<Boolean, String> addList(HttpServletRequest request, HttpServletResponse response,
            Optional<User> userOpt, Optional<UserAnonimous> userAnonOpt)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String description = request.getParameter("description");

        Boolean success_bool = false;
        String success_string = "";

        if (name.equals("") || description.equals("")) {
            success_bool = false;
            success_string = name.equals("") ? "Name is missing" : "Description is missing";
        } else {
            success_bool = true;

            InputStream image = null;
            long catID = Long.parseLong(request.getParameter("category"));
            //id of friends that have possibility to modify list
            String[] friends = request.getParameterValues("friends");

            long userID = 0;
            long userAnonID = 0;
            boolean anonHasAlreadyList = false;
            if (userOpt.isPresent()) {
                userID = userOpt.get().getId();
            } else if (userAnonOpt.isPresent()) {
                userAnonID = userAnonOpt.get().getId();
                ProductListDAO uaDAO = new ProductListDAO();
                Optional<ProductList> pl = uaDAO.getListAnon(userAnonID);
                anonHasAlreadyList = pl.isPresent();
            }

            System.out.println(anonHasAlreadyList);
            if (!anonHasAlreadyList) {
                // Image retrieval
                try {
                    Part filePart = request.getPart("image");
                    if (filePart != null) {
                        // obtains input stream of the upload file
                        image = filePart.getInputStream();
                    }
                } catch (IOException | ServletException ex) {
                    Logger.getLogger(ItemRegister.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Create ProductList to insert into DB
                ProductList newList = new ProductList(userID, userAnonID, catID, name, description, image);

                // Add it to DB        
                ProductListDAO plDAO = new ProductListDAO();
                Optional<Long> listID = plDAO.createAndReturnID(newList);

                AccessDAO accessDAO = new AccessDAO();
                // If not anonymous user, add to Access
                if (userOpt.isPresent()) {
                    accessDAO.create(new Access(userID, listID.get(), true));
                }
                //inserisco gli amici nella lista
                if (friends != null) {
                    for (String i : friends) {
                        accessDAO.create(new Access(Long.valueOf(i), listID.get(), false));
                    }
                }

            } else {//utente anonimo possiede gia una lista
                try {
                    response.setContentType("text/html;charset=UTF-8");
                    request.setAttribute("error", "YOU CAN'T CREATE ANOTHER");
                    getServletContext().getRequestDispatcher("/ROOT/error/Error.jsp").forward(request, response);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return new Pair<>(success_bool, success_string);
    }

    private List<CatList> getListCategories() {
        CatProductListDAO clDAO = new CatProductListDAO();
        return clDAO.getAll();
    }

    private void removeList(HttpServletRequest request, HttpServletResponse response, Optional<User> userOpt, Optional<UserAnonimous> userAnonOpt) {
        long listID = Long.parseLong(request.getParameter("listID"));

        // Rimuovo direttamente da CLIST
        // Cascade dovrebbe rimuovere dalle altre, tipo ACCESS
        ProductListDAO plDAO = new ProductListDAO();
        plDAO.delete(listID);
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
