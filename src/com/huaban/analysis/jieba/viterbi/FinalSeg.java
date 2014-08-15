package com.huaban.analysis.jieba.viterbi;

import com.huaban.analysis.jieba.CharacterUtil;
import com.huaban.analysis.jieba.Pair;
import com.huaban.analysis.jieba.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

public class FinalSeg {
    private static FinalSeg singleInstance;
    private static final String PROB_EMIT = "/clj-jb-dict/prob_emit.txt";
    private static char[] states = new char[] {'B', 'M', 'E', 'S'};
    private static Map<Character, Map<Character, Double>> emit;
    private static Map<Character, Double> start;
    private static Map<Character, Map<Character, Double>> trans;
    private static Map<Character, char[]> prevStatus;
    private static Double MIN_FLOAT = -3.14e100;;

    private FinalSeg() {}

    public synchronized static FinalSeg getInstance() {
        if (null == singleInstance) {
            singleInstance = new FinalSeg();
            singleInstance.loadModel();
        }
        return singleInstance;
    }

    private void loadModel() {
        long s = System.currentTimeMillis();
        prevStatus = new HashMap<Character, char[]>();
        prevStatus.put('B', new char[] {'E', 'S'});
        prevStatus.put('M', new char[] {'M', 'B'});
        prevStatus.put('S', new char[] {'S', 'E'});
        prevStatus.put('E', new char[] {'B', 'M'});

        start = new HashMap<Character, Double>();
        start.put('B', -0.26268660809250016);
        start.put('E', -3.14e+100);
        start.put('M', -3.14e+100);
        start.put('S', -1.4652633398537678);

        trans = new HashMap<Character, Map<Character, Double>>();
        Map<Character, Double> transB = new HashMap<Character, Double>();
        transB.put('E', -0.510825623765990);
        transB.put('M', -0.916290731874155);
        trans.put('B', transB);
        Map<Character, Double> transE = new HashMap<Character, Double>();
        transE.put('B', -0.5897149736854513);
        transE.put('S', -0.8085250474669937);
        trans.put('E', transE);
        Map<Character, Double> transM = new HashMap<Character, Double>();
        transM.put('E', -0.33344856811948514);
        transM.put('M', -1.2603623820268226);
        trans.put('M', transM);
        Map<Character, Double> transS = new HashMap<Character, Double>();
        transS.put('B', -0.7211965654669841);
        transS.put('S', -0.6658631448798212);
        trans.put('S', transS);

        InputStream is = this.getClass().getResourceAsStream(PROB_EMIT);
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            emit = new HashMap<Character, Map<Character, Double>>();
            Map<Character, Double> values = null;
            while (br.ready()) {
                String line = br.readLine();
                String[] tokens = line.split("\t");
                if (tokens.length == 1) {
                    values = new HashMap<Character, Double>();
                    emit.put(tokens[0].charAt(0), values);
                } else {
                    values.put(tokens[0].charAt(0), Double.valueOf(tokens[1]));
                }
            }
        } catch (IOException e) {
            System.err.println(String.format("%s: load model failure!", PROB_EMIT));
        } finally {
            try {
                if (null != is) is.close();
            } catch (IOException e) {
                System.err.println(String.format("%s: close failure!", PROB_EMIT));
            }
        }
        System.out.println(String.format("model load finished, time elapsed %d ms.",
                System.currentTimeMillis() - s));
    }

    public void cut(String sentence, List<Word> tokens) {
        StringBuilder chinese = new StringBuilder();
        StringBuilder other = new StringBuilder();
        for (int i = 0; i < sentence.length(); ++i) {
            char ch = sentence.charAt(i);
            if (CharacterUtil.isChineseLetter(ch)) {
                if (other.length() > 0) {
                    processOtherUnknownWords(other.toString(), tokens);
                    other = new StringBuilder();
                }
                chinese.append(ch);
            } else {
                if (chinese.length() > 0) {
                    viterbi(chinese.toString(), tokens);
                    chinese = new StringBuilder();
                }
                other.append(ch);
            }

        }
        if (chinese.length() > 0)
            viterbi(chinese.toString(), tokens);
        else {
            processOtherUnknownWords(other.toString(), tokens);
        }
    }


    public void viterbi(String sentence, List<Word> tokens) {
        Vector<Map<Character, Double>> v = new Vector<Map<Character, Double>>();
        Map<Character, Vector<Character>> path = new HashMap<Character, Vector<Character>>();

        v.add(new HashMap<Character, Double>());
        for (char state : states) {
            Double emP = emit.get(state).get(sentence.charAt(0));
            if (null == emP) emP = MIN_FLOAT;
            v.get(0).put(state, start.get(state) + emP);
            path.put(state, new Vector<Character>());
            path.get(state).add(state);
        }

        for (int i = 1; i < sentence.length(); ++i) {
            Map<Character, Double> vv = new HashMap<Character, Double>();
            v.add(vv);
            Map<Character, Vector<Character>> newPath = new HashMap<Character, Vector<Character>>();
            for (char y : states) {
                Double emp = emit.get(y).get(sentence.charAt(i));
                if (emp == null) emp = MIN_FLOAT;
                Pair<Character> candidate = null;
                for (char y0 : prevStatus.get(y)) {
                    Double tranp = trans.get(y0).get(y);
                    if (null == tranp) tranp = MIN_FLOAT;
                    tranp += (emp + v.get(i - 1).get(y0));
                    if (null == candidate)
                        candidate = new Pair<Character>(y0, tranp);
                    else if (candidate.freq <= tranp) {
                        candidate.freq = tranp;
                        candidate.key = y0;
                    }
                }
                vv.put(y, candidate.freq);
                Vector<Character> newPathValue = new Vector<Character>();
                newPathValue.addAll(path.get(candidate.key));
                newPathValue.add(y);
                newPath.put(y, newPathValue);
            }
            path = newPath;
        }
        double probE = v.get(sentence.length() - 1).get('E');
        double probS = v.get(sentence.length() - 1).get('S');
        Vector<Character> posList;
        if (probE < probS)
            posList = path.get('S');
        else
            posList = path.get('E');

        int begin = 0, next = 0;
        for (int i = 0; i < sentence.length(); ++i) {
            char pos = posList.get(i);
            if (pos == 'B')
                begin = i;
            else if (pos == 'E') {
                tokens.add(Word.createWord(sentence.substring(begin, i + 1)));
                next = i + 1;
            } else if (pos == 'S') {
                tokens.add(Word.createWord(sentence.substring(i, i + 1)));
                next = i + 1;
            }
        }
        if (next < sentence.length()) tokens.add(Word.createWord(sentence.substring(next)));
    }

    private void processOtherUnknownWords(String other, List<Word> tokens) {
        Matcher mat = CharacterUtil.reSkip.matcher(other);
        int offset = 0;
        while (mat.find()) {
            if (mat.start() > offset) {
                tokens.add(Word.createWord(other.substring(offset, mat.start())));
            }
            tokens.add(Word.createWord(mat.group()));
            offset = mat.end();
        }
        if (offset < other.length()) tokens.add(Word.createWord(other.substring(offset)));
    }
}
