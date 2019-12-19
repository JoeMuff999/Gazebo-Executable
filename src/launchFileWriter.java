import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

public class launchFileWriter {
    static Document xmlDoc;
    static Element root;
    static final String CURR_DIR = System.getProperty("java.class.path") + "/..";
    static UserParameters usr;

    public static void main(String[] args) {
        readUserParameters();
        writeTurtlebotLaunch();
        writeMapLaunch();
        System.out.println("Launch files written successfully");
    }

    /*
     * method to read the user input from the Qt interface. creates a UserParameters
     * object to store all of the users inputs
     */
    private static void readUserParameters() {
        try {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(CURR_DIR + "/parameters.txt"), "UTF-8"));

            String worldDir, occGridDir, tbModel;
            int tbAmt;
            worldDir = reader.readLine();
            occGridDir = reader.readLine();
            tbModel = reader.readLine();
            tbAmt = Integer.parseInt(reader.readLine());

            double[][] pos = new double[tbAmt][3];
            double[][] attitude = new double[tbAmt][3];

            for (int i = 0; i < tbAmt; i++) {
                String line = reader.readLine();
                String[] vals = line.split("\\s+");
                for (int j = 0; j < 3; j++)
                    pos[i][j] = Double.parseDouble(vals[j]);

                // convert quaternion to euler
                double[] quaternions = new double[] { Double.parseDouble(vals[3]), Double.parseDouble(vals[4]),
                        Double.parseDouble(vals[5]), Double.parseDouble(vals[6]) };
                attitude[i] = convertQuaternionToEuler(quaternions);
            }

            // Close to unlock.
            reader.close();

