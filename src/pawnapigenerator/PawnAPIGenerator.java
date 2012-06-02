package pawnapigenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Michel "Mauzen" Soll
 */
public class PawnAPIGenerator {
    
    private static final String REGEX_ALL = "((stock|public)\\s+)?((\\w+):)?(\\w+)\\s*[(]\\s*([^\\s]*(\\s*,\\s*[^\\s]+)*)[)]\\s*[{]";
    private static final String REGEX_NATIVE = "native\\s+((\\w+):)?(\\w+)\\s*[(]\\s*(.*(\\s*,\\s*(.+))*)[)]\\s*;";
    private static final String REGEX_DEFINE = "#define\\s+((\\w+)([(](%\\d+)?(,%\\d+)*[)])?)\\s*([^\\s]*)";
    
    public static final String[] KEYWORDS = {"DB", "DBResult", "File", "Float", "Menu", "PlayerText3D", "Text",
        "Text3D", "_", "anglemode", "assert", "bool", "break", "case", "char", "const", "continue", "default", "defined",
        "do", "else", "enum", "exit", "false", "filemode", "floatround_method", "for", "forward", "goto", "if", "library",
        "native", "new", "operator", "public", "return", "seek_whence", "sizeof", "sleep", "state", "static", "stock",
        "switch", "tagof", "true", "while"};

    private ArrayList<String> headers;
    private ArrayList<PawnStatement> head;
    
    private int totalFiles;
    private int validFiles;
    private int lines;
    
    private Pattern reg;
    private Pattern nat;
    private Pattern def;
    
    private boolean verbose;
    
    public PawnAPIGenerator(File dir, Pattern m) {
        this.reg = m;
        this.nat = Pattern.compile(REGEX_NATIVE);
        this.def = Pattern.compile(REGEX_DEFINE);
        headers = new ArrayList<>();
        head = new ArrayList<>();
        
        checkRecursive(dir);
        
        parseAll();
        
        System.out.println();
        System.out.println("Succesfully read " + validFiles + "/" + totalFiles + " files (" + lines + " lines)");
        System.out.println("Found " + headers.size() + " valid statements:");
        System.out.println("\t" + countStatements(StatementType.NATIVE) + " natives, " + countStatements(StatementType.PUBLIC) + " publics, "
                + countStatements(StatementType.STOCK) + " stocks, " + countStatements(StatementType.NONE) + " other functions");
        System.out.println("\t" + countStatements(StatementType.DEFINE) + " defines");
        
    }    
    
    
    private void checkRecursive(File dir) {
        File[] all = dir.listFiles();
        
        for ( File b : all) {
            if (b.isDirectory()) {
                checkRecursive(b);
                continue;
            }
            if (b.getName().endsWith(".inc")) analyzeFile(b);
        }
    }
    
    
    private void analyzeFile(File f) {
        // Filecounter
        totalFiles++;
        
        BufferedReader fi = null;
        try {
            fi = new BufferedReader(new FileReader(f));
            
            // Read the whole file to buffer
            String full = "";
            while (fi.ready()) {
                full += fi.readLine() + "\r\n";
                lines++;
            }            
            fi.close();
            
            PawnStatement gen;
            
            // Group regex matches
            Matcher m = reg.matcher(full);
            int count = 0;
            while (m.find()) {
                gen = HeaderParser.parseFromRegEx(m);
                if (gen != null && !headers.contains(gen.getIndexName())) {
                    head.add(gen);
                    headers.add(gen.getIndexName());
                }
                count++;
            }
            
            m = nat.matcher(full);
            while (m.find()) {
                gen = HeaderParser.parseFromNativeRegEx(m);
                if (gen != null && !headers.contains(gen.getIndexName())) {
                    head.add(gen);
                    headers.add(gen.getIndexName());
                }
                count++;                
            }            
            
            m = def.matcher(full);
            while (m.find()) {
                gen = HeaderParser.parseDefineRegEx(m);
                // Only add non-parameter defines
                if (gen != null && !headers.contains(gen.getIndexName())) {
                    head.add(gen);
                    headers.add(gen.getIndexName());
                }
                count++;
                //count++;                
            }
            
            if (verbose) System.out.println(f.getName() + ": " + count + " statements");
            
            
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: Couldn't read from file: " + f.toString());
            return;
        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't read from file: " + f.toString());
            return;
        } 
        
        // Successcounter
        validFiles++;        
        
    }
    
