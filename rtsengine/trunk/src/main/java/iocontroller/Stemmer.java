package iocontroller;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by phil on 17.05.2015.
 */
public class Stemmer {

    // private static  Properties props;
    // private static  StanfordCoreNLP pipeline;

    public static void init() {

    }


    public static List<String> stem(String text) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        pipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> wordList=new ArrayList<String>();
        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class))
        {
            for(CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                wordList.add(lemma);
            }
        }
        return wordList;
        //TODO is to remove special symbols
    }

    public static List<String> trivial_stem(String text) {
        List<String> terms = new LinkedList<String>();

        StringTokenizer stringTokenizer = new StringTokenizer(text);
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            terms.add(token);
        }

        return terms;
    }
}
