package at.lorenz.booster.transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class Database {
    Connection connection;

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize() {
        this.connection = this.getSQLConnection();

        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM `booster` LIMIT 1;");
            ResultSet rs = ps.executeQuery();
            this.close(ps, rs);
        } catch (SQLException e) {
            System.err.println("Es konnte keine Verbindung zur SQLite Datenbank hergestellt werden.");
            e.printStackTrace();
        }

    }

    public Map<Integer, Integer> getBooster(String UUID) {
        Map<Integer, Integer> booster = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<Integer, Integer> var8;
        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM `booster` WHERE `uuid`=?;");
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return booster;
            }

            booster.put(0, rs.getInt("fly"));
            booster.put(1, rs.getInt("mining"));
            booster.put(2, rs.getInt("xp"));
            booster.put(3, rs.getInt("speed"));
            booster.put(4, rs.getInt("vision"));
            var8 = booster;
        } catch (SQLException e) {
            e.printStackTrace();
            return booster;
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return var8;
    }

    public Integer getBooster(String UUID, String type) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM `booster` WHERE `uuid`=?;");
            ps.setString(1, UUID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return 0;
    }

    public void setupBooster(String UUID) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("INSERT OR IGNORE INTO `booster` (uuid) VALUES (?);");
            ps.setString(1, UUID);
            ps.executeUpdate();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public void addBooster(String UUID, String type, Integer multiplier) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.getSQLConnection();
            ps = conn.prepareStatement("UPDATE `booster` SET `" + type + "`= `" + type + "` + ? WHERE `uuid`=?;");
            ps.setInt(1, multiplier);
            ps.setString(2, UUID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }

            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
