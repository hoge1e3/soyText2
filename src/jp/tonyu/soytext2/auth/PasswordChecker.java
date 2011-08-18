package jp.tonyu.soytext2.auth;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novell.ldap.util.Base64;
 
/**
 * The class like md5sum command.
 */
public class PasswordChecker {
 
  private static String digest(String algorithm, byte[] input)
    throws NoSuchAlgorithmException{
 
    MessageDigest md5 = MessageDigest.getInstance(algorithm);
    byte[] output     = md5.digest(input);
 
    String outdata = Base64.encode(output);
    return outdata;
  }
 
   public static void main(String[] args) throws NoSuchAlgorithmException {
	   System.out.println(match("foo", "{MD5}rL0Y20zC+Fzt72VPzMSk2A=="));
   } 
   static Pattern fieldPat=Pattern.compile("^\\{([^}]+)\\}(.*)$");
   public static boolean match(String rawInput, String userPasswordField) throws NoSuchAlgorithmException {
	   Matcher m=fieldPat.matcher(userPasswordField);
	   boolean res=false;
	   if (m.matches()) {
		   res= digest(m.group(1),rawInput.getBytes()).equals(m.group(2));
		   
	   }	   
	   //res=true;
	   System.out.println("Matching with '"+userPasswordField+"' - "+res);
	   return res;
   }
}
