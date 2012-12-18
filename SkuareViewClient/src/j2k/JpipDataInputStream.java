package j2k;

import java.io.IOException;
import java.io.InputStream;
import java.io.EOFException;

import java.net.ProtocolException;


/**
 *
 * The class <code>JpipDataInputStream</code> allows to read JPIP data-bin segments, 
 * as it is defined in the Part 9 of the the JPEG2000 standard.
 * 
 * @author Juan Pablo Garcia Ortiz
 * @see java.io.InputStream
 * @see JpipDataSegment
 * @version 0.1
 *
 */
public class JpipDataInputStream 
{
	/**
		The last class identifier read.
	*/
	private long classId = 0;				
	
	/**
		The last code-stream index read.
	*/
	private long codestream = 0;		
	
	/**
		The total length in bytes of the last VBAS read.
	*/
	private int vbasLength = 0;
	
	/**
		The first byte of the last VBAS read.
	*/
	private int vbasFstByte = 0;
	
	/**
		The <code>InputStream</code> base.
	*/
	private InputStream in;
	
	
	/**
	  Constructs a object based on the indicated <code>InputStream</code>.
	*/
	public JpipDataInputStream(InputStream in)
	{
		this.in = in;
	}
	
	/**
		Reads an VBAS integer from the stream. The length in bytes of the
		VBAS is stored in the <code>vbasLength</code>variable, and the first
		byte of the VBAS is stored in the <code>vbasFstByte</code> variable.
		
		@throws java.io.IOException
	*/
	private long readVBAS() throws IOException
	{
		int c;
		long value = 0;
		
		vbasLength = 0;
		
		do {
			if(vbasLength >= 9) throw new ProtocolException("VBAS length not supported");	
			
			if((c = in.read()) < 0) {
				if(vbasLength > 0) throw new EOFException("EOF reached before completing VBAS");
				else return -1;
			}
			
			value = (value << 7) | (long)(c & 0x7F);
			
			if(vbasLength == 0) vbasFstByte = c;
			vbasLength++;
			
		} while((c & 0x80) != 0);
		
		return value;	
	}
	
	/**
		Reads the next data segment from the stream, adn stores its information in
		the <code>JpipDataSegment</code> object passed as parameter. The data buffer
		is not reallocated every time. It is only reallocated if the next data length
		is bigger than the previous one.
		
		@param segment Object where to store the data.
		@throws java.io.IOException
		@return Returns <code>true</code> if a new data segment was read, 
						or <code>false</code> if the end of stream was reached.
	*/
	public boolean readSegment(JpipDataSegment segment) throws IOException
	{
		int m;
		long id;
		
		if((id = readVBAS()) < 0) return false;
		
		segment.id = id;
		
		if(vbasFstByte == 0) {
			segment.isEOR = true;
			
			if((segment.id = in.read()) < 0)
				throw new EOFException("EOF reached before completing EOR message");
				
			segment.length = readVBAS();
			
		} else {
			segment.isEOR = false;
			segment.id &= (long)~(0x70 << ((vbasLength - 1) * 7));

			segment.isFinal = ((vbasFstByte & 0x10) != 0);

			m = (vbasFstByte & 0x7F) >> 5;

			if(m == 0) throw new ProtocolException("Invalid Bin-ID value format"); 
			else if(m >= 2) {
				classId = readVBAS();
				if(m > 2) codestream = readVBAS();
			}

			segment.classId = classId;
			segment.codestream = codestream;

			segment.offset = readVBAS();
			segment.length = readVBAS();

			if((classId == JpipConstants.EXTENDED_PRECINCT_DATA_BIN_CLASS) ||
				(classId == JpipConstants.EXTENDED_TILE_DATA_BIN_CLASS))
				segment.aux = readVBAS(); 
		}
		
		if(segment.length > 0) {
			if(segment.data == null) 
				segment.data = new byte[(int)segment.length];
			else if(segment.data.length < segment.length)
				segment.data = new byte[(int)segment.length];

			if(in.read(segment.data, 0, (int)segment.length) != segment.length) 
				throw new EOFException("EOF reached before read " + segment.length + " bytes");
		}
		
		return true;
	}
}
		
		 
		