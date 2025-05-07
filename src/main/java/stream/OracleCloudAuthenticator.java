package stream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

public class OracleCloudAuthenticator {

    private final String tenancyId;
    private final String userId;
    private final String keyFingerprint;
    private final PrivateKey privateKey;

    public OracleCloudAuthenticator(String tenancyId, String userId, String keyFingerprint, PrivateKey privateKey) {
        this.tenancyId = tenancyId;
        this.userId = userId;
        this.keyFingerprint = keyFingerprint;
        this.privateKey = privateKey;
    }

    public String generateAuthHeader(String httpMethod, String requestPath, String dateHeader) throws Exception {
        String signingString = "(request-target): " + httpMethod.toLowerCase() + " " + requestPath + "\n" +
                               "date: " + dateHeader;

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signingString.getBytes(StandardCharsets.UTF_8));

        String signatureBase64 = Base64.getEncoder().encodeToString(signature.sign());

        return "Signature version=\"1\",keyId=\"" + tenancyId + "/" + userId + "/" + keyFingerprint + "\",algorithm=\"rsa-sha256\",headers=\"(request-target) date\",signature=\"" + signatureBase64 + "\"";
    }
}