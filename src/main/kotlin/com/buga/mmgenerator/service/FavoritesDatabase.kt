package com.buga.mmgenerator.service

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object FavoritesDatabase {

    private var customDbUrl: String? = null

    val defaultDbFile: File by lazy {
        val userHome = System.getProperty("user.home", ".")
        val dir = File(userHome, ".mm-generator")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        File(dir, "favorites.db")
    }

    val dbUrl: String
        get() = customDbUrl ?: "jdbc:sqlite:${defaultDbFile.absolutePath}"

    /**
     * For testing purposes: allows configuring an in-memory or temp database URL.
     */
    fun setCustomUrl(url: String?) {
        customDbUrl = url
        initDatabase()
    }

    fun getConnection(): Connection {
        return DriverManager.getConnection(dbUrl)
    }

    fun initDatabase() {
        try {
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS favorites (
                            filename TEXT PRIMARY KEY,
                            title TEXT,
                            is_favorite INTEGER NOT NULL DEFAULT 1,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        );
                        """.trimIndent()
                    )
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getFavoriteFilenames(): Set<String> {
        val favorites = mutableSetOf<String>()
        try {
            getConnection().use { conn ->
                conn.prepareStatement("SELECT filename FROM favorites WHERE is_favorite = 1").use { stmt ->
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            favorites.add(rs.getString("filename"))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return favorites
    }

    fun setFavorite(filename: String, isFavorite: Boolean, title: String = "") {
        try {
            getConnection().use { conn ->
                if (isFavorite) {
                    conn.prepareStatement(
                        """
                        INSERT OR REPLACE INTO favorites (filename, title, is_favorite)
                        VALUES (?, ?, 1);
                        """.trimIndent()
                    ).use { stmt ->
                        stmt.setString(1, filename)
                        stmt.setString(2, title)
                        stmt.executeUpdate()
                    }
                } else {
                    conn.prepareStatement("DELETE FROM favorites WHERE filename = ?").use { stmt ->
                        stmt.setString(1, filename)
                        stmt.executeUpdate()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun isFavorite(filename: String): Boolean {
        try {
            getConnection().use { conn ->
                conn.prepareStatement("SELECT is_favorite FROM favorites WHERE filename = ? AND is_favorite = 1").use { stmt ->
                    stmt.setString(1, filename)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            return true
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return false
    }

    fun clearAllFavorites() {
        try {
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate("DELETE FROM favorites")
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}
