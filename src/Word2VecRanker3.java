import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

public class Word2VecRanker3 {



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
//            vec = WordVectorSerializer.readWord2VecModel("PreTrainedModel/model.txt");


            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(new WordEmbeddingsSimilarity(vec, fieldName, WordEmbeddingsSimilarity.Smoothing.MEAN));


            rankResults(indexReader, indexSearcher, vec, fieldName, resultsPath, k);


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }finally {
            System.out.println("DONE");
//            WordVectorSerializer.writeWord2VecModel(vec, "TrainedModel/model.txt");
        }

    }



    public static void rankResults(IndexReader indexReader , IndexSearcher indexSearcher, Word2Vec vec, String fieldName , String resultsPath, int k) throws IOException, ParseException {

        QueryParser parser = new QueryParser(fieldName, new WhitespaceAnalyzer());
        int qNum = 1;
        String text = "";

        for (String queryString :Utils.getAllQueries(System.getProperty("user.dir")+"/lisa/LISA.QUE" )) {
            System.out.println("a");
            String[] terms = queryString.split(" ");
            INDArray denseAverageQueryVector = vec.getWordVectorsMean(Arrays.asList(terms));
            TopDocs hits = indexSearcher.search(parser.parse(queryString), k);
            ScoreDoc[] hitsArray = hits.scoreDocs;
            System.out.println("b");
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                Document doc = indexSearcher.doc(scoreDoc.doc);
                text += qNum + "\t0\t" + doc.get("id") + "\t0\t" + hitsArray[i].score + "\tmethod1" + "\n";

                System.out.println(doc.getField("id") + " : " + scoreDoc.score);

                //Explanation ex = searcher.explain(query, scoreDoc.doc);
                //System.out.println(ex);

                Terms docTerms = indexReader.getTermVector(scoreDoc.doc, fieldName);
                INDArray denseAverageDocumentVector = VectorizeUtils.toDenseAverageVector(docTerms, vec);

                //            INDArray denseAverageTFIDFDocumentVector = VectorizeUtils.toDenseAverageTFIDFVector(docTerms, indexReader.numDocs(), vec);
                System.out.println("cosineSimilarityDenseAvg=" + Transforms.cosineSim(denseAverageQueryVector, denseAverageDocumentVector));
                //            System.out.println("cosineSimilarityDenseAvgTFIDF=" + Transforms.cosineSim(denseAverageTFIDFQueryVector, denseAverageTFIDFDocumentVector));
            }
            System.out.println(qNum + " query done");
            qNum++;
        }
        try (PrintWriter out = new PrintWriter(resultsPath+"/RESULTS_k"+ k +".test")) {
            out.println(text );
        }
    }


    public static List<Integer> getTopDocs(HashMap<Integer, Double> similarities, int k){
        Map<Integer, Double> sortedSim = sortByValue(similarities);
        ArrayList<Integer> topDocs = new ArrayList<>(sortedSim.keySet());
        return topDocs.subList(0,k);
    }

    private static Map<Integer, Double> sortByValue(Map<Integer, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(hm.entrySet());
        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });
        // put data from sorted list to hashmap
        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }



    public static void main(String[] args) {
        Index(System.getProperty("user.dir")+"/lisa" , "Index4" );
//        Reader.Read();
        Rank("Index4" ,"phase4Results", 50);
        Utils.mergeAllDocs(System.getProperty("user.dir")+"/lisa" );
    }

}
