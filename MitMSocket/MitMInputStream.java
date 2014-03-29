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
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketImpl;


class MitMInputStream extends InputStream {
	
	private MitMSocketImplFactory sif;
	private SocketImpl si;
	private MitMSocketImpl msi;
	private InputStream is;
	
	MitMInputStream(MitMSocketImplFactory sif, SocketImpl si, MitMSocketImpl msi, InputStream is) {
		this.sif = sif;
		this.si = si;
		this.msi = msi;
		this.is = is;
	}
	
	private boolean notifyListener(byte[] b) {
		MitMAction a = sif.getListener().socketInputRead(si, is, b);
		
		if (a == MitMAction.CLOSE_SOCKET) {
			try {
				msi.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return false;
		} else if (a == MitMAction.DROP_DATA) {
			return false;
		}
		
		return true;
	}

    public int read() throws IOException {
    	int toReturn;
    	
    	while ((toReturn = is.read()) != -1) {
    		if (notifyListener(new byte[] {(byte) toReturn})) {
    			return toReturn;
    		}
    	}
    	
    	return -1;
    }

    public int read(byte b[]) throws IOException {
    	byte[] toReturnData = new byte[b.length];
    	int toReturnLength;
    	
        System.arraycopy(b, 0, toReturnData, 0, b.length);
    	
    	while ((toReturnLength = is.read(toReturnData)) > 0) {
        	byte[] temp = new byte[toReturnLength];
        	System.arraycopy(toReturnData, 0, temp, 0, toReturnLength);
        	
    		if (notifyListener(temp)) {
    			System.arraycopy(toReturnData, 0, b, 0, b.length);
    			return toReturnLength;
    		}
    		
            System.arraycopy(b, 0, toReturnData, 0, b.length);
    	}
    	
        return toReturnLength;
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || (off + len) > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        
        byte[] toReturnData = new byte[b.length];
        int toReturnLength;
        
        System.arraycopy(b, 0, toReturnData, 0, b.length);
        
        while ((toReturnLength = is.read(toReturnData, off, len)) > 0) {
        	byte[] temp = new byte[toReturnLength];
        	System.arraycopy(toReturnData, off, temp, 0, toReturnLength);
        	
        	if (notifyListener(temp)) {
    			System.arraycopy(toReturnData, 0, b, 0, b.length);
    			return toReturnLength;
        	}
        	
            System.arraycopy(b, 0, toReturnData, 0, b.length);
        }
        
        return toReturnLength;
    }

    public long skip(long n) throws IOException {
    	return is.skip(n);
    }

    public int available() throws IOException {
        return is.available();
    }

    public void close() throws IOException {
    	is.close();
    }

    public synchronized void mark(int readlimit) {
    	is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        is.reset();
    }

    public boolean markSupported() {
        return is.markSupported();
    }

}
