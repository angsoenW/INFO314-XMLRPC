package edu.uw.info314.xmlrpc.server;

import java.util.*;
import java.util.logging.*;
import static spark.Spark.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;

class Call {
    public String name;
    public List<Object> args = new ArrayList<Object>();
}

public class App {
    public static final Logger LOG = Logger.getLogger(App.class.getCanonicalName());

    public static void main(String[] args) {
        port(8080);
        LOG.info("Starting up on port 8080");

        // This is the mapping for POST requests to "/RPC";
        // this is where you will want to handle incoming XML-RPC requests
        before((request, response) -> {
            if (!request.uri().equals("/RPC")) {
                halt(404, "Not Found");
            }
            if (!request.requestMethod().equals("POST")) {
                halt(405, "Method Not Allowed");
            }
        });

        post("/RPC", (request, response) -> {
            Call call = extractXMLRPCCall(request.body());

            int[] params = new int[call.args.size()];
            for (int i = 0; i < params.length; i++) {
                params[i] = (int)call.args.get(i);  
            }

            Calc calc = new Calc();
            if (call.name.equals("add")) {
                return getResponse(calc.add(params));
            } else if (call.name.equals("subtract")) {
                return getResponse(calc.subtract(params[0], params[1]));
            } else if (call.name.equals("multiply")) {
                return getResponse(calc.multiply(params));
            } else if (call.name.equals("divide")) {
                if (params[1] == 0) {
                    return faultString(1, "divide by zero");
                }
                return getResponse(calc.divide(params[0], params[1]));
            } else if (call.name.equals("modulo")) {
                if (params[1] == 0) {
                    return faultString(1, "divide by zero");
                }
                return getResponse(calc.modulo(params[0], params[1]));
            } else {
               return halt(401, "Not Implemented");
            }

        });

    }

        private static String getResponse(int ans) {
            return "<?xml version=\"1.0\"?><methodResponse><params><param><value><string>" 
                    + ans
                    + "</string></value></param></params></methodResponse>" ;
        }

        public static String faultString (int faultCode, String msg) {
            return "<methodResponse><fault><value><struct><member><name>faultCode</name><value><int>"
                    + faultCode
                    + "</int></value></member><member><name>faultString</name><value><string>"
                    + msg
                    + "</string></value></member></struct></value></fault></methodResponse>"
            ;
        }

        

        // Each of the verbs has a similar format: get() for GET,
        // put() for PUT, delete() for DELETE. There's also an exception()
        // for dealing with exceptions thrown from handlers.
        // All of this is documented on the SparkJava website (https://sparkjava.com/).
    

    public static Call extractXMLRPCCall(String body) {
        Call call = new Call();
        List<Object> params = new ArrayList<>();

        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            ByteArrayInputStream content = new ByteArrayInputStream(body.getBytes());
            DocumentBuilder db = fact.newDocumentBuilder();
            Document doc = db.parse(content);
            doc.getDocumentElement().normalize();

            call.name = doc.getElementsByTagName("methodName").item(0).getTextContent();

            NodeList list = doc.getElementsByTagName("i4");

            for (int i = 0; i < list.getLength(); i++) {
                params.add(Integer.valueOf(list.item(i).getTextContent()));
            }
            call.args = params;

        } catch (Exception e) {
            return null;
        }

        return call;

    }
}
