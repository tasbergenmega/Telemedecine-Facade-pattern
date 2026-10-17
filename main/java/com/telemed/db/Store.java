package com.telemed.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Store implements AutoCloseable {

    private final Connection conn;

    public Store(String dbFilePath) {
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            this.conn.setAutoCommit(true);
            migrate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open store: " + dbFilePath, e);
        }
    }

    public Connection connection() {
        return conn;
    }

    private void migrate() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id           TEXT PRIMARY KEY,
                    doctor_id    TEXT NOT NULL,
                    patient_id   TEXT NOT NULL,
                    slot_iso     TEXT NOT NULL,
                    region       TEXT NOT NULL,
                    status       TEXT NOT NULL,
                    created_at   TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS session_notes (
                    id             TEXT PRIMARY KEY,
                    appointment_id TEXT NOT NULL,
                    content        TEXT,
                    status         TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS video_sessions (
                    id             TEXT PRIMARY KEY,
                    appointment_id TEXT NOT NULL,
                    status         TEXT NOT NULL,
                    started_at     TEXT,
                    ended_at       TEXT
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS prescriptions (
                    id             TEXT PRIMARY KEY,
                    session_id     TEXT NOT NULL,
                    drug_code      TEXT NOT NULL,
                    dosage_mg      INTEGER NOT NULL,
                    status         TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS billing_records (
                    id             TEXT PRIMARY KEY,
                    appointment_id TEXT NOT NULL,
                    amount_cents   INTEGER NOT NULL,
                    currency       TEXT NOT NULL,
                    status         TEXT NOT NULL
                )
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS chain_state (
                    chain_id         TEXT PRIMARY KEY,
                    status           TEXT NOT NULL,
                    appointment_id   TEXT,
                    session_id       TEXT,
                    prescription_id  TEXT,
                    billing_id       TEXT,
                    last_error       TEXT,
                    updated_at       TEXT NOT NULL
                )
            """);
        }
    }

    public int execute(String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("SQL error: " + sql, e);
        }
    }

    public List<Object[]> query(String sql, Object... params) {
        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 0; i < cols; i++) row[i] = rs.getObject(i + 1);
                    out.add(row);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SQL error: " + sql, e);
        }
        return out;
    }

    public Object[] queryOne(String sql, Object... params) {
        List<Object[]> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if (p == null) ps.setNull(i + 1, Types.NULL);
            else if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
            else if (p instanceof Long) ps.setLong(i + 1, (Long) p);
            else ps.setString(i + 1, p.toString());
        }
    }

    @Override
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException ignored) { }
    }
}