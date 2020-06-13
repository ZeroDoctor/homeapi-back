package com.zerulus.homeapipostgres.dao;

import com.zerulus.homeapipostgres.model.FileFolder;
import com.zerulus.homeapipostgres.util.UpdateFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository("postgres")
public class FolderDataAccessService implements FolderDao {

    private final JdbcTemplate jdbcTemplate;
    private Connection conn;

    @Autowired
    public FolderDataAccessService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        try {
            this.conn = DriverManager.getConnection("jdbc:postgresql://192.168.99.100:5432/homedb", "postgres", "Gift9327");
        } catch (SQLException throwables) {
            System.out.println("ERROR: failed to create connection to server");
            throwables.printStackTrace();
        }
    }

    public PreparedStatement createStatement(List<Object> list, String sql) {
        PreparedStatement statement = null;
        try{
            statement = conn.prepareStatement(sql);

            for (int i = 1; i <= list.size(); i++) {
                if(list.get(i - 1) instanceof String) {
                    statement.setString(i, (String) list.get(i - 1));
                }
                else if(list.get(i - 1) instanceof Long) {
                    statement.setLong(i, (Long) list.get(i - 1));
                }
                else if(list.get(i - 1) instanceof Short) {
                    statement.setShort(i, (Short) list.get(i - 1));
                }
                else if(list.get(i - 1) instanceof Timestamp) {
                    statement.setTimestamp(i, (Timestamp) list.get(i - 1));
                }
            }

            return statement;
        }catch (Exception e) {
            System.out.println("ERROR: failed to prepare statement " + list);
            e.printStackTrace();
        }

        return null;
    }

    private List<FileFolder> extractResult(ResultSet resultSet) throws SQLException {
        List<FileFolder> list = new ArrayList<>();
        while(resultSet.next()) {
            FileFolder f = new FileFolder(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getLong(5),
                    resultSet.getTimestamp(6),
                    resultSet.getString(7),
                    resultSet.getShort(8),
                    resultSet.getString(9)
            );

            list.add(f);
        }

        return list;
    }

    @Override
    public void insertFile(FileFolder file) {
        String sql =
                "INSERT INTO file VALUES (CAST(? AS ltree),?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (path_id, fdirectory) DO UPDATE SET " +
                        "fname = ?, ftype = ?, ffull_name = ?," +
                        "file_size = ?, last_modified = ?," +
                        "fpath = ?, fdirectory = ?, parent = ?";

        List<Object> list = new ArrayList<>(file.getList());
        list.addAll(file.getList().subList(1, file.getList().size()));
        PreparedStatement statement = createStatement(list, sql);
        try {
            statement.execute();
        } catch (SQLException e) {
            System.out.println("ERROR: failed to rep file " + statement);
            e.printStackTrace();
        }
    }

    @Override
    public List<FileFolder> selectAllFiles() {
        String sql = "SELECT * FROM file";
        return jdbcTemplate.query(sql, this::extractResult);
    }

    @Override
    public Optional<List<FileFolder>> selectFolderByID(String id) {
        String sql = "SELECT * FROM file WHERE path_id ~ '"+id+".*{1}';";
        List<FileFolder> file = jdbcTemplate.query(sql, this::extractResult);
        return Optional.ofNullable(file);
    }

    @Override
    public Optional<List<FileFolder>> selectFileById(String id) {
        String sql = "SELECT * FROM file WHERE path_id = '"+id+"'";
        List<FileFolder> file = jdbcTemplate.query(sql, this::extractResult);
        return Optional.ofNullable(file);
    }

    public Optional<List<FileFolder>> selectById(String id) {
        String sql = "SELECT * FROM file WHERE path_id <@ '"+id+"'";
        List<FileFolder> file = jdbcTemplate.query(sql, this::extractResult);

        return Optional.ofNullable(file);
    }

    @Override
    public void deleteFileById(String id, short dir, String type) {
        String sql = "DELETE FROM file WHERE path_id = '"+id+"'" +
                " AND fdirectory = '"+dir+"' AND ftype = '"+type+"'";
        jdbcTemplate.update(sql);
    }

    @Override
    public int moveFileById(UpdateFile file) {
        int result = -1;
        PreparedStatement statement = createStatement(file.getList(), file.toString());
        try {
            result = statement.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("ERROR: failed to update correctly " + statement);
            throwables.printStackTrace();
        }

        return result;
    }

    @PreDestroy
    public void onDestory() {
        System.out.println("INFO: closing database connection...");
        try {
            conn.close();
        } catch (SQLException throwables) {
            System.out.println("ERROR: failed to close connection");
            throwables.printStackTrace();
        }
    }
}
