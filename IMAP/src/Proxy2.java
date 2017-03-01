import java.io.*;
import java.net.*;

public class Proxy2 {
	public static void main(String argv[]) throws Exception {
		String clientSentence;
		String capitalizedSentence;
		ServerSocket welcomeSocket = new ServerSocket(8080);
		System.out.println("build socket to localhost");
		//Socket connection = new Socket("imap.gmail.com", 143);
		Socket connection = new Socket("localhost", 143);
		System.out.println("build socket to imap");

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
//			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
//			clientSentence = inFromClient.readLine();
//			System.out.println("Received: " + clientSentence);
//			capitalizedSentence = clientSentence.toUpperCase() + '\n';
//			outToClient.writeBytes(capitalizedSentence);
		}
	}
}
