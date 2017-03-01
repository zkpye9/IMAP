import java.io.*;
import java.net.*;
import java.util.Date;

public class Proxy extends Thread {

	static public void main(String[] args) throws IOException {
		int localPort, remotePort;
		String remoteHost;
		OutputStream inTraffic = null, outTraffic = null;
		boolean multi = false, verbose = false;
		int latency = -1, bufferSize = -1;

		try {
			int arg = 0;

			while (args[arg].charAt(0) == '-') {
				if (args[arg].startsWith("-v")) {
					arg++;
					System.err.println("Verbose output enabled");
					verbose = true;
				}

				if (args[arg].startsWith("-o")) {
					arg++;
					String logName = args[arg++];
					if (logName.equals("out")) {
						if (verbose)
							System.err.println("Outputting traffic to console");
						inTraffic = outTraffic = System.out;
					} else {
						if (verbose)
							System.err.println(
									"Outputting traffic to " + logName + "_in.log and " + logName + "_out.log");
						inTraffic = new FileOutputStream(logName + "_in.log");
						outTraffic = new FileOutputStream(logName + "_out.log");
					}
				}

				if (args[arg].startsWith("-m")) {
					arg++;
					if (verbose)
						System.err.println("Allowing multiple simultaneous connections");
					multi = true;
				}

				if (args[arg].startsWith("-l")) {
					arg++;
					latency = Integer.parseInt(args[arg++]);
					if (verbose)
						System.err.println("Latency = " + latency + "ms");
				}

				if (args[arg].startsWith("-b")) {
					arg++;
					bufferSize = Integer.parseInt(args[arg++]);
					if (verbose)
						System.err.println("Buffer size = " + bufferSize + " bytes");
				}
			}

			localPort = Integer.parseInt(args[arg++]);
			remoteHost = args[arg++];
			remotePort = Integer.parseInt(args[arg++]);
		} catch (Exception e) {
			System.err.println("usage: java org.mpr.util.TCPTunnel [options] local-port remote-host remote-port");
			System.err.println("  where options include:");
			System.err.println("    [-o(utput) {out|<file>}]  Log traffic to console or a file");
			System.err.println("    [-m(ulti)]                Allow multiple simultaneous connections");
			System.err.println("    [-v(erbose)]              Print connect/disconnect/error information");
			System.err.println("    [-l(atency) millis]       Read latency (set low for faster response; default=30)");
			System.err.println(
					"    [-b(uffer) bytes]         Read buffer (set high for better throughput; default=32768)");
			return;
		}

		Proxy tunnel = new Proxy(localPort, remoteHost, remotePort, inTraffic, outTraffic);
		tunnel.setVerbose(verbose);
		tunnel.setMultiConnection(multi);
		if (latency > 0)
			tunnel.setLatency(latency);
		if (bufferSize > 0)
			tunnel.setBufferSize(bufferSize);
		tunnel.run();
	}

	private int localPort, remotePort;
	private String remoteHost;
	private ServerSocket listener;
	private OutputStream inTraffic, outTraffic;
	private boolean verbose, multi;
	private int latency, bufferSize;

	public Proxy(int localPort, String remoteHost, int remotePort) throws IOException {
		this(localPort, remoteHost, remotePort, null, null);
	}

	public Proxy(int localPort, String remoteHost, int remotePort, OutputStream inTraffic, OutputStream outTraffic)
			throws IOException {
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.inTraffic = inTraffic;
		this.outTraffic = outTraffic;
		listener = new ServerSocket(localPort);
		verbose = false;
		multi = true;
		latency = bufferSize = -1;
	}

	/** Turns debugging output on and off. False by default. */

	public boolean getVerbose() {
		return verbose;
	}

	/** Turns debugging output on and off. False by default. */

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Allows or disallows multiple simultaneous connections. True by default.
	 */

	public boolean getMultiConnection() {
		return multi;
	}

	/**
	 * Allows or disallows multiple simultaneous connections. True by default.
	 * If this property is false, the tunnel forces connections to queue up
	 * single file, which makes for cleaner logs.
	 */

	public void setMultiConnection(boolean multi) {
		this.multi = multi;
	}

	/**
	 * Returns the maximum number of bytes the tunneler will hold in each
	 * internal buffer. The default buffer size is 32k.
	 */

	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Returns the maximum number of bytes the tunneler will hold in each
	 * internal buffer.
	 */

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Returns the time in milliseconds which this tunnel will sleep waiting for
	 * new input. The default latency is 30ms.
	 */

