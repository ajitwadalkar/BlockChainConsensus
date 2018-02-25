import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PublicKeyEncoder {

    public static final char[] alphabet = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    public static final BigInteger base_count = new BigInteger("58");


    static String PubkeyEnc(String strToHash){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(strToHash.getBytes(StandardCharsets.UTF_8));
        byte[] ripeHash = Ripemd160.getHash(hash);
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < ripeHash.length; i++) {
            String hex = Integer.toHexString(0xff & ripeHash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return Base58Encode(hexString.toString());
    }

    static String Base58Encode(String str) {
        BigInteger decimal = new BigInteger(str, 16);
        StringBuffer res = new StringBuffer();
        while (decimal.signum() > 0) {
            char c = alphabet[decimal.mod(base_count).intValue()];
            res.append(c);
            decimal = decimal.divide(base_count);
        }
        return res.reverse().toString();
    }

    static String Base58Decode(String str) {
        BigInteger decimal =  new BigInteger("0");
        char []chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char temp_c = chars[i];
            int index_num = 0;
            for (int j = 0; j < alphabet.length; j++) {
                if (alphabet[j] == temp_c) {
                    index_num = j;
                }
            }
            BigInteger indexNum = BigInteger.valueOf(index_num);
            decimal = decimal.multiply(base_count);
            decimal = decimal.add(indexNum);
        }
        return  decimal.toString(16);
    }


}


