package at.lorenz.booster;

import at.lorenz.api.sql.ConnectionHolder;

import java.sql.*;
import java.util.Map;

public class SqlManager {


    private final ConnectionHolder connectionHolder;

    public SqlManager() throws SQLException {
        connectionHolder = new ConnectionHolder();
    }

    public void load(User user) throws SQLException {
        PreparedStatement statement = connectionHolder.getConnection().prepareStatement("SELECT * FROM player_booster WHERE uuid LIKE ?;");
        statement.setString(1, user.getUuid().toString());

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int boosterId = resultSet.getInt("booster_id");
            int amount = resultSet.getInt("amount");
            Booster booster = Booster.values()[boosterId];
            user.getMap().put(booster, amount);
        }

        statement = connectionHolder.getConnection().prepareStatement("SELECT * FROM player_booster_ignored WHERE uuid LIKE ?;");
        statement.setString(1, user.getUuid().toString());

        resultSet = statement.executeQuery();
        while (resultSet.next()) {
            int boosterId = resultSet.getInt("booster_id");
            Booster booster = Booster.values()[boosterId];
            user.getIgnoredBoosters().add(booster);
        }
    }

    public void update(User user) throws SQLException {
        PreparedStatement statement = connectionHolder.getConnection().prepareStatement("DELETE FROM player_booster WHERE uuid LIKE ?;");
        statement.setString(1, user.getUuid().toString());
        statement.executeUpdate();
        statement = connectionHolder.getConnection().prepareStatement("DELETE FROM player_booster_ignored WHERE uuid LIKE ?;");
        statement.setString(1, user.getUuid().toString());
        statement.executeUpdate();

        for (Map.Entry<Booster, Integer> entry : user.getMap().entrySet()) {
            Booster booster = entry.getKey();
            int amount = entry.getValue();
            if (amount > 0) {
                statement = connectionHolder.getConnection().prepareStatement("INSERT INTO player_booster(uuid, booster_id, amount) VALUES (?, ?, ?);");
                statement.setString(1, user.getUuid().toString());
                statement.setInt(2, booster.ordinal());
                statement.setInt(3, amount);
                statement.executeUpdate();
            }
        }

        for (Booster booster : user.getIgnoredBoosters()) {
            statement = connectionHolder.getConnection().prepareStatement("INSERT INTO player_booster_ignored(uuid, booster_id) VALUES (?, ?);");
            statement.setString(1, user.getUuid().toString());
            statement.setInt(2, booster.ordinal());
            statement.executeUpdate();
        }


    }

    public void disable() throws SQLException {
        connectionHolder.disconnect();
    }

    public void clearAll() throws SQLException {
        PreparedStatement statement = connectionHolder.getConnection().prepareStatement("DELETE FROM player_booster;");
        statement.executeUpdate();
    }
}
