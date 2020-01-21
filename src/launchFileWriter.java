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
        printTbInfo();
        writeRViz();
        System.err.println("Launch files written successfully");
    }

    private static void printTbInfo() {
        System.out.println(usr.tbAmt);
        for (int i = 0; i < usr.tbAmt; i++) {
            System.out.println(usr.pos[i][0] + " " + usr.pos[i][1] + " " + usr.pos[i][2]);
        }
        System.out.println(usr.occupancyGridDirectory);
    }

    private static void writeRViz() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(CURR_DIR + "/templates/multi_nav_template.rviz"), "UTF-8"));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(CURR_DIR + "/rviz/multi_nav_tb.rviz"), "UTF-8"));

            String[] lines = new String[999];
            String line = null;
            int indexOfRobotModels = 0;// block is 16 lines long
            int indexOfTF = 0;
            int indexOfScanTopic = 0;
            int indexOfImageTopic = 0;
            int indexOfMapTopic = 0;
            int indexOfPathTopic = 0;
            int indexOfGlobalMapTopic = 0;
            int indexOfLocalMapTopic = 0;
            int indexOfAmclTopic = 0;
            int indexOfLocalCostmapTopic = 0;
            int indexOfPoseTopic = 0;
            int indexOfMassivePathTopic = 0;
            int indexOfEndOfMassivePathTopic = 0;
            int indexOfNavGoalTools = 0;
            int indexOfEndOfTools = 0;
            int indexOfEOF = 0;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                lines[index] = line;
                if (line.indexOf("rviz/RobotModel") != -1)
                    indexOfRobotModels = index - 1;
                if (line.indexOf("TF_PLACEHOLDER") != -1)
                    indexOfTF = index + 1;
                if (line.indexOf("SCAN_TOPIC_PLACEHOLDER") != -1)
                    indexOfScanTopic = index + 1;
                if (line.indexOf("IMAGE_TOPIC_PLACEHOLDER") != -1)
                    indexOfImageTopic = index + 1;
                if (line.indexOf("MAP_TOPIC_PLACEHOLDER") != -1)
                    indexOfMapTopic = index + 1;
                if (line.indexOf("PATH_TOPIC_PLACEHOLDER") != -1)
                    indexOfPathTopic = index + 1;
                if (line.indexOf("GLOBAL_MAP_TOPIC") != -1)
                    indexOfGlobalMapTopic = index + 1;
                if (line.indexOf("LOCAL_MAP_TOPIC") != -1)
                    indexOfLocalMapTopic = index + 1;
                if (line.indexOf("AMCL_TOPIC") != -1)
                    indexOfAmclTopic = index + 1;
                if (line.indexOf("LOCAL_COSTMAP_TOPIC") != -1)
                    indexOfLocalCostmapTopic = index + 1;
                if (line.indexOf("POSE_TOPIC") != -1)
                    indexOfPoseTopic = index + 1;
                if (line.indexOf("START_OF_MASSIVE_PATH_TOPIC") != -1)
                    indexOfMassivePathTopic = index + 1;
                if (line.indexOf("END_OF_MASSIVE_PATH_TOPIC") != -1)
                    indexOfEndOfMassivePathTopic = index + 1;
                if (line.indexOf("NAV_GOAL_TOOLS") != -1)
                    indexOfNavGoalTools = index + 1;
                if (line.indexOf("END_OF_TOOLS") != -1)
                    indexOfEndOfTools = index + 1;
                if (line.indexOf("EOF") != -1)
                    indexOfEOF = index + 1;
                index++;
            }
            // write everything up to first tb definition
            for (int i = 0; i < indexOfRobotModels; i++) {
                writer.write(lines[i]);
                writer.write("\n");
            }
            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = 0; j < 16; j++) {
                    String ln = lines[j + indexOfRobotModels];
                    String placeholderTBNAME = "INSERT_TB_NAME_HERE";
                    String placeholderRBDESC = "INSERT_TB_DESCRIPTION_HERE";
                    String placeholderTFPREFIX = "INSERT_TF_PREFIX_HERE";

                    int indexTBNAME = ln.indexOf(placeholderTBNAME);
                    int indexRBDESC = ln.indexOf(placeholderRBDESC);
                    int indexTFPREFIX = ln.indexOf(placeholderTFPREFIX);

                    if (indexTBNAME != -1)
                        ln = ln.substring(0, indexTBNAME) + "RobotModel_tb3_" + i
                                + ln.substring(indexTBNAME + placeholderTBNAME.length());
                    if (indexRBDESC != -1)
                        ln = ln.substring(0, indexRBDESC) + "tb3_" + i + "/robot_description"
                                + ln.substring(indexRBDESC + placeholderRBDESC.length());
                    if (indexTFPREFIX != -1)
                        ln = stringMaker(ln, "tb3_" + i, indexTFPREFIX, placeholderTFPREFIX);

                    writer.write(ln);
                    writer.write("\n");

                }
            }

            writeBetweenIndices(writer, lines, indexOfTF, indexOfScanTopic - 1);

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfScanTopic; j < indexOfImageTopic - 1; j++) {
                    String ln = lines[j];
                    String placeholderSCANNAME = "INSERT_LASERSCAN_NAME";
                    String placeholderSCANTOPIC = "INSERT_SCAN_TOPIC";

                    int indexSCANNAME = ln.indexOf(placeholderSCANNAME);
                    int indexSCANTOPIC = ln.indexOf(placeholderSCANTOPIC);

                    if (indexSCANNAME != -1)
                        ln = stringMaker(ln, "LaserScan_tb3_" + i, indexSCANNAME, placeholderSCANNAME);
                    if (indexSCANTOPIC != -1)
                        ln = stringMaker(ln, "/tb3_" + i + "/scan", indexSCANTOPIC, placeholderSCANTOPIC);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            writeBetweenIndices(writer, lines, indexOfMapTopic, indexOfPathTopic - 1);

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfPathTopic; j < indexOfGlobalMapTopic - 1; j++) {

                    String ln = lines[j];
                    String placeholderMOVEBASETOPIC = "INSERT_MOVE_BASE_TOPIC";

                    int indexMOVEBASETOPIC = ln.indexOf(placeholderMOVEBASETOPIC);

                    if (indexMOVEBASETOPIC != -1)
                        ln = stringMaker(ln, "/tb3_" + i + "/move_base/NavfnROS/plan", indexMOVEBASETOPIC,
                                placeholderMOVEBASETOPIC);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            writeBetweenIndices(writer, lines, indexOfGlobalMapTopic, indexOfAmclTopic - 1);

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfAmclTopic; j < indexOfLocalCostmapTopic - 1; j++) {
                    // tb3_0/particlecloud
                    String ln = lines[j];

                    String placeholderPARTICLETOPIC = "INSERT_PARTICLE_TOPIC_HERE";
                    String placeholderRVALUE = "INSERT_R_VALUE";

                    int r_val = (int) (Math.random() * 256);

                    int indexPARTICLETOPIC = ln.indexOf(placeholderPARTICLETOPIC);
                    int indexRVALUE = ln.indexOf(placeholderRVALUE);

                    if (indexPARTICLETOPIC != -1)
                        ln = stringMaker(ln, "/tb3_" + i + "/particlecloud", indexPARTICLETOPIC,
                                placeholderPARTICLETOPIC);
                    if (indexRVALUE != -1)
                        ln = stringMaker(ln, "" + r_val, indexRVALUE, placeholderRVALUE);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfLocalCostmapTopic; j < indexOfPoseTopic - 1; j++) {
                    // tb3_1/move_base/local_costmap/footprint
                    String ln = lines[j];

                    String placeholderMBLCF = "INSERT_LOCAL_COSTMAP_FOOTPRINT";

                    int indexMBLCF = ln.indexOf(placeholderMBLCF);

                    if (indexMBLCF != -1)
                        ln = stringMaker(ln, "tb3_" + i + "/move_base/local_costmap/footprint", indexMBLCF,
                                placeholderMBLCF);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfPoseTopic; j < indexOfMassivePathTopic - 1; j++) {
                    /// tb3_0/move_base_simple/goal
                    String ln = lines[j];

                    String placeholderMBNAME = "INSERT_NAME_OF_GOAL";
                    String placeholderMBSIMPLEGOALTOPIC = "INSERT_MOVE_BASE_SIMPLE_GOAL_TOPIC";

                    int indexMBNAME = ln.indexOf(placeholderMBNAME);
                    int indexMBSIMPLEGOALTOPIC = ln.indexOf(placeholderMBSIMPLEGOALTOPIC);

                    if (indexMBNAME != -1)
                        ln = stringMaker(ln, "Goal_" + "tb3_" + i, indexMBNAME, placeholderMBNAME);
                    if (indexMBSIMPLEGOALTOPIC != -1)
                        ln = stringMaker(ln, "tb3_" + i + "/move_base_simple/goal", indexMBSIMPLEGOALTOPIC,
                                placeholderMBSIMPLEGOALTOPIC);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            for (int i = 1; i < usr.tbAmt; i++) {
                for (int j = indexOfMassivePathTopic; j < indexOfEndOfMassivePathTopic - 1; j++) {
                    /// tb3_0/move_base_simple/goal
                    String ln = lines[j];

                    String placeholderPATHNAME = "INSERT_NAME_OF_PATH";
                    String placeholderMBPLAN = "INSERT_MOVE__BASE_PLAN__";
                    String placeholderMBPLANTOPIC = "INSERT_MOVE__BASE_PLAN_TOPIC";

                    int indexPATHNAME = ln.indexOf(placeholderPATHNAME);
                    int indexMBPLAN = ln.indexOf(placeholderMBPLAN);
                    int indexMBPLANTOPIC = ln.indexOf(placeholderMBPLANTOPIC);

                    if (indexPATHNAME != -1)
                        ln = stringMaker(ln, "Path_" + "tb3_" + i, indexPATHNAME, placeholderPATHNAME);
                    if (indexMBPLAN != -1)
                        ln = stringMaker(ln, "tb3_" + i + "/move_base/NavfnROS/plan", indexMBPLAN, placeholderMBPLAN);
                    if (indexMBPLANTOPIC != -1)
                        ln = stringMaker(ln, "tb3_" + i + "/move_base/DWAPlannerROS/local_plan", indexMBPLANTOPIC,
                                placeholderMBPLANTOPIC);

                    writer.write(ln);
                    writer.write("\n");
                }
            }

            writeBetweenIndices(writer, lines, indexOfEndOfMassivePathTopic, indexOfNavGoalTools - 1);

            for (int i = 0; i < usr.tbAmt; i++) {
                for (int j = indexOfNavGoalTools; j < indexOfEndOfTools - 1; j++) {
                    /// tb3_0/move_base_simple/goal
                    String ln = lines[j];

                    String placeholderINITPOSE = "INSERT_INIT_POSE__TOPIC";
                    String placeholderMBSIMPLEGOAL = "INSERT_SIMPLE__GOAL_TOPIC";

                    int indexINITPOSE = ln.indexOf(placeholderINITPOSE);
                    int indexMBSIMPLEGOAL = ln.indexOf(placeholderMBSIMPLEGOAL);

                    if (indexINITPOSE != -1)
                        ln = stringMaker(ln, "/tb3_" + i + "/initialpose", indexINITPOSE, placeholderINITPOSE);
                    if (indexMBSIMPLEGOAL != -1)
                        ln = stringMaker(ln, "/tb3_" + i + "/move_base_simple/goal", indexMBSIMPLEGOAL, placeholderMBSIMPLEGOAL);

                    writer.write(ln);
                    writer.write("\n");
                }
            }
            
            writeBetweenIndices(writer, lines, indexOfEndOfTools, indexOfEOF - 1);
            reader.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String stringMaker(String ln, String specific, int ind, String placeholder) {
        return ln.substring(0, ind) + specific + ln.substring(ind + placeholder.length());
    }

    private static void writeBetweenIndices(BufferedWriter writer, String[] lines, int start, int finish)
            throws IOException {
        while (start < finish) {
            writer.write(lines[start]);
            start++;
            writer.write("\n");
        }
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
