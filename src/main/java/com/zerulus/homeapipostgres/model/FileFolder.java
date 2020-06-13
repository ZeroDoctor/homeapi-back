package com.zerulus.homeapipostgres.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zerulus.homeapipostgres.util.Utils;

import javax.validation.constraints.NotBlank;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFolder implements Serializable {

    @NotBlank private String id;
    private String name;
    private String type;
    private String fullName;
    private long size;
    private Timestamp date;
    private String path;
    private short dir = -1;
    private String parent;

    public FileFolder() {this.parent = "root";}

    public FileFolder(File content, String id, String parent) {
        name = Utils.replaceSym(content.getName());

        if(content.isDirectory()) {
            type = "File";
            name = name.replaceAll("\\.", "_o_");
        } else {
            String[] fdefault = {"", "File"};
            String[] file = getExtension(content.getName()).orElse(fdefault);
            name = (Utils.rangeId(name,1)).replaceAll("\\.", "_o_");
            type = file[file.length - 1];

            if(name.length() <= 0) {
                name = "_o_" + type;
                type = "File";
            }
        }

        this.fullName = content.getName();
        this.size = content.length();
        this.date = new Timestamp(content.lastModified());
        this.path = content.getPath();
        this.dir = (short) (content.isDirectory() ? 1 : 0);
        this.parent = parent;

        this.id = cleanId(id + name);
    }

    public FileFolder(@JsonProperty("path_id") String id,
                      @JsonProperty("fname") String name,
                      @JsonProperty("ftype") String type,
                      @JsonProperty("ffull_name") String fullName,
                      @JsonProperty("file_size") long size,
                      @JsonProperty("last_modified") Timestamp date,
                      @JsonProperty("fpath") String path,
                      @JsonProperty("fdirectory") short dir,
                      @JsonProperty("parent") String parent) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.fullName = fullName;
        this.size = size;
        this.date = date;
        this.path = path;
        this.dir = dir;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getFullName() { return fullName; }
    public long getSize() {
        return size;
    }
    public Date getDate() {
        return date;
    }
    public String getPath() { return path; }
    public void setPath(String path) {this.path = path;}
    public int getDir() { return dir; }

    public void setParent(String parent) { this.parent = parent; }
    public String getParent() {
        return parent;
    }

    private String cleanId(String id) {
        id = id.replaceAll("[^A-Za-z0-9_.]", "");
        return id;
    }

    private Optional<String[]> getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.split("\\."));
    }

    public List<Object> getList(){
        List<Object> result = new ArrayList<>();
        result.add(id);
        result.add(name);
        result.add(type);
        result.add(fullName);
        result.add(size);
        result.add(date);
        result.add(path);
        result.add(dir);
        result.add(parent);

        return result;
    }

    public String toString() {
        return "'"+id+"','"+name+"','"+type+"','"+fullName+
                "','"+size+"','"+date+"','"+path+"','"+dir+"','"+parent+"'";
    }
}
