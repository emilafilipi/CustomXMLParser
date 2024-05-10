package nSoftwareTask2;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//Callback interface
interface XMLDataHandler {
    void handleStartElement(String elementName, Map<String, String> attributes);
    void handleStartElement(String elementName);
    void handleAttribute(String name, String value);
    void handleText(String elementName);
    void handleEndElement(String elementName);
}

//Used to store parsed data in a tree-like data structure that preserves the hierarchical relationship between tags
class Node {
    String name;
    Map<String, String> attributes = new HashMap<>();
    List<Node> children = new ArrayList<>();
    StringBuilder text = new StringBuilder();

    Node(String name) {
        this.name = name;
    }

    void addAttributes(Map attributes) {
        this.attributes.putAll(attributes);
    }

    void appendText(String additionalText) {
        text.append(additionalText);
    }

    void addChild(Node child) {
        children.add(child);
    }
}
public class XMLParser {
    private XMLDataHandler handler;
    private Stack<Node> nodeStack = new Stack<>();
    private Stack<String> pathStack = new Stack<>();
    private Node root = null;

    public XMLParser(){

    }
    public XMLParser(XMLDataHandler handler) {
        this.handler = handler;
    }
    public void setHandler(XMLDataHandler handler) {
        this.handler = handler;
    }

