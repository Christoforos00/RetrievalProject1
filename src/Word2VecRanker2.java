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
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

public class Word2VecRanker2 {


    public static void TrainModel(String path){
        SentenceIterator iter = null;
        try {
            iter = new BasicLineIterator(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        Word2Vec vec = new Word2Vec.Builder()
                .layerSize(50)
                .windowSize(10)
                .tokenizerFactory(new DefaultTokenizerFactory())
                .iterate(iter)
                .elementsLearningAlgorithm(new CBOW<>())
                .build();
        vec.fit();
        WordVectorSerializer.writeWord2VecModel(vec, "TrainedModel/model.txt");
    }



    public static void Rank(String collectionPath ,String resultsPath, int k){
        Word2Vec vec = null;
        try {
//            SentenceIterator iter = new BasicLineIterator(collectionPath+"/Merged");
//            String fieldName = "contents";

            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get("Index4" )));
            String fieldName = "contents";
            FieldValuesSentenceIterator iter = new FieldValuesSentenceIterator(indexReader, fieldName);

            vec = new Word2Vec.Builder()
                    .layerSize(100)
                    .windowSize(6)
                    .tokenizerFactory(new DefaultTokenizerFactory())
                    .iterate(iter)
                    .elementsLearningAlgorithm(new CBOW<>())
                    .build();
            vec.fit();
//            vec = WordVectorSerializer.readWord2VecModel("PreTrainedModel/model.txt");

            rankResults( vec, fieldName, collectionPath, resultsPath, k);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }finally {
            System.out.println("DONE");
//            WordVectorSerializer.writeWord2VecModel(vec, "TrainedModel/model.txt");
        }
    }



    public static void rankResults( Word2Vec vec, String fieldName ,String collectionPath, String resultsPath, int k) throws IOException, ParseException {

        int qNum = 1;
        String text = "";
        ArrayList<CustomDoc> allDocs = Utils.getAllDocs(collectionPath);

        for (String queryString :Utils.getAllQueries(System.getProperty("user.dir")+"/lisa/LISA.QUE" )) {
            System.out.println(qNum + " query done");
            String[] terms = queryString.split(" ");
            INDArray denseAverageQueryVector = vec.getWordVectorsMean(Arrays.asList(terms));
            HashMap<Integer, Double> similarities = new HashMap<>();

            for ( CustomDoc doc : allDocs){
                String docString = doc.getTitle() + " " + doc.getBody();
                String[] docTerms = docString.split(" ");
                INDArray denseAverageDocumentVector = vec.getWordVectorsMean(Arrays.asList(docTerms));
                double sim = Transforms.cosineSim(denseAverageQueryVector , denseAverageDocumentVector);
                similarities.put(Integer.parseInt(doc.getId()) , sim );

            }

            for (int docId : getTopDocs(similarities, k)){
                text += qNum + "\t0\t" + docId + "\t0\t" + similarities.get(docId) + "\tmethod1" + "\n";
                System.out.println(similarities.get(docId) );
            }

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
//        Index(System.getProperty("user.dir")+"/lisa" , "Index4" );
//        Reader.Read();
        Rank("lisa" ,"phase4Results", 50);
    }


}
