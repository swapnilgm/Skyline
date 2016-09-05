package kdom.processor;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Swapnil
 *
 */
public class ListenerServer implements Runnable {

	private ServerSocket serverSocket;
	
	private Map<SocketAddress, ServerSideResponser> connectedThreads;
	
	/**
	 * @throws IOException 
	 * 
	 */
	public ListenerServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.connectedThreads = new LinkedHashMap<SocketAddress, ServerSideResponser>();
	}

	public void run() {
		System.out.println("Waiting for incoming request at :: " + this.serverSocket.getLocalSocketAddress());
		while(true) {
			try {
				Socket clientSocket = this.serverSocket.accept();
				SocketAddress clientSocketAddress = clientSocket.getRemoteSocketAddress();
				synchronized (this.connectedThreads) {
					if(this.connectedThreads.get(clientSocketAddress) == null) {
						ServerSideResponser serverSideResponser = new ServerSideResponser(clientSocket);
						this.connectedThreads.put(clientSocketAddress, serverSideResponser);
						Thread clientThread = new Thread(serverSideResponser);
						clientThread.start();
					}		
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