            usr = new UserParameters(worldDir, occGridDir, tbModel, tbAmt, pos, attitude);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * manager method for writing the turtlebot launch files method defines the xml
     * file, calls the writing method (writeTurtlebots), and then writes the xml
     * file
     */
    private static void writeTurtlebotLaunch() {
        try {
            // create XML doc for turtlebot launch file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            xmlDoc = dBuilder.newDocument();

            root = xmlDoc.createElement("launch");
            xmlDoc.appendChild(root);

            writeTurtlebots(usr.tbAmt, usr.tbModel, usr.pos, usr.attitude);

            TransformerFactory tFF = TransformerFactory.newInstance();
            Transformer tF = tFF.newTransformer();
            tF.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(xmlDoc);
            StreamResult res = new StreamResult(new File(CURR_DIR + "/launch/turtlebot_setup.launch"));
            tF.transform(src, res);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* TODO AMCL parameters --> http://wiki.ros.org/amcl#Parameters */
    /*
     * method that defines the elements and attributes of the xml file for the
     * turtlebots writes the header, defines the tb pose arguments, and defines the
     * tb namespaces
     */
    private static void writeTurtlebots(int amt, String TURTLEBOT3_MODEL, double[][] initialPositions,
            double[][] initialOrientations) {

        // header info
        Element tbModel = xmlDoc.createElement("arg");
        Attr doc = xmlDoc.createAttribute("doc");
        doc.setValue("model type [burger, waffle, waffle_pi]");

        Attr nameAttribute = xmlDoc.createAttribute("name");
        Attr defaultAttribute = xmlDoc.createAttribute("default");

        nameAttribute.setValue("model");
        defaultAttribute.setValue(TURTLEBOT3_MODEL);

        tbModel.setAttributeNode(nameAttribute);
        tbModel.setAttributeNode(defaultAttribute);
        tbModel.setAttributeNode(doc);

        root.appendChild(tbModel);

        // for each turtlebot, create model arg , create position arg, and create
        // attitude arg
        for (int i = 0; i < amt; i++) {
            // model arg
            Attr name = createAttributeHelper("name", i + "_tb3");
            Attr def = createAttributeHelper("default", "tb3_" + i);
            Attr[] attrs = new Attr[] { name, def };
            Element argElem = createElementHelper("arg", attrs);
            root.appendChild(argElem);

            // position arg
            String[] posStr = new String[] { "x", "y", "z" };
            int index = 0;
            for (String s : posStr) {
                name = createAttributeHelper("name", i + "_tb3_" + s + "_pos");
                def = createAttributeHelper("default", "" + initialPositions[i][index]);
                argElem = createElementHelper("arg", new Attr[] { name, def });
                root.appendChild(argElem);
                index++;
            }
            // attitude arg
            String[] attStr = new String[] { "pitch", "yaw", "roll" };
            index = 0;
            for (String s : attStr) {
                name = createAttributeHelper("name", i + "_tb3_" + s);
                def = createAttributeHelper("default", "" + initialOrientations[i][index]);
                argElem = createElementHelper("arg", new Attr[] { name, def });
                root.appendChild(argElem);
                index++;
            }
        }

        // define namespaces for turtlebots
        for (int i = 0; i < amt; i++) {
            // "arg" string for each tb3 (ie: "$(arg 0_tb3)")
            String tbArg = "$(arg " + i + "_tb3)";

            // top level namespace element
            Attr ns = createAttributeHelper("ns", tbArg);
            Element nsElem = createElementHelper("group", new Attr[] { ns });
            root.appendChild(nsElem);

            // sub-element for robot urdf paramters
            Attr name = createAttributeHelper("name", "robot_description");
            Attr command = createAttributeHelper("command",
                    "$(find xacro)/xacro --inorder $(find turtlebot3_description)/urdf/turtlebot3_$(arg model).urdf.xacro");
            Element param = createElementHelper("param", new Attr[] { name, command });
            nsElem.appendChild(param);

            // sub-element for publisher info
            Attr pkg = createAttributeHelper("pkg", "robot_state_publisher");
            Attr type = createAttributeHelper("type", "robot_state_publisher");
            name = createAttributeHelper("name", "robot_state_publisher");
            Attr output = createAttributeHelper("output", "screen");
            Element node = createElementHelper("node", new Attr[] { pkg, type, name, output });
            nsElem.appendChild(node);

            // sub-sub-element for publisher characteristics
            name = createAttributeHelper("name", "publish_frequency");
            type = createAttributeHelper("type", "double");
            Attr value = createAttributeHelper("value", "50.0");
            param = createElementHelper("param", new Attr[] { name, type, value });
            node.appendChild(param);

            // sub-sub element for publisher tf definition
            name = createAttributeHelper("name", "tf_prefix");
            value = createAttributeHelper("value", tbArg);
            param = createElementHelper("param", new Attr[] { name, value });
            node.appendChild(param);

            // sub element for gazebo arguments
            name = createAttributeHelper("name", "spawn_urdf");
            pkg = createAttributeHelper("pkg", "gazebo_ros");
            type = createAttributeHelper("type", "spawn_model");
            String[] posStr = new String[] { "x", "y", "z" };
            String[] attStr = new String[] { "P", "Y", "R" };
            String[] attStrFull = new String[] { "pitch", "yaw", "roll" };
            String argString = "-urdf -model " + tbArg;
            for (int j = 0; j < 3; j++) {
                argString += " -" + posStr[j] + " $(arg " + i + "_tb3_" + posStr[j] + "_pos)";
            }
            for (int j = 0; j < 3; j++) {
                argString += " -" + attStr[j] + " $(arg " + i + "_tb3_" + attStrFull[j] + ")";
            }
            argString += " -param robot_description";
            Attr args = createAttributeHelper("args", argString);
            node = createElementHelper("node", new Attr[] { name, pkg, type, args });
            nsElem.appendChild(node);

        }
    }

    private static void writeMapLaunch() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(CURR_DIR + "/templates/map_template.launch"), "UTF-8"));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(CURR_DIR + "/launch/user_world.launch"), "UTF-8"));
            String line = null;
            String placeholder = "map_directory";
            while ((line = reader.readLine()) != null) {
                int index = line.indexOf(placeholder);
                if (index != -1) {
                    line = line.substring(0, index) + usr.worldDirectory + line.substring(index + placeholder.length());
                }
                writer.write(line);
            }

            // Close to unlock.
            reader.close();
            // Close to unlock and flush to disk.
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[] convertQuaternionToEuler(double[] quaternion) {
        double qx = quaternion[0];
        double qy = quaternion[1];
        double qz = quaternion[2];
        double qw = quaternion[3];

        double sqx = qx * qx;
        double sqy = qy * qy;
        double sqz = qz * qz;
        double sqw = qw * qw;

        double[] eulerAngle = new double[3];
        eulerAngle[0] = Math.asin(-2.0 * (qx * qz - qy * qw) / (sqx + sqy + sqz + sqw));// pitch / attitude
        eulerAngle[1] = Math.atan2(2.0 * (qx * qy + qz * qw), (sqx - sqy - sqz + sqw));// yaw / heading
        eulerAngle[2] = Math.atan2(2.0 * (qy * qz + qx * qw), (-sqx - sqy + sqz + sqw));// roll / bank

        return eulerAngle;
    }

    private static Attr createAttributeHelper(String name, String value) {
        Attr attr = xmlDoc.createAttribute(name);
        attr.setValue(value);
        return attr;
    }

    private static Element createElementHelper(String name, Attr[] attributes) {
        Element elem = xmlDoc.createElement(name);
        for (Attr attr : attributes) {
            elem.setAttributeNode(attr);
        }
        return elem;
    }

    private static class UserParameters {
        String worldDirectory;
        String occupancyGridDirectory;
        String tbModel;
        int tbAmt;
        double[][] pos = new double[tbAmt][3];
        double[][] attitude = new double[tbAmt][3];

        private UserParameters(String worldDirectory, String occupancyGridDirectory, String tbModel, int tbAmt,
                double[][] pos, double[][] attitude) {
            this.worldDirectory = worldDirectory;
            this.occupancyGridDirectory = occupancyGridDirectory;
            this.tbModel = tbModel;
            this.tbAmt = tbAmt;
            this.pos = pos;
            this.attitude = attitude;
        }
    }
}
