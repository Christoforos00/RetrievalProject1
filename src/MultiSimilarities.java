import org.apache.lucene.search.similarities.*;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiSimilarities {


    public static void main(String[] args) {
        String path = System.getProperty("user.dir")+"/lisa";

        Similarity similarity;

        similarity =  new MultiSimilarity(new Similarity[] {new ClassicSimilarity(), new BM25Similarity()});
        Indexer.indexFiles( path , "Index5/CLASSIC+BM25index" , similarity , false);
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/CLASSIC+BM25index" , "lisa/LISA.QUE" , "phase5Results/CLASSIC+BM25results" , "contents",k , null, similarity);
        System.out.println("1 done.");

        similarity = new MultiSimilarity(new Similarity[] {new ClassicSimilarity(),  new LMJelinekMercerSimilarity(0.3f)});
        Indexer.indexFiles( path , "Index5/CLASSIC+LMindex" , similarity , false);
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/CLASSIC+LMindex"  , "lisa/LISA.QUE" , "phase5Results/CLASSIC+LMresults" , "contents",k , null, similarity);
        System.out.println("2 done.");

        similarity =  new MultiSimilarity(new Similarity[] {new BM25Similarity(), new LMJelinekMercerSimilarity(0.3f)});
        Indexer.indexFiles( path , "Index5/BM25+LMindex" , similarity, false );
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/BM25+LMindex" , "lisa/LISA.QUE" , "phase5Results/BM25+LMresults" , "contents",k , null, similarity);
        System.out.println("3 done.");

        Word2Vec vec = WordVectorSerializer.readWord2VecModel(new File("PreTrainedModel/model.txt"));

        similarity =   new MultiSimilarity(new Similarity[] {new WordEmbeddingsSimilarity(vec, "contents", WordEmbeddingsSimilarity.Smoothing.MEAN), new BM25Similarity()});
        Indexer.indexFiles( path , "Index5/WV+BM25index" ,similarity , true );
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/WV+BM25index", "lisa/LISA.QUE" , "phase5Results/WV+BM25results" , "contents",k , vec, similarity);
        System.out.println("4 done.");

        similarity =  new MultiSimilarity(new Similarity[] {new WordEmbeddingsSimilarity(vec, "contents", WordEmbeddingsSimilarity.Smoothing.MEAN), new ClassicSimilarity()});
        Indexer.indexFiles( path , "Index5/WV+CLASSICindex" , similarity , true );
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/WV+CLASSICindex" , "lisa/LISA.QUE" , "phase5Results/WV+CLASSICresults" , "contents",k , vec, similarity);
        System.out.println("5 done.");

        similarity =  new MultiSimilarity(new Similarity[] {new WordEmbeddingsSimilarity(vec, "contents", WordEmbeddingsSimilarity.Smoothing.MEAN), new LMJelinekMercerSimilarity(0.3f)});
        Indexer.indexFiles( path , "Index5/WV+LMindex" , similarity, true );
        for ( int k : new ArrayList<Integer>(Arrays.asList(20,30,50)) )
            Searcher.search("Index5/WV+LMindex" , "lisa/LISA.QUE" , "phase5Results/WV+LMresults" , "contents",k , vec, similarity);
        System.out.println("6 done.");

    }
}
