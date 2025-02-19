package com.lielamar.auth.shared.storage;

import com.lielamar.auth.shared.handlers.ConfigHandler;
import com.lielamar.auth.shared.storage.json.JSONStorage;
import com.lielamar.auth.shared.storage.mongodb.MongoDBStorage;
import com.lielamar.auth.shared.storage.sql.SQLStorage;
import org.bukkit.Bukkit;

import java.util.UUID;

public abstract class StorageHandler {

    public static boolean isLoaded;

    /**
     * Sets the Key of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to set the Key of
     * @param secretKey Key to set
     * @return The set Key
     */
    public abstract String setKey(UUID uuid, String secretKey);

    /**
     * Returns the Key of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to get the Key of
     * @return Key of the player
     */
    public abstract String getKey(UUID uuid);

    /**
     * Checks whether or not a player who's UUID is uuid has a Key
     *
     * @param uuid UUID of the player to check the Key of
     * @return Whether or not the player has a Key
     */
    public abstract boolean hasKey(UUID uuid);

    /**
     * Removes the Key of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to remove the Key of
     */
    public abstract void removeKey(UUID uuid);

    /**
     * Sets the Last IP of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to set the IP of
     * @param lastIP IP to set
     * @return The set IP
     */
    public abstract String setIP(UUID uuid, String lastIP);

    /**
     * Returns the IP of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to get the IP of
     * @return Last IP of the player
     */
    public abstract String getIP(UUID uuid);

    /**
     * Checks whether or not a player who's UUID is uuid has a Last IP
     *
     * @param uuid UUID of the player to check the IP of
     * @return Whether or not the player has a Last IP
     */
    public abstract boolean hasIP(UUID uuid);

    /**
     * Sets the Enable Date of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to set the Enable Date of
     * @param enableDate Enable Date to set
     * @return The set Enable Date
     */
    public abstract long setEnableDate(UUID uuid, long enableDate);

    /**
     * Returns the Enable Date of the player who's UUID is uuid
     *
     * @param uuid UUID of the player to get the Enable Date of
     * @return Enable Date of the player
     */
    public abstract long getEnableDate(UUID uuid);

    /**
     * Checks whether or not a player who's UUID is uuid has an Enable Date
     *
     * @param uuid UUID of the player to check the Enable Date of
     * @return Whether or not the player has an Enable Date
     */
    public abstract boolean hasEnableDate(UUID uuid);

    /**
     * Unloads everything related to the storage type
     */
    public abstract void unload();
    
    /**
     * Check if external storage was loaded
     */
    public abstract boolean isLoaded();

    /**
     * Sets up the Storage connection of the database
     *
     * @param configHandler Config the get the necessary data from
     * @param absolutePath
     * @return Created Storage Handler
     */
    public static StorageHandler loadStorageHandler(ConfigHandler configHandler, String absolutePath) {
        try {
            isLoaded = true;
            switch (configHandler.getStorageMethod()) {
                case MYSQL:
                    return new SQLStorage("com.mysql.cj.jdbc.MysqlDataSource",
                            configHandler.getHost(), configHandler.getDatabase(), configHandler.getUsername(), configHandler.getPassword(), configHandler.getPort(),
                            configHandler.getTablePrefix(), configHandler.getMaximumPoolSize(), configHandler.getMinimumIdle(), configHandler.getMaximumLifetime(), configHandler.getKeepAliveTime(), configHandler.getConnectionTimeout());

                case H2:
                    return new SQLStorage("org.h2.jdbcx.JdbcDataSource",
                            configHandler.getHost(), configHandler.getDatabase(), configHandler.getUsername(), configHandler.getPassword(), configHandler.getPort(),
                            configHandler.getTablePrefix(), configHandler.getMaximumPoolSize(), configHandler.getMinimumIdle(), configHandler.getMaximumLifetime(), configHandler.getKeepAliveTime(), configHandler.getConnectionTimeout());

                case MARIADB:
                    return new SQLStorage("org.mariadb.jdbc.MariaDbDataSource",
                            configHandler.getHost(), configHandler.getDatabase(), configHandler.getUsername(), configHandler.getPassword(), configHandler.getPort(),
                            configHandler.getTablePrefix(), configHandler.getMaximumPoolSize(), configHandler.getMinimumIdle(), configHandler.getMaximumLifetime(), configHandler.getKeepAliveTime(), configHandler.getConnectionTimeout());

                case POSTGRESQL:
                    return new SQLStorage("org.postgresql.ds.PGSimpleDataSource",
                            configHandler.getHost(), configHandler.getDatabase(), configHandler.getUsername(), configHandler.getPassword(), configHandler.getPort(),
                            configHandler.getTablePrefix(), configHandler.getMaximumPoolSize(), configHandler.getMinimumIdle(), configHandler.getMaximumLifetime(), configHandler.getKeepAliveTime(), configHandler.getConnectionTimeout());

                case MONGODB:
                    return new MongoDBStorage(configHandler.getHost(), configHandler.getDatabase(), configHandler.getUsername(), configHandler.getPassword(), configHandler.getPort(),
                            configHandler.getCollectionPrefix(), configHandler.getMongodbURI());

                default: // JSON
                    return new JSONStorage(absolutePath);
            }
        } catch (Exception exception) {
            isLoaded = false;
            exception.printStackTrace();
            Bukkit.getServer().getLogger().severe("Couldn't load the Database you specified for the above reason. Defaulting to JSON!");
            return new JSONStorage(absolutePath);
        }
    }
}
