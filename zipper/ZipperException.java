package zipper;

import zip.Zipper.ZIP_ERROR;

/**
 * Zipper exceptions
 * 
 * @author <a href="mailto:bcnyorch@gmail.com">Jordi Castilla</a>
 *
 */
public class ZipperException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ZipperException(String target, ZIP_ERROR error) {
	    super(target + " " + error.toString().replace("_", " "));
	}
}
