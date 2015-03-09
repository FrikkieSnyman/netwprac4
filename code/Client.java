// Created by:
// Fredercik Hendrik Snyman 13028741
// Hugo Greyvenstein        13019989

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.locks.*;

@SuppressWarnings("unchecked")
public class Client implements Runnable{
    private Socket server;
    private String line;
    private String db = "Address/db.txt";
    private String[] cmd = {"QUIT","ADD","DEL","FIND","UPDNUMBER","UPDNAME","PRINT","HELP"};
    private ArrayList<Friend> friendList = null;
    private int clientNumber;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock r = lock.readLock();
    private final Lock w = lock.writeLock();

    public Client(Socket server, int clientNumber){
        this.server = server;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run(){
        try {
                // addUser("Frikkie","0827079070");
                Scanner socketReader = new Scanner(server.getInputStream());
                
                PrintWriter socketOut = new PrintWriter(new BufferedOutputStream(server.getOutputStream()));
             
                String getRequest = socketReader.next();
                String urlRequest = socketReader.next();

                handleGETRequest(urlRequest, socketOut);

                System.out.println(getRequest + " " +  urlRequest);
        }
        catch (IOException e) {
            System.out.println("-- closing program due to IOException being thrown --");
            System.out.println("-- " + e.getCause() + " --");
        }
        catch (Exception e) {
            // System.out.println("-- exception thrown closing server --");
            // System.out.println("-- " + e.getMessage() + " --");
            // e.printStackTrace();
        } finally {
            System.out.println("Responded to request");
        }
    }

    private void handleGETRequest(String getRequest, PrintWriter socketOut){
        if (getRequest.compareTo("/") == 0){
            try{
                String httpResponse = getHtmlText("Html_Page/index.html");
                printHtmlResponse(httpResponse,socketOut);                
            } catch (Exception e){
                e.printStackTrace();
            }
        } else{
            String[] splitGetRequest = getRequest.split("\\?",-1);
//**************************************************************************************
// PRINT ALL USERS
//**************************************************************************************            
            if (splitGetRequest[0].compareTo("/print") == 0){
                try{
                    String httpResponse = getHtmlText("Html_Page/print.html");
                    String[] splitHtml = httpResponse.split("\\?\\?\\?",2);
                    socketOut.write("HTTP/1.1 200 OK\r\n");
                    socketOut.write("Content-Type: text/html\r\n");
                    splitHtml[0] += print();
                    splitHtml[0] += splitHtml[1];
                    socketOut.write("Content-length: " + splitHtml[0].length() + "\r\n");
                    socketOut.write("\r\n" + splitHtml[0] + "\r\n\r\n");
                    socketOut.flush();

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
//**************************************************************************************

//**************************************************************************************
// ADD USER            
//**************************************************************************************
            else if (splitGetRequest[0].compareTo("/add") == 0) {
                try {
                    String httpResponse = getHtmlText("Html_Page/add.html");
                    String[] splitHtml = httpResponse.split("\\?\\?\\?",2);
                    socketOut.write("HTTP/1.1 200 OK\r\n");
                    socketOut.write("Content-Type: text/html\r\n");

                    String[] parameters = splitGetRequest[1].split("\\&");
                    // System.out.println("-- Name: " + parameters[0]);
                    // System.out.println("-- Number: " + parameters[1]);
                    System.out.println("Adding new user");
                    String tempName = parameters[0].substring(5, parameters[0].length());
                    String tempNum = parameters[1].substring(7, parameters[1].length());
                    String tempPhoto = parameters[2];

                    System.out.println("-- Name: ." + tempName.replace('+', ' ') + ".");
                    System.out.println("-- Number: ." + tempNum + ".");
                    System.out.println("-- Photo: ." + tempPhoto + ".");
                    addUser(
                            tempName.replace('+', ' '),
                            tempNum
                        );

                    splitHtml[0] += print();
                    splitHtml[0] += splitHtml[1];
                    socketOut.write("Content-length: " + splitHtml[0].length() + "\r\n");
                    socketOut.write("\r\n" + splitHtml[0] + "\r\n\r\n");
                    socketOut.flush();                                
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
//**************************************************************************************

//**************************************************************************************
// FIND USER
//**************************************************************************************            
            else if (splitGetRequest[0].compareTo("/find") == 0) {
                try {
                    String httpResponse = getHtmlText("Html_Page/find.html");
                    String[] splitHtml = httpResponse.split("\\?\\?\\?", 3);
                    socketOut.write("HTTP/1.1 200 OK\r\n");
                    socketOut.write("Content-Type: text/html\r\n");

                    String[] parameters = splitGetRequest[1].split("\\&");
                    // System.out.println("-- Name: " + parameters[0]);
                    // System.out.println("-- Number: " + parameters[1]);
                    
                    String tempName = parameters[0].substring(5, parameters[0].length());
                    String tempNum = parameters[1].substring(7, parameters[1].length());

                    // System.out.println("-- Name: ." + tempName.replace('+', ' ') + ".");
                    // System.out.println("-- Number: ." + tempNum + ".");
                    
                    String databaseResponseNameSearch = find(tempName.replace('+', ' '), null);
                    String databaseResponseNumberSearch = find(tempNum, null);
                    databaseResponseNameSearch.replaceAll("\n", "<br />");
                    databaseResponseNumberSearch.replaceAll("\n", "<br />");

                    splitHtml[0] += databaseResponseNameSearch;
                    splitHtml[0] += splitHtml[1];
                    splitHtml[0] += databaseResponseNumberSearch;
                    splitHtml[0] += splitHtml[2];
                    socketOut.write("Content-length: " + splitHtml[0].length() + "\r\n");
                    socketOut.write("\r\n" + splitHtml[0] + "\r\n\r\n");
                    socketOut.flush();                                
                } catch (Exception e) {
                    e.printStackTrace();
                }                
            }
//**************************************************************************************

//**************************************************************************************
// DELETE USER
//**************************************************************************************            
            else if (splitGetRequest[0].compareTo("/delete") == 0){
                try{
                    String httpResponse = getHtmlText("Html_Page/delete.html");
                    String[] splitHtml = httpResponse.split("\\?\\?\\?",2);
                    socketOut.write("HTTP/1.1 200 OK\r\n");
                    socketOut.write("Content-Type: text/html\r\n");

                    String[] parameters = splitGetRequest[1].split("\\&");
                    String tempName = parameters[0].substring(5, parameters[0].length());
                    String tempNum = parameters[1].substring(7, parameters[1].length());

                    if (delete(tempName, tempNum)){
                        splitHtml[0] += "Contact succesfully deleted";
                        splitHtml[0] += splitHtml[1];
                        socketOut.write("Content-length: " + splitHtml[0].length() + "\r\n");
                        socketOut.write("\r\n" + splitHtml[0] + "\r\n\r\n");
                        socketOut.flush();                             
                    } else{
                        splitHtml[0] += "No contact deleted";
                        splitHtml[0] += splitHtml[1];
                        socketOut.write("Content-length: " + splitHtml[0].length() + "\r\n");
                        socketOut.write("\r\n" + splitHtml[0] + "\r\n\r\n");
                        socketOut.flush();
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
//**************************************************************************************
        }
    }

    private static void printHtmlResponse(String httpResponse, PrintWriter socketOut){
        socketOut.write("HTTP/1.1 200 OK\r\n");
        socketOut.write("Content-Type: text/html\r\n");
        socketOut.write("Content-length: " + httpResponse.length() + "\r\n");
        socketOut.write("\r\n" + httpResponse + "\r\n\r\n");
        socketOut.flush();
    }

    private static String getHtmlText(String fp) throws FileNotFoundException, IOException {
        // File file = new File(fp);
        FileReader reader = new FileReader(fp);
        BufferedReader br = new BufferedReader(reader);
        String ret = "";
        String temp = "";
        while ((temp = br.readLine()) != null && temp.length() > 0) {
            ret += temp;
        }
        return ret;
    }

    private Boolean updateName(String oldName, String newName){
        Boolean found = false;
        r.lock();
        try{
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(oldName)){
                    friendList.get(i).setName(newName);
                    System.out.println("Friend contact details updated.");
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("No such friend found in database.");
            }
        } finally {
            r.unlock();
        }

        w.lock();
        try{
            printFile(friendList);
        } finally {
            w.unlock();
        }

        return found;
    }

    private Boolean updateNumber(String name, String newNumber){
        Boolean found = false;
        r.lock();
        try{
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(name)){
                    friendList.get(i).setNumber(newNumber);
                    System.out.println("Friend contact details updated.");
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("No such friend found in database.");
            }
        } finally {
            r.unlock();
        }

        w.lock();
        try{
            printFile(friendList);
        } finally {
            w.unlock();
        }

        return found;
    }

    private Boolean delete(String name, String number){
        Friend contact = getFriendWithName(name);

        if (contact != null && contact.getNumber().equals(number)){
            r.lock();
            try {
                friendList = read();

                for (int i = 0; i < friendList.size(); ++i){
                    if (friendList.get(i).getName().equals(contact.getName())){
                        friendList.remove(i);
                        break;
                    }
                }
            } finally {
                r.unlock();
            }
            w.lock();
            try{
                printFile(friendList);
            } finally {
                w.unlock();
            }
            System.out.println("Contact deleted.");
            return true;
        } else{
            System.out.println("No such person with name and number combination in database");
            return false;
        }
    }

    private String find(String criteria, PrintStream out){
        String digits = "[0-9]+";
        String returnString = "";

        if (criteria.matches(digits)){
            returnString = findByNumber(criteria,out);
        } else{
            returnString = findByName(criteria,out);
        }
        return returnString;
    }

    private String findByNumber(String criteria, PrintStream out){
        r.lock();
        try{
            String returnString = "";
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getNumber().equals(criteria)) {
                    if (out != null) {
                        out.println("Contact found.");
                        out.println(friendList.get(i).toString());                        
                    }
                    // returnString = "Contact found";
                    returnString = friendList.get(i).toString();
                    returnString += "\n";
                    return returnString;
                }
            }
            if (out != null)
                out.println("Contact not in database.");
            returnString = "Contact not in database.\n";
            return returnString;
        } finally {
            r.unlock();
        }
    }

    private String findByName(String criteria, PrintStream out){
        r.lock();
        try {
            String returnString;
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(criteria)){
                    if (out != null) {
                        out.println("Contact found.");
                        out.println(friendList.get(i).toString());                        
                    }
                    // returnString = "Contact found";
                    returnString = friendList.get(i).toString();
                    returnString += "\n";
                    return returnString;
                }
            }
            if (out != null)
                out.println("Contact not in database.");
            returnString = "Contact not in database.\n";
            return returnString;
        } finally {
            r.unlock();
        }
    }

    private Friend getFriendWithName(String name){
        r.lock();
        try {
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(name)){
                    return friendList.get(i);
                }
            }
        } finally {
            r.unlock();
        }

        return null;
    }

