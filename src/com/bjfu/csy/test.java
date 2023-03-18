package com.bjfu.csy;


public class test {
    public static void main(String[] args) {
        String s = "\",\", \";\", \":\", \"(\", \")\", \"{\", \"}\", \"?\",\"#\",\"'\",\"\\\"\"";
        System.out.println(s.replace(",","|").replace(" ",""));
    }
}
