package de.ipbhalle.io;

import java.io.File;
import java.io.FilenameFilter;

/**
 * An implementation of the FilenameFilter interface, providing various 
 * constructors to specify which substrings should occur in the filename
 * in order to accept it.
 * 
 * @author mgerlich
 *
 */
public class FileNameFilterImpl implements FilenameFilter {

	/**
	 * the prefix that should be looked for
	 */
	private String prefix = "";
	
	/**
	 * the suffix that should be looked for
	 */
	private String suffix = "";
	
	/**
	 * the String which should not be present in filename
	 */
	private String not = "";
	
	/**
	 * the standard constructor - prefix and not are set to nothing, with suffix
	 * being set to .txt
	 * so all files ending with .txt are accepted 
	 */
	public FileNameFilterImpl() {
		// set standard values
		this.prefix = "";	// accept all files
		this.suffix = ".txt";	// only accept txt files
		this.not = "";
	}
	
	/**
	 * both prefix and suffix are set to infix
	 * not is set to be empty
	 * @param infix - the String that should occur anywhere
	 */
	public FileNameFilterImpl(String infix) {
		this.prefix = (infix.isEmpty() ? "" : infix);	// check if files contain infix
		this.suffix = (infix.isEmpty() ? "" : infix);	// check if files contain infix
		this.not = "";
	}
	
	/**
	 * not is set to be empty
	 * @param prefix - the String that should occur at the beginning of the filename
	 * @param suffix - the String that should occur at the end of the filename
	 */
	public FileNameFilterImpl(String prefix, String suffix) {
		this.prefix = (prefix.isEmpty() ? "" : prefix);	// file must start with this prefix
		this.suffix = (suffix.isEmpty() ? "" : suffix);	// file must have this ending or type
		this.not = "";
	}
	
	/**
	 * 
	 * @param prefix - the String that should occur at the beginning of the filename
	 * @param suffix - the String that should occur at the end of the filename
	 * @param not - the String that should not occur anywhere in the filename
	 */
	public FileNameFilterImpl(String prefix, String suffix, String not) {
		this(prefix, suffix);
		this.not = (not.isEmpty() ? "" : not);
	}
	
	@Override
	public boolean accept(File dir, String name) {
		if(prefix.isEmpty() && suffix.isEmpty() && not.isEmpty()) {	// all empty - use all files
			return true;
		}
		else if(prefix.equals(suffix)) {				// prefix equals suffix - use them as infix
			if(name.contains(prefix) && not.isEmpty())
				return true;
			else if(name.contains(prefix) && !name.contains(not))
				return true;
			else return false;
		}
		else if(prefix.isEmpty() && !suffix.isEmpty()) {	// prefix empty but suffix not
			if(name.endsWith(suffix) && not.isEmpty())
				return true;
			
			if(name.endsWith(suffix) && !name.contains(not))
				return true;
			else return false;
		}
		else if(!prefix.isEmpty() && suffix.isEmpty()) {	// prefix not empty but suffix
			if(name.startsWith(prefix) && not.isEmpty())
				return true;
			else if(name.startsWith(prefix) && !name.contains(not))
				return true;
			else return false;
		}
		else {							// all values set
			if(name.startsWith(prefix) && name.endsWith(suffix) && !name.contains(not))
				return true;
			else return false;
		}
	}
}
