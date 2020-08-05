package at.lorenz.booster.transfer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database {
    String dbname = "booster";
    public String SQLiteCreateBoosterTable = "CREATE TABLE IF NOT EXISTS booster (`uuid` varchar(36) NOT NULL PRIMARY KEY,`fly` INTEGER NOT NULL DEFAULT 1,`mining` INTEGER NOT NULL DEFAULT 1,`xp` INTEGER NOT NULL DEFAULT 1,`speed` INTEGER NOT NULL DEFAULT 0,`vision` INTEGER NOT NULL DEFAULT 0);";

    public Connection getSQLConnection() {
        File dataFolder = new File("plugins/ProjektBooster", this.dbname + ".db");
        if (!dataFolder.exists()) {
            try {
                dataFolder.createNewFile();
            } catch (IOException var5) {
                System.err.println("File write error: " + this.dbname + ".db");
            }
        }

        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return this.connection;
        } catch (SQLException var3) {
            System.err.println("SQLite exception on initialize");
        } catch (ClassNotFoundException var4) {
            System.err.println("You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }

        return null;
    }

    public void load() {
        this.connection = this.getSQLConnection();

        try {
            Statement s = this.connection.createStatement();
            s.executeUpdate(this.SQLiteCreateBoosterTable);
            s.close();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

        this.initialize();
    }
}
