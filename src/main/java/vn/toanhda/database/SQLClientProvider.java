package vn.toanhda.database;

import java.sql.Connection;

public interface SQLClientProvider {
  Connection getConnection();
}
