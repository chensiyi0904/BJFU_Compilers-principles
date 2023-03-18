package com.bjfu.csy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
//CLASS
public class Analyzer {
    // 关键词
    private ArrayList<String> keys;
    //运算符
    private ArrayList<String> ops;
    //符号
    private ArrayList<String> symbols;
    //维护标志
    private HashMap<String, Integer> values;
    // 读入指针
    private int p;
    //维护扫描指针
    private int preP;
    //当前扫描的字符串
    private String token;
    //是否在扫描注释
    private int isNoted;

    private String result;

    private Analyzer() {
        this.init();
    }

    private void init() {
        keys = new ArrayList<>(Arrays.asList("auto", "double,", "int", "struct", "break", "else", "long", "switch",
                "case", "enum", "register", "typedef", "char", "extern", "return", "union",
                "const", "float", "short", "unsigned", "continue", "for", "signed", "void",
                "default", "goto", "sizeof", "volatile", "do", "if", "while", "static"));
        ops = new ArrayList<>(Arrays.asList("+", "-", "*", "/", "%", "++", "--", "+=", "-=", "*=", "/=", "&", "|", "^", "~", "<<", ">>", ">>>", "<<<",
                "==", "!=", ">", "<", "=", ">=", "<=", "&&", "||", "!", "."));
        symbols = new ArrayList<>(Arrays.asList(",", ";", ":", "(", ")", "{", "}", "?", "#", "'", "\""));
        values = new HashMap<>();
        for (String key : keys) {
            values.put(key, 1);
        }
        for (String op : ops) {
            values.put(op, 4);
        }
        for (String symbol : symbols) {
            values.put(symbol, 5);
        }
        values.put("word", 2);
        values.put("number", 3);
        //初始设置位置为0
        this.p = 0;
        this.preP = 0;
        //设置是否是处在注释
        // 0 为无
        this.isNoted = 0;
        //设置对应为空
        this.token = "";
        this.result = "";
    }

    public void Analyzing(String input) {
        char ch;
        while (p < input.length()) {
            if (isNoted == 1) {
                reset();
                return;
            }
            if (isNoted == 2) {
                ch = input.charAt(p);
                if (!Character.isLetterOrDigit(ch)) {
                    symbolAnalyze(input);
                }
                if (isNoted == 2) {
                    p++;
                }
            } else {
                ch = input.charAt(p);
                if (Character.isDigit(ch)) {
                    digitAnalyze(input);
                } else if (Character.isLetter(ch) || ch == '_') {
                    letterAnalyze(input);
                } else if (ch == ' ') {
                    p++;
                } else {
                    symbolAnalyze(input);
                }
            }
        }
    }

    private void reset() {
        this.p = 0;
        this.isNoted = 0;
        this.preP = 0;
    }

    private void symbolAnalyze(String input) {
        char ch;
        for (; p < input.length(); p++) {
            ch = input.charAt(p);
            if (!Character.isLetterOrDigit(ch) && ch != ' ') {
                token += ch;
            } else {
                break;
            }
        }
        while (!token.equals("")) {
            if (isNoted == 0) {
                if (token.equals("//")) {
                    this.isNoted = 1;
                    this.token = "";
//                    System.out.println("//,开始注释");
                    break;
                }
                if (token.equals("/*")) {
                    this.isNoted = 2;
                    this.token = "";
//                    System.out.println("/*，开始注释");
                    break;
                }
                if (symbols.contains(token)) {
                    tokenOutAndReset();
                } else if (ops.contains(token)) {
                    tokenOutAndReset();
                } else {
                    token = token.substring(0, token.length() - 1);
                    p--;
                }
            } else {
                preP = Math.max(preP, p);
                if (token.equals("*/")) {
                    this.isNoted = 0;
                    this.preP = 0;
                    this.token = "";
//                    System.out.println("*/注释结束");
                    return;
                } else {
                    token = token.substring(0, token.length() - 1);
                    if (token.length() == 0) {
                        p = preP;
                        preP = 0;
                    } else {
                        p--;
                    }
                }
            }
        }
    }

    private void letterAnalyze(String input) {
        while (p < input.length()) {
            char ch = input.charAt(p);
            if (Character.isLetterOrDigit(ch)) {
                token += ch;
                p++;
            } else {
                tokenOutAndResetForWord();
                return;
            }
        }
        tokenOutAndResetForWord();
    }

    private void digitAnalyze(String input) {
        int state = 2;
        while (p < input.length()) {
            char ch = input.charAt(p);
            switch (state) {
                case 2: {
                    if (Character.isDigit(ch)) {
                        token += ch;
                        p++;
                    } else if (ch == '.') {
                        token += ch;
                        p++;
                        state = 4;
                    } else if (ch == ' ' || ops.contains(String.valueOf(ch)) || ch == '?' || ch == ';' || ch == ',' || ch == ':') {
                        state = 3;
                    } else {
                        tokenOutAndResetError();
                    }
                    break;
                }
                case 3: {
                    tokenOutAndResetForNum();
                    return;
                }
                case 4: {
                    if (Character.isDigit(ch)) {
                        token += ch;
                        p++;
                    } else if (ch == 'e' || ch == 'E') {
                        token += ch;
                        p++;
                        state = 5;
                    } else if (ch == ' ' || ops.contains(String.valueOf(ch)) || ch == '?' || ch == ';' || ch == ',' || ch == ':') {
                        state = 3;
                    } else {
                        tokenOutAndResetError();
                    }
                    break;
                }
                case 5: {
                    if (Character.isDigit(ch)) {
                        token += ch;
                        p++;
                        state = 7;
                    } else if (ch == '-') {
                        token += ch;
                        p++;
                        state = 6;
                    } else {
                        tokenOutAndResetError();
                    }
                    break;
                }
                case 6: {
                    if (Character.isDigit(ch)) {
                        token += ch;
                        p++;
                        state = 7;
                    } else {
                        tokenOutAndResetError();
                    }
                    break;
                }
                case 7: {
                    if (Character.isDigit(ch)) {
                        token += ch;
                        p++;
                    } else if (ch == ' ' || ops.contains(String.valueOf(ch)) || ch == '?' || ch == ';' || ch == ',' || ch == ':') {
                        state = 3;
                    }
                }
            }
        }
    }


    private void resetP() {
        this.p = 0;
        this.preP = 0;
    }

    private void tokenOutAndReset() {
        this.result += (values.get(token) + " , " + token + "\n");
        token = "";
    }

    private void tokenOutAndResetForNum() {
        this.result += (values.get("number") + " , " + token + "\n");
        token = "";
    }

    private void tokenOutAndResetForWord() {
        if (keys.contains(token)) {
            this.result += (values.get(token) + " , " + token + "\n");
        } else {
            this.result += (values.get("word") + " , " + token + "\n");
        }
        token = "";
    }

    private void tokenOutAndResetError() {
        this.result += "error\n";
        token = "";
    }

    public static void main(String[] args) throws FileNotFoundException {
        Analyzer analyzer = new Analyzer();
        File file = new File("resource/test.cc");
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                String str = input.nextLine().trim();
                analyzer.Analyzing(str);
                analyzer.resetP();
            }
        }
        File result = new File("resource/result.txt");
        PrintStream printStream = new PrintStream(result);
        printStream.println(analyzer.result);
        System.out.println(analyzer.result);
    }
}
