package iocontroller.preprocessor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Created by phil on 14.07.15.
 */
public class SentimentAnalyser {

    protected StanfordCoreNLP pipeline;

    public SentimentAnalyser() {
        // Create StanfordCoreNLP object properties
        Properties props;
        props = new Properties();
        props.put("tokenize.options", "untokenizable=noneDelete");
        props.put("annotators", "tokenize, ssplit, parse, sentiment");
        // StanfordCoreNLP loads a lot of models, so we only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }


    public int getSentiment(String text) {
        int mainSentiment = 0;
        int longest = 0;

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            //Get sentiment for sentence

            Tree tree = sentence
                    .get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);


            String partText = sentence.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }


        }

        return mainSentiment;
    }
}
