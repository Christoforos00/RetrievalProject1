import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class Word2VecRanker {
    public static boolean saved = false;

    public static void Index(String path , String indexPath)  {
        try {

            IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            IndexWriter indexWriter = new IndexWriter(FSDirectory.open(Paths.get(indexPath)), config);
            FieldType ft = new FieldType(TextField.TYPE_STORED);
            ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            ft.setTokenized(true);
            ft.setStored(true);
            ft.setStoreTermVectors(true);
            ft.setStoreTermVectorOffsets(true);
            ft.setStoreTermVectorPositions(true);

            for ( CustomDoc cDoc : Utils.getAllDocs(path) ) {
                Document doc = new Document();
                doc.add(new StoredField("id", cDoc.getId()));
                doc.add(new Field("contents", cDoc.getTitle() + " " + cDoc.getBody(), ft));

                if (indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                    indexWriter.addDocument(doc);
                }
            }
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Rank(String indexPath ,String resultsPath, int k){
        Word2Vec vec = null;
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            String fieldName = "contents";
            FieldValuesSentenceIterator fieldValuesSentenceIterator = new FieldValuesSentenceIterator(indexReader, fieldName);

            vec = new Word2Vec.Builder()
                    .layerSize(50)
                    .windowSize(6)
                    .tokenizerFactory(new DefaultTokenizerFactory())
                    .iterate(fieldValuesSentenceIterator)
                    .elementsLearningAlgorithm(new CBOW<>())
                    .seed(12345)
                    .build();
            vec.fit();

            if (!saved){
                WordVectorSerializer.writeWord2VecModel(vec, "TrainedModel/trainedModel.txt");
                saved = true;
                System.out.println("SAVED");
            }

            // is commented out, since we train the model above
//            vec = WordVectorSerializer.readWord2VecModel(new File("PreTrainedModel/model.txt"));

            System.out.println(vec.wordsNearest("big",5));

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new WordEmbeddingsSimilarity(vec, fieldName, WordEmbeddingsSimilarity.Smoothing.MEAN));
            rankResults( indexSearcher, vec, fieldName, resultsPath, k);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }finally {
            System.out.println("DONE k: " + k);
        }

    }



    public static void rankResults(IndexSearcher indexSearcher, Word2Vec vec, String fieldName , String resultsPath, int k) throws IOException, ParseException {

        QueryParser parser = new QueryParser(fieldName, new WhitespaceAnalyzer());
        int qNum = 1;
        String text = "";

        for (String queryString :Utils.getAllQueries(System.getProperty("user.dir")+"/lisa/LISA.QUE" )) {

            queryString = removeUnknownTerms(queryString, vec);

            TopDocs hits = indexSearcher.search(parser.parse(queryString), k);
            ScoreDoc[] hitsArray = hits.scoreDocs;

            for (int i = 0; i < hits.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                Document doc = indexSearcher.doc(scoreDoc.doc);
                text += qNum + "\t0\t" + doc.get("id") + "\t0\t" + hitsArray[i].score + "\tmethod1" + "\n";

            }
            System.out.println(qNum + " query done");
            qNum++;
        }
        try (PrintWriter out = new PrintWriter(resultsPath+"/RESULTS_k"+ k +".test")) {
            out.println(text );
        }
    }

    public static String removeUnknownTerms(String queryString, Word2Vec vec){
        String[] terms = queryString.split(" ");
        queryString = "";

        for (String term : terms){
            if (!term.contains(" ") && vec.vocab().words().contains(term)){
                queryString += term + " ";
            }
        }
        if (queryString.length()>0)
            queryString = queryString.substring(0,queryString.length()-1);

        return queryString;
    }

    public static void main(String[] args) {
//        Index(System.getProperty("user.dir")+"/lisa" , "Index4" );

        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Rank("Index4" ,"phase4Results", k);
    }

}
