package iocontroller.preprocessor;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Created by phil on 17.05.2015.
 */
public class Stemmer {

    // private static volatile Properties props;
    // private static volatile StanfordCoreNLP pipeline;

    protected StanfordCoreNLP pipeline;

    public Stemmer() {
        // Create StanfordCoreNLP object properties, with POS tagging(required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        props.put("tokenize.options", "untokenizable=noneDelete");
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        // StanfordCoreNLP loads a lot of models, so we only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
    }



    public static List<String> trivial_stem(String text) {
        List<String> terms = new LinkedList<String>();

        StringTokenizer stringTokenizer = new StringTokenizer(text);
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            if (isInvalidTerm(token)) {
                continue;
            }
            terms.add(token.toLowerCase());
        }

        return terms;
    }

    public static boolean isInvalidTerm(String term) {
        return term.length() <= 2 || term.startsWith("http") || term.startsWith("@");
    }

    public List<String> stem(String text) {


        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        // run all Annotators on this text
        this.pipeline.annotate(document);
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<String> wordList = new LinkedList<String>();
        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {




            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                if (isInvalidTerm(lemma)) {
                    continue;
                }

                // Retrieve and add the lemma for each word into the list of lemmas
                wordList.add(lemma);
            }
        }

        return wordList;
    }
}
