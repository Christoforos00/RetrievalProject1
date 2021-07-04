

import org.apache.lucene.queryparser.classic.ParseException;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Word2VecRanker2 {

    
    public static void Rank(String collectionPath ,String resultsPath, int k){
        Word2Vec vec = null;
        try {

            vec = WordVectorSerializer.readWord2VecModel("PreTrainedModel/model.txt");
            rankResults( vec,collectionPath, resultsPath, k);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }finally {
            System.out.println("DONE");
        }
    }



    public static void rankResults( Word2Vec vec, String collectionPath, String resultsPath, int k) throws IOException, ParseException {

        int qNum = 1;
        String text = "";
        ArrayList<CustomDoc> allDocs = Utils.getAllDocs(collectionPath);

        for (String queryString :Utils.getAllQueries(System.getProperty("user.dir")+"/lisa/LISA.QUE" )) {
            String[] terms = queryString.split(" ");
            INDArray denseAverageQueryVector = VectorizeUtils.averageWordVectors( Arrays.asList(terms), vec);
            HashMap<Integer, Double> similarities = new HashMap<>();

            for ( CustomDoc doc : allDocs){
                String docString = doc.getTitle() + " " + doc.getBody();
                String[] docTerms = docString.split(" ");
                INDArray denseAverageDocumentVector = VectorizeUtils.averageWordVectors( Arrays.asList(docTerms), vec);
                double sim = Transforms.cosineSim(denseAverageQueryVector , denseAverageDocumentVector);
                similarities.put(Integer.parseInt(doc.getId()) , sim );

            }

            for (int docId : getTopDocs(similarities, k)){
                text += qNum + "\t0\t" + docId + "\t0\t" + similarities.get(docId) + "\tmethod1" + "\n";
            }

            System.out.println(qNum + " query done");
            qNum++;
        }
        try (PrintWriter out = new PrintWriter(resultsPath+"/RESULTS2_k"+ k +".test")) {
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

        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Rank("lisa" ,"phase4Results", k);
    }


}
