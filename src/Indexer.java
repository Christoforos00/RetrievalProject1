import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.IOException;
import java.nio.file.Paths;




public class Indexer {

    public static void indexFiles(String path , String indexPath, Similarity similarity){

        try {
            Directory indexDir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(indexDir, iwc);

            for ( CustomDoc cDoc : Utils.getAllDocs(path) ){
                Document doc = new Document();
                doc.add( new StoredField("id", cDoc.getId()) );
                doc.add( new TextField("contents",cDoc.getTitle()+" "+cDoc.getBody() , Field.Store.NO) );

                if (indexWriter.getConfig().getOpenMode() == OpenMode.CREATE)
                    indexWriter.addDocument(doc);

            }
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Utils.generateTrecEvalQrels(System.getProperty("user.dir") + "/lisa/LISARJ.NUM");

        String path = System.getProperty("user.dir")+"/lisa";

        Indexer.indexFiles( path , "Index" , new ClassicSimilarity());

        Indexer.indexFiles( path , "Index3_BM25" , new BM25Similarity());
        Indexer.indexFiles( path , "Index3_LM" , new LMJelinekMercerSimilarity(0.3f));

        System.out.println("Indexing is done.");
    }

}
