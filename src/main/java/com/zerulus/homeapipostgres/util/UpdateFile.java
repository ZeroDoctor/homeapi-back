package com.zerulus.homeapipostgres.util;

import java.util.ArrayList;
import java.util.List;

public class UpdateFile {

    private final String id;

    private final String newId;
    private final String newPath;
    private final String newParent;
    private final String type;

    public UpdateFile(String id, String newId, String newPath, String newParent, String type) {
        this.id = id;
        this.newId = newId;
        this.newPath = newPath;
        this.newParent = newParent;
        this.type = type;
    }

    public List<Object> getList() {
        List<Object> result = new ArrayList<>();
        result.add(newId);
        result.add(newPath);
        result.add(newParent);
        result.add(id);
        result.add(type);

        return result;
    }

    public String toString() {
        return "UPDATE file SET (path_id, fpath, parent) = (CAST(? AS ltree),?,?) WHERE path_id = CAST(? AS ltree) AND ftype = ?";
    }
}
