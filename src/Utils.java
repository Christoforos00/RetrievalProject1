import java.io.*;

public class Utils {

    public static void getAllDocs(String path){

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.getName().equals("LISA.QUE") || file.getName().equals("LISA.REL")  || file.getName().equals("LISARJ.NUM")  || file.getName().equals("README")   )
                continue;
            System.out.println(file.getName());
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentBatch = getNextBatch( br );
                System.out.println(currentBatch);
//                CustomDoc currentDoc = batchToDoc(currentBatch);

            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    public static String getNextBatch(BufferedReader br) throws IOException {           // title n body seperated with \n and 6xSPACE
        String line = br.readLine();
        String batch = "";
        while ( line !=null  ){
            if (line.contains("****"))
                break;
            batch += " " + line;
            line = br.readLine();
        }
        return batch;
    }

//    public static CustomDoc batchToDoc(String batch){
//        String parts[] = batch.
//    }

    public static void main(String[] args) {
        Utils.getAllDocs( System.getProperty("user.dir")+"/lisa"  );

    }




}
