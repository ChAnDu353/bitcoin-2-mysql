package com.zanet.btcsql;
 
import java.util.Arrays; 
 
import org.bitcoinj.core.Base58; 
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
  
public class ScriptToPublicAddress {
 
 
	static String inputAddress = "PUSHDATA(73)[30460221008111f865148023914a2f8783ce9149165a12031c389d960119615ac22fc71d52022100fcf054db4f1ebaee90d1d9c3c696bcc4cab5349a0ea7cfbe40fbe1d09dff0e6301] PUSHDATA(65)[04c3ae65b3f30c34305efed15fdb6f50e8f539b277db9206a6d841ab2b942f17e29b3788c3cebb9d219e16708390d3dbca16bcdb5209549d12e6c304ba2f085ba3]";
	static String pubkeyinhex  = "04c3ae65b3f30c34305efed15fdb6f50e8f539b277db9206a6d841ab2b942f17e29b3788c3cebb9d219e16708390d3dbca16bcdb5209549d12e6c304ba2f085ba3";
 
	public String  getAddress(  String pubkeyhex ) {

		byte[] byteAAdr = hexStringToByteArray(pubkeyhex);
		byte[] s1 = SHA256hash(byteAAdr);

		byte[] s2 = RIPEMD160(s1); // version added here in same function

		byte[] s4 = SHA256hash(s2);

		s4 = SHA256hash(s4);
		
		byte[] chksum =  Arrays.copyOfRange(s4, 0, 4);

		byte[] bitcoinaddress = concateByteArray(s2, chksum);

		String pubAddress = Base58.encode(bitcoinaddress); 

		return pubAddress;	
	}
	
	public byte[] hexStringToByteArray(String hStr) {
        if (hStr != null) {
            int len = hStr.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hStr.charAt(i), 16) << 4)
                        + Character.digit(hStr.charAt(i + 1), 16));
            }
            return data;
        }
        return new byte[0];
    }
	  
private byte[] SHA256hash(byte[] tobeHashed){
		SHA256Digest digester=new SHA256Digest(); 
		byte[] retValue=new byte[digester.getDigestSize()]; 
		digester.update(tobeHashed, 0, tobeHashed.length); 
		digester.doFinal(retValue, 0);
	    return retValue; 
}
 
private   byte[] RIPEMD160(byte[] tobeHashed){
	RIPEMD160Digest digester = new RIPEMD160Digest();
	byte[] retValue=new byte[digester.getDigestSize()]; 
	digester.update(tobeHashed, 0, tobeHashed.length); 
	digester.doFinal(retValue, 0);	   

	byte[] version = new byte[]{0x00};
	return concateByteArray(version,retValue);	
}
 
private   byte[] concateByteArray(byte[] firstarray, byte[] secondarray){
	byte[] output = new byte[firstarray.length + secondarray.length];
	int i=0;
	for(i=0;i<firstarray.length;i++){
	    output[i] = firstarray[i];
	}
	int index=i;
	for(i=0;i<secondarray.length;i++){
	  output[index] = secondarray[i];
	  index++;
	}
	return output;
}
  
  
}