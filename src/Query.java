import java.util.ArrayList;

public class Query {
    public enum POSSIBLE_QUERIES {SELECT,DELETE, INSERT, UPDATE};

    POSSIBLE_QUERIES query;
    ArrayList<String> names = new ArrayList<>();
    String sql;
    Class<?> returnType;


    public Query(POSSIBLE_QUERIES query, String sql, Class<?> returnType) {
        this.query = query;
        this.sql = sql;

        this.returnType = returnType;
    }
}
