import java.util.HashMap;

public class ResultMap {
    HashMap<String, String> mapperProps = new HashMap<>();

    public void addMapperProps(String prop, String col) {
        mapperProps.put(prop, col);
    }
}
