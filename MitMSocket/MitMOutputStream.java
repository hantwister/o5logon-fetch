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
import java.io.OutputStream;
import java.net.SocketImpl;


class MitMOutputStream extends OutputStream {
	
	private MitMSocketImplFactory sif;
	private SocketImpl si;
	private MitMSocketImpl msi;
	private OutputStream os;
	
	MitMOutputStream(MitMSocketImplFactory sif, SocketImpl si, MitMSocketImpl msi, OutputStream toReturn) {
		this.sif = sif;
		this.si = si;
		this.msi = msi;
		this.os = toReturn;
	}
	
	private boolean notifyListener(byte[] b) {
		MitMAction a = sif.getListener().socketOutputWrite(si, os, b);
		
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

	@Override
	public void write(int b) throws IOException {
		if (notifyListener(new byte[] {(byte) b})) {
			os.write(b);
		}
	}

	@Override
	public void write(byte b[]) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (b.length == 0) {
			return;
		}
		
		if (notifyListener(b.clone())) {
			os.write(b);
		}
    }

	@Override
	public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || (off + len) > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        
        byte[] temp = new byte[len];
        System.arraycopy(b, off, temp, 0, len);
        
        if (notifyListener(temp)) {
        	os.write(b, off, len);
        }
        
    }

	@Override
	public void flush() throws IOException {
		os.flush();
    }

	@Override
    public void close() throws IOException {
		os.close();
    }

}