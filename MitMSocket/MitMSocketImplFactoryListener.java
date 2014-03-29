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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketImpl;

public interface MitMSocketImplFactoryListener {
	public MitMAction socketInputRead(SocketImpl si, InputStream is, byte[] b);
	public MitMAction socketOutputWrite(SocketImpl si, OutputStream os, byte[] b);
}
