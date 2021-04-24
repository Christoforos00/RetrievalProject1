import java.io.*;
import java.util.ArrayList;

public class Utils {

    public static ArrayList<CustomDoc> getAllDocs(String path){
        ArrayList<CustomDoc> docs = new ArrayList<CustomDoc>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
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
        int titleEndIndex = batch.indexOf(".");
        while( !batch.substring(titleEndIndex+1,titleEndIndex+2).equals("\n" ) ){
            titleEndIndex = batch.indexOf(".",titleEndIndex+1);
        }
        String title = batch.substring(0,titleEndIndex+1).replace("\n"," ").trim();
        String body = batch.substring(titleEndIndex+1, batch.length() ).replace("\n"," ").trim();;
        return new CustomDoc(title, body );
    }


    public static ArrayList<String> getAllQueries(String path){
        ArrayList<String> queries = new ArrayList<String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String currentQuery = getNextQuery(reader);

            while(!currentQuery.equals("")) {
                queries.add( currentQuery );
                currentQuery = getNextQuery(reader);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  queries;

    }


    public static String getNextQuery(BufferedReader reader) throws IOException {
        String batch = "";
        reader.readLine();
        String line = reader.readLine();
        while ( line !=null  ){
            batch += line + " ";
            if (line.contains("#")) {
                batch = batch.substring(0, batch.length() - 2);
                break;
            }
            line = reader.readLine();
        }
        return batch;
    }




    public static void main(String[] args) {
//        Utils.getAllDocs( System.getProperty("user.dir")+"/lisa"  );
        Utils.getAllQueries( System.getProperty("user.dir")+"/lisa/LISA.QUE"  );

    }




}
