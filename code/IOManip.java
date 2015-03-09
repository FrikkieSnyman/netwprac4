// Created by:
// Fredercik Hendrik Snyman 13028741
// Hugo Greyvenstein        13019989

import java.io.*;

public class IOManip{

    private PrintStream out;

    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private String[] colours = {RED,GREEN,YELLOW,BLUE,PURPLE,CYAN,WHITE};


    public IOManip(PrintStream out){
        this.out = out;
    }

    public void clearConsole(){
        writeESC();
        out.print("[2J");
    }

    public void moveTo(int x, int y){
        writeESC();
        out.print("["+x+";"+y+"H");
    }

    public void setColor(String color){
        color = color.toUpperCase();
        switch (color){
            case "RESET":
                out.print(RESET);
                break;
            case "BLACK":
                out.print(BLACK);
                break;
            case "RED":
                out.print(RED);
                break;
            case "GREEN":
                out.print(GREEN);
                break;
            case "YELLOW":
                out.print(YELLOW);
                break;
            case "BLUE":
                out.print(BLUE);
                break;
            case "PURPLE":
                out.print(PURPLE);
                break;
            case "CYAN":
                out.print(CYAN);
                break;
            case "WHITE":
                out.print(WHITE);
                break;
            default:break;
        }
    }

    public void setColor(int i){
        out.print(colours[i % 7]);
    }

    private void writeESC(){
        out.write(27);
    }
}