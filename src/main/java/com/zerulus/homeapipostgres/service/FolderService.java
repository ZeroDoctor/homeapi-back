package com.zerulus.homeapipostgres.service;

import com.zerulus.homeapipostgres.dao.FolderDao;
import com.zerulus.homeapipostgres.model.FileFolder;
import com.zerulus.homeapipostgres.util.UpdateFile;
import com.zerulus.homeapipostgres.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.*;
import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Service
public class FolderService {

    private final FolderDao folderDao;
    private final String root;

    @Autowired
    public FolderService(@Qualifier("postgres") FolderDao folderDao) {
        this.root = System.getProperty("user.dir");
        this.folderDao = folderDao;
    }

    public void addFile(FileFolder fileFolder) { folderDao.insertFile(fileFolder); }
    public List<FileFolder> getAllFiles() { return folderDao.selectAllFiles(); }
    public Optional<List<FileFolder>> getFolderByID(String id) { return folderDao.selectFolderByID(id); }
    public Optional<List<FileFolder>> getFileById(String id) { return folderDao.selectFileById(id); }
    public String deleteById(String id, boolean isDir) {
        FileFolder file = getFileFolderId(id, isDir);
        String path = file.getPath();

        if(delete(new File(path))) {
            if(!isDir) id = Utils.rangeId(id, 1);
            folderDao.deleteFileById(id, (short) file.getDir(), file.getType());
            return "deleted: " + path;
        }
        return "failed to delete: " + path;
    }

    public String createFolder(String id) {
        String result = "ERROR: Folder " + id + " not created...";

        String path = root + "/" + id.replace(".", "/");
        File file = new File(path);
        if(!file.exists()) {
            if (file.mkdir()) {
                result = "INFO: Folder " + id + " created...";
            }

            folderDao.insertFile(new FileFolder(file, Utils.rangeId(id, 1) + ".", Utils.rangeId(id, 1)));
        }


        return  result;
    }

    // i.e. {id}="root.Testing.file" {newId}="root.OtherFolder.file"
    public int moveFileById(String id, String newId, boolean isDir) {
        FileFolder file = getFileFolderId(id, isDir);

        if(file != null) {
            String newPath = root + "/" + newId.replace(".", "/");
            String path = file.getPath();

            return move(path, newPath, newId, file);
        }

        return 500;
    }

    public Resource download(String id, boolean isDir) throws InterruptedException {
        Resource result = null;
        FileFolder filefolder = getFileFolderId(id, isDir);
        if(filefolder == null) return null;
        if(!isDir) {
            result = downloadFile(filefolder.getPath());
        } else {
            String[] temp = id.split("\\.");
            String name = temp[temp.length - 1];
            File file = downloadFolder(filefolder.getPath(), name);

            if(file != null) {
                result = downloadFile(file.getPath() + ".zip");
                String parent = Utils.rangeId(id, 1);
                FileFolder fileFolder = null;
                try {
                    fileFolder = new FileFolder(result.getFile(), parent+".", parent);
                } catch (IOException e) {
                    System.out.println("ERROR: failed to grab .zip folder");
                    e.printStackTrace();
                }
                folderDao.insertFile(fileFolder);
            }
        }

        return result;
    }

