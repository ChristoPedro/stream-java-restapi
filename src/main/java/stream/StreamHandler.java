package stream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class StreamHandler {

    private final String streamEndpoint;
    private final OracleCloudAuthenticator authenticator;

    public StreamHandler(String streamEndpoint, OracleCloudAuthenticator authenticator) {
        this.streamEndpoint = streamEndpoint;
        this.authenticator = authenticator;
    }

    public void createMessage(String streamOcid) {
        try {
            String httpMethod = "post";
            String dateHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
            String requestPath = "/20180418/streams/" + streamOcid + "/messages";
            String authHeader = authenticator.generateAuthHeader(httpMethod, requestPath, dateHeader);

            URL url = new URL(streamEndpoint + requestPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod.toUpperCase());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Date", dateHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Gera uma mensagem aleatória e codifica em Base64
            String randomMessage = UUID.randomUUID().toString();
            String encodedMessage = java.util.Base64.getEncoder().encodeToString(randomMessage.getBytes("utf-8"));

            // Corpo da requisição para criar uma mensagem
            String requestBody = String.format(
                "{\"messages\": [{\"key\": null, \"value\": \"%s\"}]}",
                encodedMessage
            );

            // Envia o corpo da requisição
            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                System.out.println("Message created successfully: " + randomMessage);
            } else {
                handleErrorResponse(connection, responseCode, "Failed to create message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createCursor(String streamOcid, String partition) {
        try {
            String httpMethod = "post";
            String dateHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
            String requestPath = "/20180418/streams/" + streamOcid + "/cursors";
            String authHeader = authenticator.generateAuthHeader(httpMethod, requestPath, dateHeader);

            URL url = new URL(streamEndpoint + requestPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod.toUpperCase());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Date", dateHeader);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Corpo da requisição para criar o cursor
            String requestBody = String.format("{\"partition\": \"%s\", \"type\": \"TRIM_HORIZON\"}", partition);
            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                java.io.InputStream inputStream = connection.getInputStream();
                if (inputStream != null) {
                    java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    System.out.println("Cursor created successfully: " + response.toString());

                    // Extrai o cursor do JSON retornado manualmente
                    String responseString = response.toString();
                    String cursor = extractCursorFromJson(responseString);
                    System.out.println("Extracted Cursor: " + cursor);
                    return cursor;
                }
            } else {
                handleErrorResponse(connection, responseCode, "Failed to create cursor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractCursorFromJson(String json) {
        // Extrai o valor do campo "value" manualmente
        int startIndex = json.indexOf("\"value\":\"") + 9; // Posição inicial do valor
        int endIndex = json.indexOf("\"", startIndex); // Posição final do valor
        if (startIndex > 8 && endIndex > startIndex) {
            return json.substring(startIndex, endIndex);
        }
        return null; // Retorna null se o cursor não for encontrado
    }

    public void readMessages(String streamOcid, String cursor) {
        try {
            String httpMethod = "get";
            String dateHeader = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
                    .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC));
            String requestPath = "/20180418/streams/" + streamOcid + "/messages?cursor=" + cursor + "&limit=10";
            String authHeader = authenticator.generateAuthHeader(httpMethod, requestPath, dateHeader);

            URL url = new URL(streamEndpoint + requestPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(httpMethod.toUpperCase());
            connection.setRequestProperty("Authorization", authHeader);
            connection.setRequestProperty("Date", dateHeader);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Messages from Stream: " + response.toString());
            } else {
                handleErrorResponse(connection, responseCode, "Failed to read messages");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleErrorResponse(HttpURLConnection connection, int responseCode, String errorMessage) {
        try {
            java.io.InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(errorStream));
                String errorLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
                System.out.println(errorMessage + ". HTTP Response Code: " + responseCode);
                System.out.println("Error Response: " + errorResponse.toString());
            } else {
                System.out.println(errorMessage + ". HTTP Response Code: " + responseCode);
                System.out.println("No error response body received.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}