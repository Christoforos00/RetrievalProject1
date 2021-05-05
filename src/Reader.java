import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class Reader {

    public static void Read(){

        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("Index")));
            printIndexDocuments(indexReader);

            //Close indexReader
            indexReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printIndexDocuments(IndexReader indexReader){
        try {
            System.out.println("--------------------------");
            System.out.println("Documents in the index...");

            for (int i=0; i<indexReader.maxDoc(); i++) {
                Document doc = indexReader.document(i);
                System.out.println("\tid="+doc.getField("id"));
            }
        } catch (CorruptIndexException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public static void main(String[] args) {
        Read();
    }
}
