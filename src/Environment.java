public class Environment {
    String id;
    TransactionManager transactionManager;
    DataSource dataSource;

    Environment(String id, TransactionManager transactionManager, DataSource dataSource) {
        this.id = id;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }
}
