import java.io.*;
import java.util.ArrayList;

public class Utils {

    public static ArrayList<CustomDoc> getAllDocs(String path){
        ArrayList<CustomDoc> docs = new ArrayList<CustomDoc>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            System.out.println(file);
            if (file.getName().equals("LISA.QUE") || file.getName().equals("LISA.REL")  || file.getName().equals("LISARJ.NUM")  || file.getName().equals("README")   )
                continue;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String currentBatch = getNextBatch(reader);
                while(!currentBatch.equals("")) {
                    docs.add(batchToDoc(currentBatch));
                    currentBatch = getNextBatch(reader);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        System.out.println(docs.size());
        return docs;
    }

    public static String getNextBatch(BufferedReader reader) throws IOException {
        String batch = "";
        reader.readLine();
        String line = reader.readLine();
        while ( line !=null  ){
            if (line.contains("****"))
                break;
            batch += line + "\n";
            line = reader.readLine();
        }
        return batch;
    }

    public static CustomDoc batchToDoc(String batch){
        int titleEndIndex = batch.indexOf("\n ");
        String title = batch.substring(0,titleEndIndex).replace("\n"," ");
        String body = batch.substring(titleEndIndex+6, batch.length() ).replace("\n"," ");
        System.out.println(new CustomDoc(title, body ) );
        return new CustomDoc(title, body );
    }

    public static void main(String[] args) {
        Utils.getAllDocs( System.getProperty("user.dir")+"/lisa"  );

    }




}
