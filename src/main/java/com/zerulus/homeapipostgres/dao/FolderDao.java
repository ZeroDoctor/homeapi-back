package com.zerulus.homeapipostgres.dao;

import com.zerulus.homeapipostgres.model.FileFolder;
import com.zerulus.homeapipostgres.util.UpdateFile;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface FolderDao {

    void insertFile(FileFolder folder);
    List<FileFolder> selectAllFiles();
    Optional<List<FileFolder>> selectFolderByID(String id);
    Optional<List<FileFolder>> selectFileById(String id);
    Optional<List<FileFolder>> selectById(String id);
    void deleteFileById(String id, short dir, String type);
    int moveFileById(UpdateFile file);
}
