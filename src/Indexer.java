import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;



public class Indexer {

    public static void indexFiles(String path){

        try {
            Directory indexDir = FSDirectory.open(Paths.get("Index"));
            Analyzer analyzer = new EnglishAnalyzer();
            Similarity similarity = new ClassicSimilarity();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(indexDir, iwc);

            for ( CustomDoc cDoc : Utils.getAllDocs(path) ){
                Document doc = new Document();
                doc.add( new StoredField("Id",
                        cDoc.getId()) );
                doc.add( new TextField("Contents",
                        cDoc.getTitle()+cDoc.getBody() , Field.Store.YES) );

                if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE) {
                    indexWriter.addDocument(doc);
                }
            }
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Indexer.indexFiles(System.getProperty("user.dir")+"/lisa" );
    }
}
