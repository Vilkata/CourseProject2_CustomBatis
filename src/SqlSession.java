import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlSession implements Closeable {
    Connection connection;

    SqlSession(Connection connection) {
        this.connection = connection;
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new MyBatisExeption();
        }
    }
}
