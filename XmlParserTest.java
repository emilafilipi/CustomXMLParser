package nSoftwareTask2;

import java.util.HashMap;
import java.util.Map;

public class XmlParserTest {
    static Map<String, String> orderID = new HashMap<>();
    public static void main(String[] args) {
        //Save order ids and their path
        final String[] currentOrderId = new String[1];
        final String[] currentPath = new String[1];
        final boolean captureData[] = new boolean[1];
        captureData[0] = false;

        // Create an instance of the XMLParser and set the handler
        XMLParser parser = new XMLParser();
        // Define a simple XML data handler for testing
        XMLDataHandler handler = new XMLDataHandler() {
            @Override
            public void handleStartElement(String elementName, Map<String, String> attributes) {
                if (elementName.equals("order")) {

                    //Save order ids and their paths in case the amount is greater than 100
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        if(entry.getKey().equals("id"))
                            currentOrderId[0] = entry.getValue();
                    }

                    currentPath[0] = parser.getCurrentPath();
                }

                //Boolean value to check if the next text occurred is between amount tags
                if(elementName.equals("amount")){
                    captureData[0] = true;
                }
            }

            @Override
            public void handleStartElement(String elementName) {
                //System.out.println("Start Element: " + elementName + " at " + parser.getCurrentPath() );
            }

            @Override
            public void handleAttribute(String name, String value) {
                //System.out.println("Attribute: " + name + " = " + value + " at " + parser.getCurrentPath());
            }

            @Override
            public void handleText(String elementName) {
                //System.out.println("Text: " + elementName + " at " + parser.getCurrentPath());
                if(captureData[0] && Integer.parseInt(elementName) > 100)
                    orderID.put(currentOrderId[0], currentPath[0]);

            }

            @Override
            public void handleEndElement(String elementName) {
                //System.out.println("End Element: " + elementName + " at " + parser.getCurrentPath());
                if(elementName.equals("amount"))
                    captureData[0] = false;
                //System.out.println();
            }
        };

        parser.setHandler(handler);

        //Calculate how long parsing lasts
        long startTime = System.currentTimeMillis();

        //XML file in URL
        //parser.parseAsTree("https://aiweb.cs.washington.edu/research/projects/xmltk/xmldata/data/courses/reed.xml");
        //parser.parse("https://aiweb.cs.washington.edu/research/projects/xmltk/xmldata/data/courses/reed.xml");

        //Large XML file in URL
        //parser.parseAsTree("https://aiweb.cs.washington.edu/research/projects/xmltk/xmldata/data/treebank/treebank_e.xml");
        //parser.parse("https://aiweb.cs.washington.edu/research/projects/xmltk/xmldata/data/treebank/treebank_e.xml");

        //Another XML file in URL
        //parser.parseAsTree("https://nspublicforsharing.s3.amazonaws.com/standard.xml");
        //parser.parse("https://nspublicforsharing.s3.amazonaws.com/standard.xml");

        //XML file saved locally
        //parser.parseAsTree("C:\\Users\\User\\IdeaProjects\\nSoftwareTask2\\test.xml");
        //parser.parse("C:\\Users\\User\\IdeaProjects\\nSoftwareTask2\\test.xml");

        //XML file saved locally that contains the orders and their amounts
        //parser.parseAsTree("C:\\Users\\User\\IdeaProjects\\nSoftwareTask2\\orders.xml");
        parser.parse("C:\\Users\\User\\IdeaProjects\\nSoftwareTask2\\orders.xml");


        long endTime = System.currentTimeMillis();

        //Print the parsed tree
        parser.printTree();

        System.out.println("\nParsing the XML file took " + (endTime-startTime)/1000 + " seconds \n");

        printAmountOver100();
    }

    public static void printAmountOver100 (){
        System.out.println("\nOrder IDs with an amount greater than 100: ");

        for (Map.Entry<String, String> entry : orderID.entrySet()) {
            System.out.println("Order id=" + entry.getKey() + " at path: " + entry.getValue());
        }
    }
}