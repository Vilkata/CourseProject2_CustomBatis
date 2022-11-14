import java.io.Reader;

public class SqlSessionFactoryBuilder {
    public SqlSessionFactory build(Reader r) {
        return this.build(r, null);
    }

    public SqlSessionFactory build(Reader r, Properties properties) {
        Configuration config;
        try {
            ConfigBuilder configBuilder = new ConfigBuilder(r, properties);
            config = configBuilder.parse();
        }

        return new SqlSessionFactory(config);
    }
}
