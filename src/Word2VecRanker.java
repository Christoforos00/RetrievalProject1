import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Word2VecRanker {

    public static void Rank(String indexPath){

        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            String fieldName = "contents";
            FieldValuesSentenceIterator fieldValuesSentenceIterator = new FieldValuesSentenceIterator(indexReader, fieldName);

            Word2Vec vec = new Word2Vec.Builder()
                    .layerSize(50)
                    .windowSize(6)
                    .tokenizerFactory(new DefaultTokenizerFactory())
                    .iterate(fieldValuesSentenceIterator)
                    .seed(12345)
                    .build();
            vec.fit();

            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new WordEmbeddingsSimilarity(vec, fieldName, WordEmbeddingsSimilarity.Smoothing.MEAN));

            for (String query :Utils.getAllQueries(System.getProperty("user.dir")+"/lisa/LISA.QUE" ))
                rankResults(query, indexReader, indexSearcher, vec, fieldName);


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


    }



    public static void rankResults(String queryString, IndexReader indexReader , IndexSearcher indexSearcher, Word2Vec vec, String fieldName) throws IOException, ParseException {
        Terms fieldTerms = MultiFields.getTerms(indexReader, fieldName);
        String[] terms = queryString.split(" ");

        INDArray denseAverageQueryVector = vec.getWordVectorsMean(Arrays.asList(terms));
        QueryParser parser = new QueryParser(fieldName, new WhitespaceAnalyzer());
        Query query = parser.parse(queryString);

        TopDocs hits = indexSearcher.search(query, 10);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = indexSearcher.doc(scoreDoc.doc);

            String title = doc.get(fieldName);
            System.out.println(title + " : " + scoreDoc.score);

            //Explanation ex = searcher.explain(query, scoreDoc.doc);
            //System.out.println(ex);

            Terms docTerms = indexReader.getTermVector(scoreDoc.doc, fieldName);

            INDArray denseAverageDocumentVector = VectorizeUtils.toDenseAverageVector(docTerms, vec);
//            INDArray denseAverageTFIDFDocumentVector = VectorizeUtils.toDenseAverageTFIDFVector(docTerms, indexReader.numDocs(), vec);

            System.out.println("cosineSimilarityDenseAvg=" + Transforms.cosineSim(denseAverageQueryVector, denseAverageDocumentVector));
//            System.out.println("cosineSimilarityDenseAvgTFIDF=" + Transforms.cosineSim(denseAverageTFIDFQueryVector, denseAverageTFIDFDocumentVector));
        }

    }


    public static void main(String[] args) {
        Rank(System.getProperty("user.dir")+"/Index4");
    }

}