    public Resource downloadFile(String path) {
        Path pathToFile = Paths.get(path);

        UrlResource resource = null;
        try {
            resource = new UrlResource(pathToFile.toUri());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resource;
    }

    public File downloadFolder(String path, String name) {
        File fileToZip = null;
        try {
            FileOutputStream fos = new FileOutputStream(path + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            fileToZip = new File(path);

            zipFile(fileToZip, fileToZip.getName(), zipOut);
            fos.flush();
            zipOut.flush();
            zipOut.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("OH NO STINKY");
            e.printStackTrace();
        }

        return fileToZip;
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if(fileToZip.isHidden()) return;
        if(fileToZip.isDirectory()) {
            if(fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }

            File[] children = fileToZip.listFiles();
            for(File child : children) {
                zipFile(child, fileName + "/" + child.getName(), zipOut);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        fis.close();
    }

    public FileFolder upload(MultipartFile file, String path) {
        String destPath  = root + "/" + path.replace('.', '/') + "/";
        Path createFile = createFile(file, destPath + file.getOriginalFilename());
        String parent = path;
        path = path + ".";

        System.out.println("WARNING: check if path and parent is correct... " + path + " | " + parent);
        return new FileFolder(createFile.toFile(), path, parent);
    }

    /** Helper methods below */
    public FileFolder getFileFolderId(String id, boolean isDir) {
        String tempId = id;
        if(!isDir) tempId = Utils.rangeId(id, 1);
        List<FileFolder> files = folderDao.selectFileById(tempId).orElse(null);

        FileFolder file = null;
        String[] idArr = id.split("\\.");
        if (files != null) {
            for(FileFolder f: files) {
                if(isDir && (f.getDir() == 1)) {
                    file = f;
                    break;
                }
                else if(idArr[idArr.length - 1].equals(f.getType()) && !(isDir && (f.getDir() == 1))) {
                    file = f;
                    break;
                }
            }
        }

        return file;
    }

    public int move(String path, String newPath, String newId, FileFolder file) {
        Path source = Paths.get(path);
        Path target = Paths.get(newPath + "/" + file.getFullName());

        try {
            if(file.getDir() == 1) {
                System.out.println("moving directory...");
                
                boolean b = moveDir(source, target, file.getName(), file.getParent(), newId, file.getType());
                if(!b) {
                    System.out.println("Failed to move some, most, or all files...");
                    return 505;
                }
            } else {
                System.out.println("moving file...");
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                UpdateFile newFile = new UpdateFile(
                        file.getId(),
                        newId + "." + file.getName(),
                        newPath + "/" + file.getFullName(),
                        newId,
                        file.getType()
                );

                folderDao.moveFileById(newFile);
            }
        } catch(IOException e) {
            e.printStackTrace();
            return 404;
        }

        return 200;
    }

    public boolean moveDir(Path src, Path tar, String parent, String superParent, String newParent, String type) {
        File[] files = src.toFile().listFiles();
        boolean isDir = src.toFile().isDirectory();

        if(files != null && isDir) {
            for(File file : files) {
                String[] parts = (file.getName().split("\\."));
                String t = parts[parts.length - 1];
                moveDir(
                        file.toPath(),
                        tar.resolve(src.relativize(file.toPath())),
                        parent + "+" + file.getName(),
                        superParent, newParent, t
                );
            }
        }

        String cleanedParent = Utils.cleanId(parent);
        String newId = newParent + "." + cleanedParent;
        String nParent = newParent + "." + Utils.cleanId(cleanedParent, 1);
        if(isDir) {
            type = "File";
            nParent = Utils.rangeId(newId, 1);
        }

        UpdateFile newFile = new UpdateFile(
                superParent + "." + cleanedParent,
                newId,
                tar.toString(),
                nParent,
                type
        );

        try {
            Files.move(src, tar, StandardCopyOption.REPLACE_EXISTING);
            folderDao.moveFileById(newFile);
        } catch (NoSuchFileException e) {
            folderDao.moveFileById(newFile);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean delete(File file) {
        if(file.exists()) {
            File[] files = file.listFiles();
            if(files == null) return (file.delete());
            for (File value : files) {
                if (value.isDirectory() && !Files.isSymbolicLink(value.toPath())) {
                    delete(value);
                }
                if(!value.delete()) return false;
            }
        }

        return (file.delete());
    }

    public Path createFile(MultipartFile file, String path) {
        Path newPath = Paths.get(path);
        try {
            Files.copy(
                    file.getInputStream(),
                    newPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newPath;
    }
    /** END */
}