    private Friend getFriendWithNumber(String number){
        r.lock();
        try {
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getNumber().equals(number)){
                    return friendList.get(i);
                }
            }
        } finally {
            r.unlock();
        }

        return null;
    }


    private Boolean addUser(String name, String number){

        String digits = "[0-9]+";
        String alpha = "[A-Za-z ]+";

        if (!(number.matches(digits)) || !(name.matches(alpha)) || (number.length() != 10)){
            System.out.println("Please make sure that <name> contains only letters and <number> contains strictly 10 numbers.");
            return false;
        }

        Friend friend = new Friend(name,number);

        w.lock();
        try {
            friendList = read();
            friendList.add(friend);
            printFile(friendList);
        } finally {
            w.unlock();
        }

        return true;
    }

    private String print(){
        String returnThis = "";
        r.lock();
        try {
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i) {
                returnThis += friendList.get(i).toString() + "<br />";
            }
        } finally {
            r.unlock();
        }

        return returnThis;
    }

    private void printFile(ArrayList<Friend> friendList){
        try {
            FileOutputStream fout = new FileOutputStream(db);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fout);
            objectOutputStream.writeObject(friendList);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<Friend> read(){
        try {
            ArrayList<Friend> temp = new ArrayList<>();
            FileInputStream fileInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                fileInputStream = new FileInputStream(db);
                objectInputStream = new ObjectInputStream(fileInputStream);
                temp = (ArrayList<Friend>) objectInputStream.readObject();

            } catch (EOFException e) {
                e.printStackTrace();
            }

            if (temp == null) {
                temp = new ArrayList<>();
            }

            if (objectInputStream != null) {
                objectInputStream.close();
            }
            return temp;
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}