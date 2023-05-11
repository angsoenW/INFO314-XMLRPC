import java.io.*;
import java.net.*;
import java.net.http.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import org.w3c.dom.Node;

/**
 * This approach uses the java.net.http.HttpClient classes, which
 * were introduced in Java11.
 */
public class Client {
    static String host;
    static String port;

    public static void main(String... args) throws Exception {
        if (args.length != 2) {
            System.out.println("not a valid call");
        } else {

            host = args[0];
            port = args[1];
        }
        System.out.println(add() == 0);
        System.out.println(add(1, 2, 3, 4, 5) == 15);
        System.out.println(add(2, 4) == 6);
        System.out.println(subtract(12, 6) == 6);
        System.out.println(multiply(3, 4) == 12);
        System.out.println(multiply(1, 2, 3, 4, 5) == 120);
        System.out.println(divide(10, 5) == 2);
        System.out.println(modulo(10, 5) == 0);
    }

    public static int add(int lhs, int rhs) throws Exception {
        return calculation("add", new Integer[] { lhs, rhs });
    }

    public static int add(Integer... params) throws Exception {
        return calculation("add", (Integer[]) params);
    }

    public static int subtract(int lhs, int rhs) throws Exception {
        return calculation("subtract", new Integer[] { lhs, rhs });
    }

    public static int multiply(int lhs, int rhs) throws Exception {
        return calculation("multiply", new Integer[] { lhs, rhs });
    }

    public static int multiply(Integer... params) throws Exception {
        return calculation("multiply", (Integer[]) params);
    }

    public static int divide(int lhs, int rhs) throws Exception {
        return calculation("divide", new Integer[] { lhs, rhs });
    }

    public static int modulo(int lhs, int rhs) throws Exception {
        return calculation("modulo", new Integer[] { lhs, rhs });
    }

    private static int calculation(String methodName, Integer... params) {
        try {
            HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

            String ansTree = "<params>";

            if (params.length == 0) {
                ansTree += "<param><value><i4>" + 0 + "</i4></value></param>";
            } else {
                for (Object param : params) {

                    ansTree += "<param><value><i4>" + param.toString() + "</i4></value></param>";
                }
            }
            ansTree = ansTree + "</params>";
            String requestBody = "<?xml version = '1.0'?><methodCall><methodName>" + methodName + "</methodName>"
                    + ansTree + "</methodCall>";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + host + ":" + port + "/RPC"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .header("Content-Type", "text/xml")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(response.body().getBytes());
            System.out.println(response.body());
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(bais);
            doc.getDocumentElement().normalize();
            String result = doc.getElementsByTagName("string").item(0).getTextContent();

            return Integer.valueOf(result);

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }
}
