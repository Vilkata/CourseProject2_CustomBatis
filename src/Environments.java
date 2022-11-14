import java.util.HashMap;
import java.util.HashSet;

public class Environments {
    HashMap<String, Environment> environments = new HashMap<>();
    String defaults;

    Environments(String defaults) {
        this.defaults = defaults;
    }

    public void addEnvironment(Environment environment) {
        environments.put(environment.id, environment);
    }
}
