

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;

public class test
{
    static Document xmlDoc;
    static Element root;
    public static void main (String [] args)
    {
        writeTurtlebotLaunch(Integer.parseInt(args[0]));
        writeMapLaunch();
    }

    public static void writeMapLaunch()
    {

    }

    public static void writeTurtlebotLaunch(int tBAmt)
    {
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.newDocument();

            root = xmlDoc.createElement("launch");
            xmlDoc.appendChild(root);

            double[][] pos = new double[tBAmt][3];
            double[][] attitude = new double[tBAmt][3];
            String turtlebotModel = "$(env TURTLEBOT3_MODEL)"; //until I can get the input file
            writeTurtlebots(tBAmt, turtlebotModel, pos, attitude);
            
            //writeTurtlebotNamespaces(tBAmt);


            TransformerFactory tFF = TransformerFactory.newInstance();
            Transformer tF = tFF.newTransformer();
            tF.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(xmlDoc);
            String currPath = System.getProperty("user.dir");
            StreamResult res = new StreamResult(new File(currPath + "/test.xml" ));
            tF.transform(src, res);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
    /* TODO AMCL parameters --> http://wiki.ros.org/amcl#Parameters */
    public static void writeTurtlebots(int amt, String TURTLEBOT3_MODEL, double[][] initialPositions, double[][] initialOrientations)
    {
        //standard xml format for tbModel
        //<arg name="model" default="$(env TURTLEBOT3_MODEL)" doc="model type [burger, waffle, waffle_pi]"/>

        Element tBModel = xmlDoc.createElement("arg");
        Attr doc = xmlDoc.createAttribute("doc");
        doc.setValue("model type [burger, waffle, waffle_pi]");

        Attr nameAttribute = xmlDoc.createAttribute("name");
        Attr defaultAttribute = xmlDoc.createAttribute("default");

        nameAttribute.setValue("model");
        defaultAttribute.setValue(TURTLEBOT3_MODEL);

        tBModel.setAttributeNode(nameAttribute);
        tBModel.setAttributeNode(defaultAttribute);
        tBModel.setAttributeNode(doc);


        root.appendChild(tBModel);

        //for each turtlebot, create model arg ,  create position arg, and create attitude arg
        for(int i = 0; i < amt; i++)
        {
            //model arg
            Attr name = createAttributeHelper("name", i+1 + "_tb3");
            Attr def = createAttributeHelper("default", "tb3_" + i);
            Attr[] attrs = new Attr[]{name,def};
            Element argElem = createElementHelper("arg",attrs);
            root.appendChild(argElem);

            //position arg
            String[] posStr = new String[]{"x","y","z"};
            int index = 0;
            for(String s : posStr)
            {
              name = createAttributeHelper("name", i+1 + "_tb3_" + s + "_pos");
              def = createAttributeHelper("default", "" + initialPositions[i][index]);
              argElem = createElementHelper("arg", new Attr[]{name,def});
              root.appendChild(argElem);
              index++;
            }

            String[] attStr = new String[]{"pitch","yaw","roll"};
            index = 0;
            for(String s : attStr)
            {
              name = createAttributeHelper("name", i+1 + "_tb3_" + s);
              def = createAttributeHelper("default", "" + initialOrientations[i][index]);
              argElem = createElementHelper("arg", new Attr[]{name,def});
              root.appendChild(argElem);
              index++;
            }
        }
    }

    public static Attr createAttributeHelper(String name, String value)
    {
      Attr attr = xmlDoc.createAttribute(name);
      attr.setValue(value);
      return attr;
    }

    public static Element createElementHelper(String name, Attr[] attributes)
    {
        Element elem = xmlDoc.createElement(name);
        for(Attr attr : attributes)
        {
            elem.setAttributeNode(attr);
        }
        return elem;
    }
}
