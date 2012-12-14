package Test;


/**
 *
 * The class <code>JpipDataSegment</code> is used to construct objects
 * to store segments of JPIP data. These segments can be data-bin segments
 * as well as EOR messages. In this last case, the EOR code is stored in
 * the <code>id</code> field and the EOR message body is stored in the 
 * <code>data</code> field.
 *
 * @author Juan Pablo Garcia Ortiz
 * @see JpipDataInputStream
 * @version 0.1
 *
*/
public class JpipDataSegment
{
	/**
		The data-bin in-class identifier.
	*/
	public long id;
	
	/**
		The data-bin auxiliary information.
	*/
	public long aux;
	
	/**
		The data-bin class identifier.
	*/
	public long classId;
	
	/**
		The code-stream index.
	*/
	public long codestream;
	
	/**
		Offset of this segment within the data-bin data.
	*/
	public long offset;
	
	/**
		Length of this segment.
	*/
	public long length;
	
	/**
		The segment data.
	*/
	public byte data[];
	
	/**
		Indicates if this segment is the last one (when there is
		a data segment stream).
	*/
	public boolean isFinal;
	
	/**
		Indicates if this segment is a End Of Response message.
	*/
	public boolean isEOR;
	
	/**
		Returns a string representation of the JPIP data segment.
	*/
	public String toString()
	{
		String res;
		
		res = getClass().getName() + " [";
		if(isEOR) res += "EOR id=" + id + " len=" + length;
		else {
			res += "class=" + classId + " stream=" + codestream;
			res += " id=" + id +  " off=" + offset + " len=" + length;
			if(isFinal) res += " final";
		}
		res += "]";
		
		return res;
	}
}
		
		 
		