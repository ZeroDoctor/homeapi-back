package com.zerulus.homeapipostgres.seed;

import com.zerulus.homeapipostgres.dao.FolderDataAccessService;
import com.zerulus.homeapipostgres.model.FileFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Stack;

@Component
public class DBSeeder implements CommandLineRunner {

    private final FolderDataAccessService folderService;

    @Autowired
    public DBSeeder(FolderDataAccessService folderService) {
        this.folderService = folderService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Updating root directory... ");

        if(args.length > 0 && args[0].equals("--refresh")) {
            System.out.println("Done!");
            

            Stack<File> localStack = new Stack<File>(); // for traversing local file system
            Stack<FileFolder> virtualStack = new Stack<FileFolder>(); // for creating virtual file system
            ArrayList<FileFolder> folderList = new ArrayList<FileFolder>();

            // getting local root folder
            String sysRoot = System.getProperty("user.dir") + "/root";
            File dirPath = new File(sysRoot);
     
            if(!dirPath.exists()) {
                System.out.print("ERROR: please create a root folder like so: mkdir ");
                System.out.println(sysRoot);
                System.exit(0);
            }
            boolean isDir;
            localStack.push(dirPath);

            // creating virtual root folder
            FileFolder parent = new FileFolder(
                    "root", "root", "File",
                    "root", 0, new Timestamp((new Date().getTime())),
                    "root", (short) 1, "_"
            );
            virtualStack.push(parent);

            int count = 0;
            // traverse and create local and virtual tree respectively
            File[] contents = new File[0];
            while(!localStack.isEmpty()) {

                contents = localStack.pop().listFiles();
                parent = virtualStack.pop();

                if (contents != null) {
                    for (File content : contents) {
                        isDir = content.isDirectory();
                        FileFolder node = new FileFolder(content, parent.getId() + ".", parent.getId());
                        folderService.insertFile(node);
                        count++;
                        if(count % 50 == 0) System.out.println("INFO: finished repping " + count + " files/folders");

                        if (isDir) {
                            localStack.push(content);
                            virtualStack.push(node); // is a folder so we will see again
                        } else {
                            folderList.add(node); // is a file so we will not see again
                        }

                        //parent.getChildren().add(node.getId());
                    }
                }

                folderList.add(parent);
            }
        }

        //folderService.closeConn();
        System.out.println("Done!");
    }
}
