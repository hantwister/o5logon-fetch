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

package MitMSocket;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

class MitMSocketImpl extends SocketImpl {

	private final SocketImpl si;
	private final MitMSocketImplFactory sif;

	MitMSocketImpl(SocketImpl si, MitMSocketImplFactory sif) {
		this.si = si;
		this.sif = sif;
	}

	private Object call(String name, Class<?>[] param, Object[] args) {
		Class<?> receivingClass = si.getClass();
		
		while (receivingClass != null) {
			Method m = null;
			try {
				m = receivingClass.getDeclaredMethod(name, param);
			} catch (SecurityException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchMethodException e) {
				receivingClass = receivingClass.getSuperclass();
				continue;
			}
			
			try {
				m.setAccessible(true);
				return m.invoke(si, args);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	@Override
	public void setOption(int optID, Object value) throws SocketException {
		si.setOption(optID, value);
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		return si.getOption(optID);
	}

	@Override
	protected void create(boolean stream) throws IOException {
		call("create", new Class<?>[] { boolean.class },
				new Object[] { stream });
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		call("connect", new Class<?>[] { String.class, int.class },
				new Object[] { host, port });
	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		call("connect", new Class<?>[] { InetAddress.class, int.class },
				new Object[] { address, port });
	}

	@Override
	protected void connect(SocketAddress address, int timeout)
			throws IOException {
		call("connect", new Class<?>[] { SocketAddress.class, int.class },
				new Object[] { address, timeout });
	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {
		call("bind", new Class<?>[] { InetAddress.class, int.class },
				new Object[] { host, port });
	}

	@Override
	protected void listen(int backlog) throws IOException {
		call("listen", new Class<?>[] { int.class }, new Object[] { backlog });
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {
		call("accept", new Class<?>[] { SocketImpl.class }, new Object[] { s });
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		InputStream returned = (InputStream) call("getInputStream",
				new Class<?>[] {}, new Object[] {});

		return new MitMInputStream(sif, si, this, returned);
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		OutputStream returned = (OutputStream) call("getOutputStream",
				new Class<?>[] {}, new Object[] {});

		return new MitMOutputStream(sif, si, this, returned);
	}

	@Override
	protected int available() throws IOException {
		return (Integer) call("available", new Class<?>[] {}, new Object[] {});
	}

	@Override
	protected void close() throws IOException {
		call("close", new Class<?>[] {}, new Object[] {});
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		call("sendUrgentData", new Class<?>[] { int.class },
				new Object[] { data });
	}

	@Override
	protected FileDescriptor getFileDescriptor() {
		return (FileDescriptor) call("getFileDescriptor", new Class<?>[] {},
				new Object[] {});
	}

	@Override
	protected InetAddress getInetAddress() {
		return (InetAddress) call("getInetAddress", new Class<?>[] {},
				new Object[] {});
	}

	@Override
	protected int getLocalPort() {
		return (Integer) call("getLocalPort", new Class<?>[] {},
				new Object[] {});
	}

	@Override
	protected int getPort() {
		return (Integer) call("getPort", new Class<?>[] {}, new Object[] {});
	}

	@Override
	protected void setPerformancePreferences(int connectionTime, int latency,
			int bandwidth) {
		call("setPerformancePreferences", new Class<?>[] { int.class,
				int.class, int.class }, new Object[] { connectionTime, latency,
				bandwidth });
	}

	@Override
	protected void shutdownInput() {
		call("shutdownInput", new Class<?>[] {}, new Object[] {});
	}

	@Override
	protected void shutdownOutput() {
		call("shutdownOutput", new Class<?>[] {}, new Object[] {});
	}

	@Override
	protected boolean supportsUrgentData() {
		return (Boolean) call("supportsUrgentData", new Class<?>[] {}, new Object[] {});
	}

	@Override
	public String toString() {
		return si.toString();
	}
}
