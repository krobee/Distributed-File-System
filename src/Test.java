import java.io.File;

import util.Config;

public class Test {
     
    public static void main(String[] args)  {
    	long freeSpace = new File(Config.FILE_DIR).getFreeSpace();
    	System.out.println(freeSpace);
    }

 
 
}