package com.zerulus.homeapipostgres.api;

import com.zerulus.homeapipostgres.model.FileFolder;
import com.zerulus.homeapipostgres.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RequestMapping("api")
@RestController
public class FolderController {

    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping(path="{id}")
    public String addFile(MultipartFile file, @PathVariable("id") String path) {
        System.out.print("Processing Post Upload Reqeust " + path + "... ");
        FileFolder fileFolder = folderService.upload(file, path);
        folderService.addFile(fileFolder);
        System.out.println("Done!");

        return "uploaded to: " + fileFolder.getId();
    }

    @GetMapping
    public List<FileFolder> getAllFiles() {
        return folderService.getAllFiles();
    }

    @GetMapping(path="{id}")
    public List<FileFolder> getFolderById(@PathVariable("id") String id) {
        System.out.print("Processing Get Folder Reqeust " + id + "... ");
        List<FileFolder> result = folderService.getFolderByID(id).orElse(null);
        System.out.println("Done!");
        return result;
    }

    @GetMapping(path="file/{id}")
    public List<FileFolder> getFileById(@PathVariable("id") String id) {
        System.out.print("Processing Get File Reqeust " + id + "... ");
        List<FileFolder> result = folderService.getFileById(id).orElse(null); 
        System.out.println("Done!");
        return result;
    }

    @GetMapping(path="download/file/{id}")
    public Resource downloadFileById(@PathVariable("id") String id) {
        try {
            System.out.print("Processing Get Download File Reqeust " + id + "... ");
            Resource result = folderService.download(id, false);
            System.out.println("Done!");
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @GetMapping(path="download/folder/{id}")
    public  Resource downloadFolderById(@PathVariable("id") String id) {
        try {
            System.out.print("Processing Get Download Folder Reqeust " + id + "... ");
            Resource result = folderService.download(id, true);
            System.out.println("Done!");
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @DeleteMapping(path="file/{id}")
    public String deleteFileById(@PathVariable("id") String id) {
        System.out.print("Processing Delete File Reqeust " + id + "... ");
        String result = folderService.deleteById(id, false); 
        System.out.println("Done!");
        return result;
    }

    @DeleteMapping(path="folder/{id}")
    public String deleteFolderById(@PathVariable("id") String id) {
        System.out.print("Processing Delete Folder Reqeust " + id + "... ");
        String result = folderService.deleteById(id, true); 
        System.out.println("Done!");
        return result;
    }

    @PutMapping(path="/move/folder/old={id}&new={newId}")
    public int moveFolderById(@PathVariable("id") String id, @PathVariable("newId") String newId) {
        return folderService.moveFileById(id, newId, true);
    }

    @PutMapping(path="/move/file/old={id}&new={newId}")
    public int moveFileById(@PathVariable("id") String id, @PathVariable("newId") String newId) {
        return folderService.moveFileById(id, newId, false);
    }

    @PutMapping(path="/mkdir/{id}")
    public String createFolderById(@PathVariable("id") String id) {
        return folderService.createFolder(id);
    }
}
