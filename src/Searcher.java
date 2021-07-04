
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class Searcher {

    public static void search(String indexPath , String queriesPath , String resultsPath, String field, int k , Similarity similarity) {
        try {
            IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(similarity );
            searchQueries(indexSearcher, queriesPath, resultsPath, field, k);
            indexReader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void searchQueries(IndexSearcher indexSearcher, String queriesPath, String resultsPath, String field, int k) throws ParseException, IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        int qNum = 1;
        String text = "";
        for (String query : Utils.getAllQueries(queriesPath)){
            TopDocs results = indexSearcher.search(parser.parse(query), k);
            ScoreDoc[] hits = results.scoreDocs;

            for(int i=0; i<hits.length; i++){
                Document hitDoc = indexSearcher.doc(hits[i].doc);
                System.out.println(hits[i].doc);
                text += qNum + "\t0\t" + hitDoc.get("id") + "\t0\t" +hits[i].score + "\tmethod1" + "\n";
            }
            qNum++;
        }
        text = text.trim();
        try (PrintWriter out = new PrintWriter(resultsPath+"/RESULTS_k"+ k +".test")) {
            out.println(text );
        }
    }

    public static void main(String[] args) {

        //phase1
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search(System.getProperty("user.dir")+"/Index" , System.getProperty("user.dir")+"/lisa/LISA.QUE" , "phase1Results" ,"contents",k, new ClassicSimilarity());

        //phase 3
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search(System.getProperty("user.dir")+"/Index3_BM25" , System.getProperty("user.dir")+"/lisa/LISA.QUE" , "phase3Results/BM25results" ,"contents",k, new BM25Similarity());

        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search(System.getProperty("user.dir")+"/Index3_LM" , System.getProperty("user.dir")+"/lisa/LISA.QUE" , "phase3Results/LMresults" , "contents",k , new LMJelinekMercerSimilarity(0.3f));

        System.out.println("Searching is done.");
    }

}
