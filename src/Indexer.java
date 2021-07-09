import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;



public class Indexer {

    public static void indexFiles(String path , String indexPath, Similarity similarity, boolean withEmb){

        try {
            Directory indexDir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setSimilarity(similarity);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(indexDir, iwc);

            FieldType ft = null;
            if (withEmb){
                ft = new FieldType(TextField.TYPE_STORED);
                ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
                ft.setTokenized(true);
                ft.setStored(true);
                ft.setStoreTermVectors(true);
                ft.setStoreTermVectorOffsets(true);
                ft.setStoreTermVectorPositions(true);
            }


            for ( CustomDoc cDoc : Utils.getAllDocs(path) ){
                Document doc = new Document();
                doc.add( new StoredField("id", cDoc.getId()) );

                if (withEmb)
                    doc.add(new Field("contents", cDoc.getTitle() + " " + cDoc.getBody(), ft));
                else
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

        Indexer.indexFiles( path , "Index" , new ClassicSimilarity(),false);
        Indexer.indexFiles( path , "Index3_BM25" , new BM25Similarity(), false);
        Indexer.indexFiles( path , "Index3_LM" , new LMJelinekMercerSimilarity(0.3f), false);

    }

}
