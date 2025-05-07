package stream;

import java.security.PrivateKey;

public class Main {
    public static void main(String[] args) {
        try {
            // Substitua os valores abaixo pelas informações do seu ambiente
            String tenancyId = "SEU_TENANCY_OCID";
            String userId = "SEU_USER_OCID";
            String keyFingerprint = "SEU_FINGERPRINT";
            PrivateKey privateKey = PrivateKeyLoader.loadPrivateKey("CAMINHO_PARA_SUA_CHAVE_PRIVADA");

            String streamEndpoint = "SEU_STREAM_ENDPOINT";
            String streamOcid = "SEU_STREAM_OCID";
            String partition = "SUA_PARTICAO";

            OracleCloudAuthenticator authenticator = new OracleCloudAuthenticator(tenancyId, userId, keyFingerprint, privateKey);
            StreamHandler streamHandler = new StreamHandler(streamEndpoint, authenticator);

            // Cria uma mensagem no stream
            streamHandler.createMessage(streamOcid);

            // Cria um cursor para o stream
            String cursor = streamHandler.createCursor(streamOcid, partition);
            if (cursor != null) {
                // Lê mensagens do stream usando o cursor
                streamHandler.readMessages(streamOcid, cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}