	public int getLatency() {
		return latency;
	}

	/**
	 * Sets the time in milliseconds which this tunnel will sleep waiting for
	 * new input. A longer latency may speed things up if a lot of data is
	 * moving through the pipe, but may slow things down if the data is arriving
	 * slowly or in bursts.
	 */

	public void setLatency(int latency) {
		this.latency = latency;
	}

	public void run() {
		try {
			while (true) {
				// Get connection

				if (verbose)
					System.err.println("TCPTunnel listening...");
				System.out.println("Before listening");
				SocketTunnel tunnel = new SocketTunnel(listener.accept(), remoteHost, remotePort);
				System.out.println("Succeed listening");
				if (multi)
					tunnel.start();
				else
					tunnel.run();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println("TCPTunnel stopped");
		}
	}

	private class SocketTunnel extends Thread {
		private Socket remoteSock, bounceSock;
		private String bounceHost;
		private int bouncePort;
		public SocketTunnel(Socket remoteSock, String bounceHost, int bouncePort) {
			this.remoteSock = remoteSock;
			this.bounceHost = bounceHost;
			this.bouncePort = bouncePort;
		}

		public void run() {
			// Log connection

			PrintWriter outMsg = null, inMsg = null;
			if (verbose) {
				if (outTraffic != null)
					outMsg = new PrintWriter(new OutputStreamWriter(outTraffic));
				if (inTraffic != null)
					inMsg = new PrintWriter(new OutputStreamWriter(inTraffic));

				String msg = "Got connection on port " + localPort + " from " + remoteSock.getInetAddress().toString()
						+ " at " + new java.util.Date();

				System.err.println(msg);
				if (outMsg != null)
					outMsg.println(msg);
				if (inMsg != null)
					inMsg.println(msg);
			}

			StreamSplitter splitIn = null, splitOut = null;
			try {
				// Connect

				bounceSock = new Socket(bounceHost, bouncePort);

				// Set up splitters

				OutputStream[] inSplits = { inTraffic, bounceSock.getOutputStream() },
						outSplits = { outTraffic, remoteSock.getOutputStream() };
				splitIn = new StreamSplitter(remoteSock.getInputStream(), inSplits);
				splitOut = new StreamSplitter(bounceSock.getInputStream(), outSplits);
				splitIn.setVerbose(verbose);
				splitOut.setVerbose(verbose);
				if (latency != -1) {
					splitIn.setLatency(latency);
					splitOut.setLatency(latency);
				}
				if (bufferSize != -1) {
					splitIn.setBufferSize(bufferSize);
					splitOut.setBufferSize(bufferSize);
				}

				// Chug

				splitIn.start();
				splitOut.start();
				while (!splitIn.isDone() && !splitOut.isDone())
					Thread.sleep(162);
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			// Close sockets

			try {
				remoteSock.close();
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			try {
				bounceSock.close();
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			splitIn.interrupt();
			splitOut.interrupt();

			// Log closure

			if (verbose) {
				String msg = "Connection from " + remoteSock.getInetAddress().toString() + " closed at "
						+ new java.util.Date() + "  In bytes: " + (splitIn != null ? splitIn.getByteCount() : 0)
						+ "  Out bytes: " + (splitOut != null ? splitOut.getByteCount() : 0);

				System.err.println(msg);
				if (outMsg != null)
					outMsg.println(msg);
				if (inMsg != null)
					inMsg.println(msg);
			}
		}
	}
	// public static void main(String argv[]) throws Exception
	// {
	// String clientSentence;
	// String capitalizedSentence;
	// ServerSocket welcomeSocket = new ServerSocket(8080);
	//
	// while(true)
	// {
	// Socket connectionSocket = welcomeSocket.accept();
	// BufferedReader inFromClient =
	// new BufferedReader(new
	// InputStreamReader(connectionSocket.getInputStream()));
	// DataOutputStream outToClient = new
	// DataOutputStream(connectionSocket.getOutputStream());
	// clientSentence = inFromClient.readLine();
	// System.out.println("Received: " + clientSentence);
	// capitalizedSentence = clientSentence.toUpperCase() + '\n';
	// outToClient.writeBytes(capitalizedSentence);
	// }
	// }
}