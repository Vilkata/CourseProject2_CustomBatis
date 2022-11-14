
import java.sql.Connection;
import java.sql.SQLException;

public class SqlSessionFactory {
    DataBaseConnectionPool pool;
    Configuration config;

    SqlSessionFactory(Configuration config) {
        this.pool = DataBaseConnectionPool.getConnectionPool();
        this.config = config;
    }

    private SqlSession createSession() {
        Connection connection = pool.getConnection();
        return new SqlSession(connection);
    }

    public SqlSession openSession() {
        try {
            return createSession();
        } catch (SQLException e) {
            throw  new MyBatisExeption();
        }
    }

}
