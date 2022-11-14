import java.util.HashMap;
public class Mapper {
    HashMap<String, ResultMap> mappers = new HashMap<>();
    HashMap<String, Query> queries;

    public void addResultMap(String id, ResultMap mapper) {
        mappers.put(id, mapper);
    }

    public void addQuery(String id, Query query) {
        queries.put(id, query);
    }
}

