/*
	COS332 - Practical 3
	Hugo Greyvenstein	13019989
	Andre Calitz		13020006
*/
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.BufferedOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;

public class HttpCalculator {
	public static void main(String args[]) {
		try {
			ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
			String calculationString = "";

			System.out.println("-- waiting for client to connect --");
			Socket talkingSocket = server.accept();

			System.out.println("-- new connection made --");

			
			// temporary infinite loop
			while (true) {
				Scanner socketReader = new Scanner(talkingSocket.getInputStream());
				socketReader.useDelimiter("\r\n");
				PrintWriter socketOut = new PrintWriter(new BufferedOutputStream(talkingSocket.getOutputStream()));
				// request to server
				String httpRequest = "";

				System.out.println("-- awaiting http request --");	
				// does not read all text from socket
				do {
					httpRequest += socketReader.next() + "\r\n";
				} while (httpRequest.charAt(0) != 'G');
				System.out.println("-- request reveived --");
				// parse request here to determine action
				StringTokenizer tokenizer = new StringTokenizer(httpRequest, " ");
				String command = "";
				String directory = "";
				if (tokenizer.hasMoreTokens()) {
					command = tokenizer.nextToken();
					if (tokenizer.hasMoreTokens()) {
						directory = tokenizer.nextToken();
						if (directory.equals("/favicon.ico") == true) {
							directory = "";
						}
					}
					else {
						throw new Exception("No directory token");
					}
				}
				else {
					throw new Exception("No command token");
				}

				if (directory.length() > 0) {
					// calculationString += directory;
					calculationString += rectifyCalcString(directory);
				}
				// System.out.println("\t* calculationString: " + calculationString);
				System.out.println(calculationString);
				// generate html to send to server
				String htmlResponse = getHtmlText("./Html_Page/index.html");

				// manipulate html to display calculation
				int splitIndex = htmlResponse.indexOf("<p id=\"calculationDisplay\"></p>");
//				System.out.println(" * index: " + splitIndex);
				String buildingResp = htmlResponse.substring(0, splitIndex - 1);

				String tempString = "<p id=\"calculationDisplay\">" + calculationString + " = " + evaluateStringEquation(calculationString) + "</p>";
				buildingResp = buildingResp.concat(tempString);
				tempString = htmlResponse.substring(splitIndex + (new String("<p id=\"calculationDisplay\"></p>")).length(), htmlResponse.length());
				// buildingResp = buildingResp.concat(calculationString);
				buildingResp = buildingResp.concat(tempString);
				htmlResponse = buildingResp;
				// System.out.println(buildingResp);

				socketOut.write("HTTP/1.1 200 OK\r\n");
				socketOut.write("Content-Type: text/html\r\n");
				socketOut.write("Connection: closed\r\n");
				socketOut.write("Cache-control: no-cache\r\n");
				socketOut.write("Content-length: " + htmlResponse.length() + "\r\n\r\n");
				socketOut.write(htmlResponse);
				socketOut.flush();
			}			
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

	public static String rectifyCalcString(String cs) {
		StringTokenizer st = new  StringTokenizer(cs, "/");
		String newString = "";
		// newString = newString.concat(cs);
		String temp = "";
		while (st.hasMoreTokens()) {
			temp = st.nextToken();
			if (temp.length() == 1) {
				newString = newString.concat(temp);
			}
			else if (temp.equals("plus")) {
				newString = newString.concat("+");
			}
			else if (temp.equals("minus")) {
				newString = newString.concat("-");
			}
			else if (temp.equals("multiply")) {
				newString = newString.concat("*");
			}
			else {
				newString = newString.concat("/");
			}
			// System.out.println(temp);
		}
		return newString;
	}

	public static String evaluateStringEquation(String cs) {
		StringTokenizer st_numbers = new StringTokenizer(cs, "+-*/");
		StringTokenizer st_operators = new StringTokenizer(cs, "0123456789");

		int ans = 0;
		String left = "";
		String right = "";
		String operator = "";

		if (cs.length() == 0) {
			return "";
		}
	
		if (st_numbers.hasMoreTokens()) {
			left = st_numbers.nextToken();
		}
		while (st_numbers.hasMoreTokens()) {		
			if (st_numbers.hasMoreTokens()) {
				right = st_numbers.nextToken();	
			}
			if (st_operators.hasMoreTokens()) {
				operator = st_operators.nextToken();
			}
			if (left.length() > 0 && right.length() > 0 && operator.length() > 0) {
				if (operator.equals("+"))
					ans = Integer.parseInt(left) + Integer.parseInt(right);

				else if (operator.equals("-"))
					ans = Integer.parseInt(left) - Integer.parseInt(right);

				else if (operator.equals("*"))
					ans = Integer.parseInt(left) * Integer.parseInt(right);

				else if (operator.equals("/"))
					ans = Integer.parseInt(left) / Integer.parseInt(right);

			}
			left = new String((new Integer(ans)).toString());
		}

		return left;
	}
}