    private void parseAll() {
        // Add keywords and fix phrases  
        for (String k : KEYWORDS) {
            head.add(new FunctionHeader(k));
        }
        
        java.util.Collections.sort(head);
    }
    
    /**
     * Assembles userDefinedLanguage.xml and PAWN.xml from the collected data.
     * @param path1
     * @param path2 
     */
    public void toFile(String path1, String path2) {
        File f = new File(path1);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                System.out.println("ERROR: Couldn't create output file 1!");
                return;
            }
        }
        try {
            FileWriter fw = new FileWriter(f);

            // Info header
            //fw.write("<!-- This file was automatically generated by Mauzen's PAWN API Generator -->\r\n");
            // XML start
            fw.write("<?xml version=\"1.0\" encoding=\"Windows-1252\" ?>\r\n"
                    + "<NotepadPlus>\r\n"
                    + "\t<AutoComplete language=\"PAWN\">\r\n"
                    + "\t\t<Environment ignoreCase=\"yes\" startFunc=\"(\" stopFunc=\")\" paramSeparator=\",\" terminal=\";\" />\r\n\t\t\r\n");

            // Fix keywords
            fw.write("\t\t<KeyWord name=\"#assert\" />\r\n"
                    + "\t\t<KeyWord name=\"#define\" />\r\n"
                    + "\t\t<KeyWord name=\"#else\" />\r\n"
                    + "\t\t<KeyWord name=\"#elseif\" />\r\n"
                    + "\t\t<KeyWord name=\"#emit\" />\r\n"
                    + "\t\t<KeyWord name=\"#endif\" />\r\n"
                    + "\t\t<KeyWord name=\"#endinput\" />\r\n"
                    + "\t\t<KeyWord name=\"#error\" />\r\n"
                    + "\t\t<KeyWord name=\"#file\" />\r\n"
                    + "\t\t<KeyWord name=\"#if\" />\r\n"
                    + "\t\t<KeyWord name=\"#include\" />\r\n"
                    + "\t\t<KeyWord name=\"#line\" />\r\n"
                    + "\t\t<KeyWord name=\"#pragma\" />\r\n"
                    + "\t\t<KeyWord name=\"#tryinclude\" />\r\n"
                    + "\t\t<KeyWord name=\"#undef\" />\r\n\t\t\r\n");

            // Custom functions
            for (PawnStatement h : head) {
                fw.write(h.toXML());
            }

            // XML end
            fw.write("\t</AutoComplete>\r\n"
                    + "</NotepadPlus>");

            fw.close();

        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't write to output file 1!");
            return;
        }
        
        f = new File(path2);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                System.out.println("ERROR: Couldn't create output file 2!");
                return;
            }
        }
        try {
            FileWriter fw = new FileWriter(f);

            // Info header
            //fw.write("<!-- This file was automatically generated by Mauzen's PAWN API Generator -->\r\n");
            // XML start
            fw.write("<NotepadPlus>\r\n"
                    + "\t<UserLang name=\"PAWN\" ext=\"pwn inc own\">\r\n"
                    + "\t\t<Settings>\r\n"
                    + "\t\t\t<Global caseIgnored=\"no\" escapeChar=\"\\\" />\r\n"
                    + "\t\t\t<TreatAsSymbol comment=\"yes\" commentLine=\"yes\" />\r\n"
                    + "\t\t\t<Prefix words1=\"no\" words2=\"no\" words3=\"no\" words4=\"no\" />\r\n"
                    + "\t\t</Settings>\r\n");
            
            fw.write("\t\t<KeywordLists>\r\n"
                    + "\t\t\t<Keywords name=\"Delimiters\">&quot;&apos;0&quot;&apos;0</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Folder+\">{</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Folder-\">}</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Operators\">&apos; - ! &quot; % &amp; ( ) , : ; ? [ ] ^ { | } ~ + &lt; = &gt;</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Comment\">1/* 2*/ 0//</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Words1\">");
            
            // Functions
            for (PawnStatement h : head) {
                if (!(h.getType() == StatementType.NATIVE || h.getType() == StatementType.NONE || h.getType() == StatementType.STOCK
                        || h.getType() == StatementType.PUBLIC)) continue;
                fw.write(h.getIndexName() + " ");
            }
            fw.write("</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Words2\">");
            // Defines
            for (PawnStatement h : head) {
                if (!(h.getType() == StatementType.DEFINE)) continue;
                fw.write(h.getIndexName() + " ");
            }
            fw.write("</Keywords>\r\n");
        
            // The UGLY rest            
            fw.write("\t\t\t<Keywords name=\"Words3\">#assert #define #else #elseif #endif #endinput #error #file #if #include #line #pragma #tryinclude #undef #emit</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Words4\">DB DBResult File Float Menu PlayerText3D Text Text3D _ anglemode assert bool break case char const continue default defined do else enum exit false filemode floatround_method for forward goto if library native new operator public return seek_whence sizeof sleep state static stock switch tagof true while</Keywords>\r\n"
                    + "\t\t</KeywordLists>\r\n"
                    + "\t\t<Styles>\r\n"
                    + "\t\t\t<WordsStyle name=\"DEFAULT\" styleID=\"11\" fgColor=\"000000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"FOLDEROPEN\" styleID=\"12\" fgColor=\"000000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"FOLDERCLOSE\" styleID=\"13\" fgColor=\"000000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"KEYWORD1\" styleID=\"5\" fgColor=\"000080\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"KEYWORD2\" styleID=\"6\" fgColor=\"800000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"KEYWORD3\" styleID=\"7\" fgColor=\"800000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"1\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"KEYWORD4\" styleID=\"8\" fgColor=\"0000C0\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"1\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"COMMENT\" styleID=\"1\" fgColor=\"008000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"COMMENT LINE\" styleID=\"2\" fgColor=\"008000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"NUMBER\" styleID=\"4\" fgColor=\"FF8000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"OPERATOR\" styleID=\"10\" fgColor=\"000000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"DELIMINER1\" styleID=\"14\" fgColor=\"808080\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"DELIMINER2\" styleID=\"15\" fgColor=\"808080\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t\t<WordsStyle name=\"DELIMINER3\" styleID=\"16\" fgColor=\"000000\" bgColor=\"FFFFFF\" fontName=\"\" fontStyle=\"0\" />\r\n"
                    + "\t\t</Styles>\r\n"
                    + "\t</UserLang>\r\n"
                    + "</NotepadPlus>");

            fw.close();

        } catch (IOException ex) {
            System.out.println("ERROR: Couldn't write to output file 2!");
            return;
        }            
            
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
                
        if (args.length == 0) {
            String path = JOptionPane.showInputDialog("Specify the full path to your pawn source directory:");
            PawnAPIGenerator pag = new PawnAPIGenerator(new File(path), Pattern.compile(REGEX_ALL));
            pag.toFile("PAWN.xml", "userDefineLang.xml");
            return;
        }
        
        PawnAPIGenerator pag = new PawnAPIGenerator(new File(args[0]), Pattern.compile(REGEX_ALL));
        
        switch (args.length) {
            case 1:
            {
                pag.toFile("PAWN.xml", "userDefineLang.xml");
                break;
            }
            case 2:
            {
                pag.toFile(args[1], "userDefineLang.xml");
                break;
            }
            case 3:
            {
                pag.toFile(args[1], args[2]);
            }
        }        
    }
    
    public int countStatements(StatementType t) {
        int count = 0;
        for (PawnStatement p : head) {
            if (p.getType() == t) count++;
        }
        return count;
    }
    
}
