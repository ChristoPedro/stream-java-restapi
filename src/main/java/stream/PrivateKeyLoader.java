package stream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PrivateKeyLoader {

    public static PrivateKey loadPrivateKey(String pemFilePath) throws Exception {
        StringBuilder keyBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(pemFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("-----BEGIN") && !line.startsWith("-----END")) {
                    keyBuilder.append(line);
                }
            }
        }

        byte[] keyBytes = Base64.getDecoder().decode(keyBuilder.toString());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}