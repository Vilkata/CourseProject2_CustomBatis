import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        String source = "C:\\Users\\Velin\\IdeaProjects\\CourseProject2_CustomBatis\\src\\myBatisConfig.xml";
        Configuration config = parseConfiguration(source);
    }

    public static Configuration parseConfiguration(String source) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File f = new File(source);
        Document doc = builder.parse(f);
        Element root = doc.getDocumentElement();
        if (!root.getNodeName().equals("configuration")) {
            throw new ParserConfigurationException("Missing configuration element");
        }
        Configuration config = createConfig(root);
        return config;
    }

    private static Configuration createConfig(Element root) throws Exception {
        Configuration config = new Configuration();
        NodeList children = root.getChildNodes();

        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            String name = child.getNodeName();
            switch (name) {
                case "environments":
                    config.environments = createEnvironments(child);
                    break;
                case "mappers":
                    config.mappers = createMappers(child);
                    break;
                case "properties":
                    String propSource = getAttributes("properties", child);
                    config.properties = createProperties(propSource);
                    break;
                default:
                    String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                    throw new ParserConfigurationException(message);
            }
        }
        return config;
    }

    public static Properties createProperties(String source) throws IOException {
        Reader r = new FileReader(source);
        Properties props = new Properties();
        props.load(r);
        return props;
    }

    public static Environment createEnvironment(Node root) throws ParserConfigurationException {
        NodeList children = root.getChildNodes();

        if (children.getLength() == 0) {
            throw new IllegalStateException("Could not find environments");
        }

        DataSource dataSource = null;
        TransactionManager transactionManager = null;
        String id = getAttributes("id", root);

        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            String name = child.getNodeName();
            switch (name) {
                case "transactionManager":
                    NamedNodeMap attributes = child.getAttributes();
                    Node type = attributes.item(0);
                    transactionManager = new TransactionManager(type.getNodeValue());
                    break;
                case "dataSource":
                    dataSource = createDataSource(child);
                    break;
                default:
                    String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                    throw new IllegalArgumentException(message);
            }
        }
        return new Environment(id, transactionManager, dataSource);
    }

    public static Environments createEnvironments(Node root) throws ParserConfigurationException {
        NodeList children = root.getChildNodes();

        if (children.getLength() == 0) {
            return null;
        }

        String defaults = getAttributes("environments", root);
        Environments environments = new Environments(defaults);

        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            String name = child.getNodeName();
            if ("environment".equals(name)) {
                Environment environment = createEnvironment(child);
                environments.addEnvironment(environment);
            } else {
                String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                throw new IllegalArgumentException(message);
            }
        }

        return environments;
    }

    public static ArrayList<Mapper> createMappers(Node root) throws Exception {
        NodeList children = root.getChildNodes();

        if (children.getLength() == 0) {
            throw new ParserConfigurationException("Didn't find any elements");
        }

        ArrayList<Mapper> mappers = new ArrayList<>();

        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            String name = child.getNodeName();
            if ("mapper".equals(name)) {
                Node typeSource = child.getAttributes().item(0);
                String url = switch (typeSource.getNodeName()) {
                    case "url" -> typeSource.getNodeValue();
                    case "resource" -> {
                        Path dir = Path.of(System.getProperty("java.class.path"));
                        Path relative = Path.of(typeSource.getNodeValue());
                        yield dir.resolve(relative).toString();
                    }
                    case "class" -> "";
                    default -> {
                        String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                        throw new IllegalArgumentException(message);
                    }
                };

//                Mapper mapper = new Mapper(url);
//                mappers.addMapper(mapper);
                Mapper mapper = buildMapper(url);
                mappers.add(mapper);
            } else {
                String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                throw new IllegalArgumentException(message);
            }
        }

        return mappers;
    }

    public static Mapper buildMapper(String url) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File f = new File(url);
        Document doc = builder.parse(f);
        Node root = doc.getDocumentElement();

        if (!root.getNodeName().equals("mapper")) {
            String message = String.format("Found illegal element %s should be mapper", root.getNodeName());
            throw new ParserConfigurationException(message);
        }

        Mapper mapper = new Mapper();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            switch (child.getNodeName()) {
                case "resultMap":
                    addResultMap(child, mapper);
                    break;
                case "select", "delete", "update", "insert":
                    addQuery(child, mapper);
                    break;
                default:
                    String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                    throw new ParserConfigurationException(message);
            }
        }

        return mapper;
    }

    private static void addResultMap(Node root, Mapper mapper) throws ParserConfigurationException {
        String id = getAttributes("id", root);
        ResultMap resultMap = createResultMap(root);
        mapper.addResultMap(id, resultMap);
    }

    private static ResultMap createResultMap(Node root) throws ParserConfigurationException {
        ResultMap resultMap = new ResultMap();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            switch (root.getNodeName()) {
                case "id", "result":
                    String column = getAttributes("column", root);
                    String property = getAttributes("property", root);
                    resultMap.addMapperProps(property, column);
                    break;
                default:
                    String message = String.format("Found illegal element %s in root %s", child.getNodeName(), root.getNodeName());
                    throw new ParserConfigurationException(message);
            }
        }

        return resultMap;
    }

    private static void addQuery(Node root, Mapper mapper) throws ClassNotFoundException, ParserConfigurationException {
        String id = getAttributes("id", root);
        Query query = createQuery(root, root.getNodeName());
        mapper.addQuery(id, query);
    }

    private static Query createQuery(Node root, String queryString) throws ClassNotFoundException, ParserConfigurationException {
        Query.POSSIBLE_QUERIES query = Query.POSSIBLE_QUERIES.valueOf(queryString.toUpperCase());

        String resultType = "Integer";
        if (query.equals(Query.POSSIBLE_QUERIES.SELECT)) {
            resultType = getAttributes("resultType", root);
        }

        Class<?> clazz = Class.forName(resultType);
        String sql = root.getTextContent();
        if (!sql.equals("")) {
            String message = String.format("Text of %s can't be empty", root.getNodeName());
            throw new ParserConfigurationException(message);
        }

        return new Query(query, sql, clazz);
    }

    public static String getAttributes(String expected, Node root) throws ParserConfigurationException {
        Node attribute = root.getAttributes().item(0);
        if (!attribute.getNodeName().equals(expected)) {
            String message = String.format("Found illegal attribute %s in root %s", attribute.getNodeName(), root.getNodeName());
            throw new ParserConfigurationException(message);
        }

        if (attribute.getNodeValue().equals("")) {
            String message = String.format("Attribute %s can't be empty in root %s", attribute.getNodeName(), root.getNodeName());
            throw new ParserConfigurationException(message);
        }


        return attribute.getNodeValue();
    }

    public static DataSource createDataSource(Node root) {
        //create properDataSource
        return new DataSource();
    }
}