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
    private IOManip ioManip;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final Lock r = lock.readLock();
    private final Lock w = lock.writeLock();

    public Client(Socket server, int clientNumber){
        this.server = server;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintStream out = new PrintStream(server.getOutputStream());
            ioManip = new IOManip(out);
            IOManip serverio = new IOManip(System.out);

            serverio.setColor(clientNumber);
            System.out.println("Client " + clientNumber + " connected.");


            ioManip.clearConsole();
            ioManip.moveTo(0,0);
            out.println("Welcome to the server. Here are the commands you can enter:");
            out.println(
                    "QUIT: Close connection to server.\n" + //done
                    "ADD <name> <number>: Add a friend with name and number to database.\n" + //done
                    "DEL <name> <number>: Deletes friend with name and number from database.\n" + //done
                    "FIND <name/number>: Prints friend details if found in database.\n" + //done
                    "UPDNUMBER <name> <new number>: Updates number of friend with name if in database.\n" + //done
                    "UPDNAME <old name> <new name>: Updates name of friend to new name.\n" +
                    "PRINT: Prints entire database."); //done


            while ((line = in.readLine()) != null && !line.equals("QUIT")){
                String[] cmdrcvd = line.split(" ",2);

                if (Arrays.asList(cmd).contains(cmdrcvd[0])) {
                    serverio.setColor(clientNumber);
                    out.println("ECHO: " + line);

                    switch (cmdrcvd[0]){
                        case "ADD":{
                            String[] param;
                            if (cmdrcvd.length > 1){
                                param = cmdrcvd[1].split("\\s+");
                            } else{
                                out.println("Invalid number of paramaters for ADD. For a list of commands, type HELP.");
                                break;
                            }
                            if (param.length != 2){
                                out.println("Invalid number of paramaters for ADD. For a list of commands, type HELP.");
                                break;
                            } else{
                                if (addUser(param[0],param[1], out)){
                                }
                                System.out.println("Client " + clientNumber + ": " +line);
                            }
                            break;
                        }
                        case "HELP":{
                            System.out.println("Client " + clientNumber + ": " +line);
                            out.println(
                                    "QUIT: Close connection to server.\n" +
                                    "ADD <name> <number>: Add a friend with name and number to database.\n" +
                                    "DEL <name> <number>: Deletes friend with name and number from database.\n" +
                                    "FIND <name/number>: Prints friend details if found in database.\n" +
                                    "UPDNUMBER <name> <new number>: Updates number of friend with name if in database.\n" +
                                    "UPDNAME <old name> <new name>: Updates name of friend to new name.\n" +
                                    "PRINT: Prints entire database.");
                            break;
                        }
                        case "PRINT":{
                            print(out);
                            System.out.println("Client " + clientNumber + ": " +line);
                            break;
                        }
                        case "FIND":{
                            String[] param;
                            if (cmdrcvd.length > 1){
                                param = cmdrcvd[1].split("\\s+");
                            } else{
                                out.println("Invalid number of paramaters for FIND. For a list of commands, type HELP.");
                                break;
                            }
                            if (param.length != 1){
                                out.println("Invalid number of paramaters for FIND. For a list of commands, type HELP.");
                                break;
                            }
                            find(param[0],out);
                            System.out.println("Client " + clientNumber + ": " +line);
                            break;
                        }
                        case "DEL":{
                            String[] param;
                            if (cmdrcvd.length > 1){
                                param = cmdrcvd[1].split("\\s+");
                            } else{
                                out.println("Invalid number of paramaters for DEL. For a list of commands, type HELP.");
                                break;
                            }
                            if (param.length != 2){
                                out.println("Invalid number of paramaters for DEL. For a list of commands, type HELP.");
                                break;
                            }

                            delete(param[0],param[1],out);
                            System.out.println("Client " + clientNumber + ": " +line);
                            break;
                        }
                        case "UPDNUMBER":{
                            String[] param;
                            if (cmdrcvd.length > 1){
                                param = cmdrcvd[1].split("\\s+");
                            } else{
                                out.println("Invalid number of paramaters for UPDNUMBER. For a list of commands, type HELP.");
                                break;
                            }
                            if (param.length != 2){
                                out.println("Invalid number of paramaters for UPDNUMBER. For a list of commands, type HELP.");
                                break;
                            }
                            updateNumber(param[0],param[1],out);
                            System.out.println("Client " + clientNumber + ": " +line);
                            break;
                        }
                        case "UPDNAME":{
                            String[] param;
                            if (cmdrcvd.length > 1){
                                param = cmdrcvd[1].split("\\s+");
                            } else{
                                out.println("Invalid number of paramaters for UPDNUMBER. For a list of commands, type HELP.");
                                break;
                            }
                            if (param.length != 2){
                                out.println("Invalid number of paramaters for UPDNUMBER. For a list of commands, type HELP.");
                                break;
                            }
                            updateName(param[0],param[1],out);
                            System.out.println("Client " + clientNumber + ": " +line);
                            break;
                        }
                        default:{
                            break;
                        }
                    }
                } else {
                    out.println("Command not recognised. For a list of commands, type HELP.");
                }
            }
            serverio.setColor(clientNumber);
            System.out.println("Client " + clientNumber + ": Connection terminated by client.");
            server.close();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            ioManip.setColor("reset");
        }
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
                ioManip.setColor(i);
                out.println(friendList.get(i).toString());
            }
            ioManip.setColor("reset");
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