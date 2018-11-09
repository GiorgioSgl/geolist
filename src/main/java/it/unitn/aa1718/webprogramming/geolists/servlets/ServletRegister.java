package it.unitn.aa1718.webprogramming.geolists.servlets;

import it.unitn.aa1718.webprogramming.geolists.utility.EmailSender;
import java.util.Random;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import it.unitn.aa1718.webprogramming.geolists.database.UserDAO;
import it.unitn.aa1718.webprogramming.geolists.database.models.User;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServletRegister extends HttpServlet {
    
    Random rand = new Random();
    String username, email, name, lastname, password,
            cookie= Integer.toString(rand.nextInt(5000000)+1), 
            image = "IMAGEN", token;
    boolean admin=false, active=false;

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void  doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        //prendo valori variabili dalla richiesta
        this.username = request.getParameter("UserName");
        this.name = request.getParameter("FirstName");
        this.lastname = request.getParameter("LastName");
        this.email = request.getParameter("Email");
        this.password = request.getParameter("Password");

        
        //controllo esistenza user
        boolean unameNew = isUnameNew(this.username);
        //controllo esistenza email
        boolean emailNew = isEmailNew(this.email);
        //controllo la email
        boolean emailCheck = emailCtrl(this.email);
        //controllo la password
        boolean passCheck = passwordCtrl(this.password);
        
        
        if(!passCheck){  //controllo password
            System.out.println("PASSWORD NON CORRETTA, DEVE CONTENERE UN NUMERO, UNA LETTERA E UN CARATTERE SPECIALE");    
        }else if (!emailCheck){  //controllo la mail
            System.out.println("EMAIL NON CORRETTA, DEVE CONTENERE UNA @,UN CARATTARE DAVANTI ALLA \"@\", UN \".\" DOPO LA @ E CON UN DOMINIO DOPO IL \".\"");
        }else if (!unameNew){
            System.out.println("USER GIA PRESENTE NEL DATABASE");
        }else if (!emailNew){
            System.out.println("EMAIL GIA PRESENTE NEL DATABASE");
        }else{
            //creo il token (PER ORA A RANDOM)
            this.token = DigestUtils.md5Hex(""+this.rand.nextInt(999999999));

            //creo user che andrò a ficcare nel database e lo inserisco
            User u = new User(this.cookie, this.username, this.name, this.lastname, 
                              this.email, this.password, this.image, this.token, false, false);
            UserDAO UD = new UserDAO();
            UD.create(u);

            //invio l'email attraverso l'email sender
            EmailSender es = new EmailSender(email,token);
            es.sendEmail();
        }
        
    }
    
    
    /**
     * funzione che controlla che la password abbia le caratteristiche richieste
     * @param password la password da controllare
     * @return true nel caso la password abbia almeno una lettera, un numero e un carattere speciale
     * false altrimenti
     */
    private boolean passwordCtrl(String password){
        
        // creo i pattern necessari per il controllo
        Pattern letters = Pattern.compile("[a-zA-z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile("[^A-Za-z0-9]");
       
        // creo i matcher che controllano i pattern
        Matcher hasLetters = letters.matcher(password);
        Matcher hasNumber = digit.matcher(password);
        Matcher hasSpecial = special.matcher(password);
        
        return hasLetters.find() && hasNumber.find() && hasSpecial.find();
    }
    
    
    /**
     * function that check if the email is written correctly
     * @param email email to check
     * @return true if the email is written correctly false otherwise
     */
    private boolean emailCtrl(String email){
        
        // controllo esistenza della "@"
        if(!email.contains("@")){
            return false;
        }else{
            if(email.indexOf("@") == 0){
                return false;
            }else{
                email = email.substring(email.indexOf("@"));
                // controllo esistenza "."
                if(!email.contains(".")){
                    return false;
                }else{
                    // controllo esistenza di una stringa dopo il punto
                    if(email.endsWith(".")){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    
    /**
     * function that check if the username does already exist in the database
     * @param username username to check
     * @return false if the username is already in the DB true otherwise
     */
    private boolean isUnameNew(String username){
        
        UserDAO db = new UserDAO();
        Optional<User> u = db.get(username);
        if(u.isPresent())
            return false;
        
        return true;
    }
    
    
    /**
     * function that check if the email does already exist in the database
     * @param email username to check
     * @return false if the email is already in the DB true otherwise
     */
    private boolean isEmailNew(String email){
        
        email = email.toLowerCase();
        
        UserDAO db = new UserDAO();
        Optional<User> u = db.getFromEmail(email);
        if(u.isPresent())
            return false;
        
        return true;
    }
}
