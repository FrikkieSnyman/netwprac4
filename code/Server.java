// Created by:
// Fredercik Hendrik Snyman 13028741
// Hugo Greyvenstein        13019989

import java.io.*;
import java.net.*;
import java.security.*;

public class Server{
    private static int port;
    private static IOManip io = new IOManip(System.out);

    public static void main(String[] args){
        int argc = args.length;
        int connections = 0;
        if (argc != 1) {
            System.out.println("Not enough actual parameters. java Server <port>");
            System.exit(0);
        }

        port = Integer.parseInt(args[0]);

        try{

            ServerSocket listener = new ServerSocket(port);
            Socket server;

            while (true){
                server = listener.accept();
                Client client = new Client(server,connections++);
                System.out.println("New connection. Connection nr: " + connections);
                Thread t = new Thread(client);
                t.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            io.setColor("reset");
        }

    }
}