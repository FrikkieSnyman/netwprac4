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
            // temporary infinite loop
            // while (true) {
                Scanner socketReader = new Scanner(server.getInputStream());
                // socketReader.useDelimiter("\n");
                PrintWriter socketOut = new PrintWriter(new BufferedOutputStream(server.getOutputStream()));
                // request to server
                String httpRequest = "";

                System.out.println("-- awaiting http request --");  
                // // does not read all text from socket
                // do {
                //     httpRequest += socketReader.next() + "\n";
                // } while (httpRequest.charAt(0) != 'G');
                // System.out.println("-- request reveived --");
                // System.out.println(httpRequest);
                // // parse request here to determine action
                // StringTokenizer tokenizer = new StringTokenizer(httpRequest, " ");
                // String command = "";
                // String directory = "";
                // if (tokenizer.hasMoreTokens()) {
                //     command = tokenizer.nextToken();
                //     if (tokenizer.hasMoreTokens()) {
                //         directory = tokenizer.nextToken();
                //         if (directory.equals("/favicon.ico") == true) {
                //             directory = "";
                //         }
                //     }
                //     else {
                //         throw new Exception("No directory token");
                //     }
                // }
                // else {
                //     throw new Exception("No command token");
                // }

                // // if (directory.length() > 0) {
                // //     // calculationString += directory;
                // //     calculationString += rectifyCalcString(directory);
                // // }
                // // System.out.println("\t* calculationString: " + calculationString);
                // // System.out.println(calculationString);
                // // generate html to send to server
                // String htmlResponse = getHtmlText("./Html_Page/index.html");

                // manipulate html to display calculation
                // int splitIndex = htmlResponse.indexOf("<p id=\"calculationDisplay\"></p>");
//              System.out.println(" * index: " + splitIndex);
                // String buildingResp = htmlResponse.substring(0, splitIndex - 1);

                // String tempString = "<p id=\"calculationDisplay\">" + calculationString + " = " + evaluateStringEquation(calculationString) + "</p>";
                // buildingResp = buildingResp.concat(tempString);
                // tempString = htmlResponse.substring(splitIndex + (new String("<p id=\"calculationDisplay\"></p>")).length(), htmlResponse.length());
                // buildingResp = buildingResp.concat(calculationString);
                // buildingResp = buildingResp.concat(tempString);
                // htmlResponse = buildingResp;
                // System.out.println(buildingResp);
                String helloWorld = "<html><body>helloWorld </body></html>";
                socketOut.write("HTTP/1.1 200 OK\r\n");
                socketOut.write("Content-Type: text/html\r\n");
                socketOut.write("Content-length: " + helloWorld.length() + "\r\n");
                // socketOut.write("Connection: closed\r\n");
                // socketOut.write("Cache-control: no-cache\r\n\r\n");
                // socketOut.write("Content-length: " + htmlResponse.length() + "\n\n");
                socketOut.write("\r\n" + helloWorld + "\r\n\r\n");
                socketOut.flush();
            // }           
        }
        catch (IOException e) {
            System.out.println("-- closing program due to IOException being thrown --");
            System.out.println("-- " + e.getCause() + " --");
        }
        catch (Exception e) {
            System.out.println("-- exception thrown closing server --");
            System.out.println("-- " + e.getMessage() + " --");
            e.printStackTrace();
        }
    }

    public static String getHtmlText(String fp) throws FileNotFoundException, IOException {
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

    private void updateName(String oldName, String newName, PrintStream out){
        Boolean found = false;
        r.lock();
        try{
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(oldName)){
                    friendList.get(i).setName(newName);
                    out.println("Friend contact details updated.");
                    found = true;
                    break;
                }
            }

            if (!found) {
                out.println("No such friend found in database.");
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
    }

    private void updateNumber(String name, String newNumber, PrintStream out){
        Boolean found = false;
        r.lock();
        try{
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(name)){
                    friendList.get(i).setNumber(newNumber);
                    out.println("Friend contact details updated.");
                    found = true;
                    break;
                }
            }

            if (!found) {
                out.println("No such friend found in database.");
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
    }

    private void delete(String name, String number, PrintStream out){
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
            out.println("Contact deleted.");
        } else{
            out.println("No such person with name and number combination in database");
        }
    }

    private void find(String criteria, PrintStream out){
        String digits = "[0-9]+";

        if (criteria.matches(digits)){
            findByNumber(criteria,out);
        } else{
            findByName(criteria,out);
        }

    }

    private void findByNumber(String criteria, PrintStream out){
        r.lock();
        try{
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getNumber().equals(criteria)){
                    out.println("Contact found.");
                    out.println(friendList.get(i).toString());
                    return;
                }
            }
            out.println("Contact not in database.");
        } finally {
            r.unlock();
        }
    }

    private void findByName(String criteria, PrintStream out){
        r.lock();
        try {
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i){
                if (friendList.get(i).getName().equals(criteria)){
                    out.println("Contact found.");
                    out.println(friendList.get(i).toString());
                    return;
                }
            }

            out.println("Contact not in database.");
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


    private Boolean addUser(String name, String number, PrintStream out){

        String digits = "[0-9]+";
        String alpha = "[A-Za-z]+";

        if (!(number.matches(digits)) || !(name.matches(alpha)) || (number.length() != 10)){
            out.println("Please make sure that <name> contains only letters and <number> contains strictly 10 numbers.");
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

    private void print(PrintStream out){
        r.lock();
        try {
            friendList = read();
            for (int i = 0; i < friendList.size(); ++i) {
                out.println(friendList.get(i).toString());
            }
        } finally {
            r.unlock();
        }
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