    //Parse the XML document and save it into a tree data structure
    public void parseAsTree(String source) {
        try {
            InputStream inputStream;

            //Checks if the XML document comes from a file or URL
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URL url = new URL(source);
                URLConnection connection = url.openConnection();
                inputStream = connection.getInputStream();
            } else {
                Path filePath = Path.of(source);
                inputStream = Files.newInputStream(filePath);
            }

            //Set the delimiting String
            Scanner scanner = new Scanner(inputStream).useDelimiter("<");

            while (scanner.hasNext()) {
                String part = scanner.next();

                //Skip XML declarations
                if (part.startsWith("?") || part.startsWith("!DOCTYPE")) {
                    continue;
                }

                //Skip comments
                if (part.startsWith("!--")) {
                    //Continue scanning until the end of the comment is found
                    while (scanner.hasNext() && !part.contains("-->")) {
                        part = scanner.next();
                    }
                    continue;
                }

                //Convert XML escape entities in this part before further processing
                part = decodeXMLEntities(part);

                //Check if the element is an opening or closing element
                if (part.startsWith("/"))
                    handleClosingTagAsTree(part);
                 else
                    handleOpeningTagAsTree(part);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Method that parses data and keeps track of the current path
    public void parse(String source) {
        try {
            InputStream inputStream;

            //Checks if the XML document comes from a file or URL
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URL url = new URL(source);
                URLConnection connection = url.openConnection();
                inputStream = connection.getInputStream();
            } else {
                Path filePath = Path.of(source);
                inputStream = Files.newInputStream(filePath);
            }

            Scanner scanner = new Scanner(inputStream).useDelimiter("<");

            while (scanner.hasNext()) {
                String part = scanner.next();

                //Skip XML declarations
                if (part.startsWith("?") || part.startsWith("!DOCTYPE")) continue;

                //Skip comments
                if (part.startsWith("!--")) {
                    //Continue scanning until the end of the comment is found
                    while (scanner.hasNext() && !part.contains("-->")) part = scanner.next();
                    continue;
                }

                //Convert XML escape entities in this part before further processing
                part = decodeXMLEntities(part);

                //Check if the element is an opening or closing element
                if (part.startsWith("/")) handleClosingTag(part);
                else handleOpeningTag(part);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String decodeXMLEntities(String input) {
        return input.replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    private void handleOpeningTag(String element) {
        //Save the ending point of this element
        int endIndex = element.indexOf(">");

        //Check if element is self-closing
        boolean isSelfClosing = element.endsWith("/>");

        //Get element's name
        String tagContent = isSelfClosing ? element.substring(0, endIndex - 1) : element.substring(0, endIndex);
        String tagName = tagContent.split("\\s+")[0];

        //Get element's attributes
        Map<String, String> attributes = getAttributesFromString(tagContent);

        //Update the stack that keeps track of the current path
        pathStack.push(tagName);

        //User's code
        handler.handleStartElement(tagName, attributes);
        handler.handleStartElement(tagName);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            handler.handleAttribute(entry.getKey(), entry.getValue());
        }

        //Remove from path if the element is self-closing
        if (isSelfClosing) {
            pathStack.pop();
            handler.handleEndElement(tagName);
        }
        if (endIndex + 1 < element.length())
            handleText(element.substring(endIndex + 1));
    }

    private void handleClosingTag(String element) {
        String tagName = element.substring(1, element.indexOf(">"));

        //User's code
        handler.handleEndElement(tagName);

        //Update the path
        pathStack.pop();
    }

    //Handle text
    private void handleText(String text) {
        text = text.trim();
        if (!text.isEmpty()) {
            handler.handleText(text);
        }
    }
    //Handle opening tags
    private void handleOpeningTagAsTree(String element) {
        //Save the ending point of this element
        int endIndex = element.indexOf(">");

        //Check if element is self-closing
        boolean isSelfClosing = element.endsWith("/>");

        //Get element's name
        String tagContent = isSelfClosing ? element.substring(0, endIndex - 1) : element.substring(0, endIndex);
        String tagName = tagContent.split("\\s+")[0];

        //Get element's attributes
        Map<String, String> attributes = getAttributesFromString(tagContent);


        //Save this element and its attributes into the tree
        Node newNode = new Node(tagName);
        newNode.addAttributes(attributes);
        if (!nodeStack.isEmpty()) {
            nodeStack.peek().addChild(newNode);
        }

        //Update the stack that keeps track of the current path
        nodeStack.push(newNode);

        //Make sure that the first element is stored into the root and the root is never empty
        if (root == null) {
            root = newNode;
        }

        //User's code
        handler.handleStartElement(tagName, attributes);
        handler.handleStartElement(tagName);

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            handler.handleAttribute(entry.getKey(), entry.getValue());
        }

        //Remove from path if the element is self-closing
        if (isSelfClosing) {
            nodeStack.pop();
            handler.handleEndElement(tagName);
        }

        //Text found
        if (endIndex + 1 < element.length()) {
            handleTextAsTree(element.substring(endIndex + 1));
        }
    }

    //Handle closing tags
    private void handleClosingTagAsTree(String element) {
        String tagName = element.substring(1, element.indexOf(">"));

        //User's code
        handler.handleEndElement(tagName);

        //Update the path
        nodeStack.pop();
    }

    //Handle text
    private void handleTextAsTree(String text) {
        text = text.trim();
        if (!text.isEmpty()) {

            //Add into the tree
            Node currentNode = nodeStack.peek();
            currentNode.appendText(text);

            //User's code
            handler.handleText(text);
        }
    }

    //Updates the current path
    protected String getCurrentPath() {
        if(pathStack.isEmpty())
            return nodeStack.stream().map(node -> node.name).collect(Collectors.joining("/"));
        else
            return String.join("/", pathStack);

    }

    //Save attributes and their values
    protected Map<String, String> getAttributesFromString(String elementData){
        Map<String, String> attributes = new HashMap<>();
        Matcher matcher = Pattern.compile("(\\w+)=\"([^\"]*)\"").matcher(elementData);

        while (matcher.find()) {
            //Get the attribute name (key) from group 1 and value from group 2
            attributes.put(matcher.group(1), matcher.group(2));
        }
        return attributes;
    }

    //Print the parsed data starting from the root
    public void printTree(){
        if(root != null) {
            System.out.println("\nParsed XML document: ");
            printTree(root, " ");
        }
        else
            System.out.println("Root is null.");
    }

    //Print the parsed data starting from any node, with a hierarchical indent
    public static void printTree(Node node, String indent) {
        if (node == null) return;

        //Print the current node's name and attributes
        System.out.print(indent + node.name);
        if (!node.attributes.isEmpty()) {
            System.out.print(" [");
            node.attributes.forEach((key, value) -> System.out.print("Attribute: " + key + "=" + value + " "));
            System.out.print("\b]");
        }

        //Print the text content if there is text
        if (node.text.length() > 0) {
            System.out.print(" Text: " + node.text.toString().trim());
        }
        System.out.println();

        //Recursively print each child node
        node.children.forEach(child -> printTree(child, indent + "  "));
    }
}
