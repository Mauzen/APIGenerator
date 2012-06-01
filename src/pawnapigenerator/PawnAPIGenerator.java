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
    private static final String REGEX_NATIVE = "\\s*native\\s+((\\w+):)?(\\w+)\\s*[(]\\s*(.*(\\s*,\\s*(.+))*)[)]\\s*;";
    
    public static final String[] KEYWORDS = {"DB", "DBResult", "File", "Float", "Menu", "PlayerText3D", "Text",
        "Text3D", "_", "anglemode", "assert", "bool", "break", "case", "char", "const", "continue", "default", "defined",
        "do", "else", "enum", "exit", "false", "filemode", "floatround_method", "for", "forward", "goto", "if", "library",
        "native", "new", "operator", "public", "return", "seek_whence", "sizeof", "sleep", "state", "static", "stock",
        "switch", "tagof", "true", "while"};

    private ArrayList<String> headers;
    private ArrayList<FunctionHeader> head;
    
    private int totalFiles;
    private int validFiles;
    private int lines;
    
    private Pattern reg;
    private Pattern nat;
    
    private boolean verbose;
    
    public PawnAPIGenerator(File dir, Pattern m) {
        this.reg = m;
        this.nat = Pattern.compile(REGEX_NATIVE);
        headers = new ArrayList<>();
        head = new ArrayList<>();
        
        checkRecursive(dir);
        
        parseAll();
        
        System.out.println();
        System.out.println("Succesfully read " + validFiles + "/" + totalFiles + " files (" + lines + " lines)");
        System.out.println("Found " + headers.size() + " valid function headers");
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
            
            FunctionHeader gen;
            
            // Group regex matches
            Matcher m = reg.matcher(full);
            int count = 0;
            while (m.find()) {
                gen = HeaderParser.parseFromRegEx(m);
                if (gen != null && !headers.contains(gen.getName())) {
                    head.add(gen);
                    headers.add(gen.getName());
                }
                count++;
            }
            
            m = nat.matcher(full);
            while (m.find()) {
                gen = HeaderParser.parseFromNativeRegEx(m);
                if (gen != null && !headers.contains(gen.getName())) {
                    head.add(gen);
                    headers.add(gen.getName());
                }
                count++;                
            }
            if (verbose) System.out.println(f.getName() + ": " + count + " possible headers");
            
            
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
                    + "\t\t<Environment ignoreCase=\"no\" startFunc=\"(\" stopFunc=\")\" paramSeparator=\",\" terminal=\";\" />\r\n\t\t\r\n");

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
            for (FunctionHeader h : head) {
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
            
            // Dynamics
            for (FunctionHeader h : head) {
                if (h.getType() == FunctionType.KEYWORD) continue;
                fw.write(h.getName() + " ");
            }
            fw.write("</Keywords>\r\n");
        
            // The UGLY rest
            fw.write("\t\t\t<Keywords name=\"Words2\">SPECIAL_ACTION_NONE SPECIAL_ACTION_DUCK SPECIAL_ACTION_USEJETPACK SPECIAL_ACTION_ENTER_VEHICLE SPECIAL_ACTION_EXIT_VEHICLE SPECIAL_ACTION_DANCE1 SPECIAL_ACTION_DANCE2 SPECIAL_ACTION_DANCE3 SPECIAL_ACTION_DANCE4 SPECIAL_ACTION_HANDSUP SPECIAL_ACTION_USECELLPHONE SPECIAL_ACTION_SITTING SPECIAL_ACTION_STOPUSECELLPHONE SPECIAL_ACTION_DRINK_BEER SPECIAL_ACTION_SMOKE_CIGGY SPECIAL_ACTION_DRINK_WINE SPECIAL_ACTION_DRINK_SPRUNK FIGHT_STYLE_NORMAL FIGHT_STYLE_BOXING FIGHT_STYLE_KUNGFU FIGHT_STYLE_KNEEHEAD FIGHT_STYLE_GRABKICK FIGHT_STYLE_ELBOW WEAPONSKILL_PISTOL WEAPONSKILL_PISTOL_SILENCED WEAPONSKILL_DESERT_EAGLE WEAPONSKILL_SHOTGUN WEAPONSKILL_SAWNOFF_SHOTGUN WEAPONSKILL_SPAS12_SHOTGUN WEAPONSKILL_MICRO_UZI WEAPONSKILL_MP5 WEAPONSKILL_AK47 WEAPONSKILL_M4 WEAPONSKILL_SNIPERRIFLE WEAPONSTATE_UNKNOWN WEAPONSTATE_NO_BULLETS WEAPONSTATE_LAST_BULLET WEAPONSTATE_MORE_BULLETS WEAPONSTATE_RELOADING PLAYER_VARTYPE_NONE PLAYER_VARTYPE_INT PLAYER_VARTYPE_STRING PLAYER_VARTYPE_FLOAT MAX_CHATBUBBLE_LENGTH SPECTATE_MODE_NORMAL SPECTATE_MODE_FIXED SPECTATE_MODE_SIDE PLAYER_RECORDING_TYPE_NONE PLAYER_RECORDING_TYPE_DRIVER PLAYER_RECORDING_TYPE_ONFOOT _objects_included _samp_included PLAYER_RECORDING_TYPE_NONE PLAYER_RECORDING_TYPE_DRIVER PLAYER_RECORDING_TYPE_ONFOOT PLAYER_STATE_NONE PLAYER_STATE_ONFOOT PLAYER_STATE_DRIVER PLAYER_STATE_PASSENGER PLAYER_STATE_WASTED PLAYER_STATE_SPAWNED PLAYER_STATE_SPECTATING MAX_PLAYER_NAME MAX_PLAYERS MAX_VEHICLES INVALID_PLAYER_ID INVALID_VEHICLE_ID NO_TEAM MAX_OBJECTS INVALID_OBJECT_ID MAX_GANG_ZONES MAX_TEXT_DRAWS MAX_MENUS INVALID_MENU INVALID_TEXT_DRAW INVALID_GANG_ZONE WEAPON_BRASSKNUCKLE WEAPON_GOLFCLUB WEAPON_NITESTICK WEAPON_KNIFE WEAPON_BAT WEAPON_SHOVEL WEAPON_POOLSTICK WEAPON_KATANA WEAPON_CHAINSAW WEAPON_DILDO WEAPON_DILDO2 WEAPON_VIBRATOR WEAPON_VIBRATOR2 WEAPON_FLOWER WEAPON_CANE WEAPON_GRENADE WEAPON_TEARGAS WEAPON_MOLTOV WEAPON_COLT45 WEAPON_SILENCED WEAPON_DEAGLE WEAPON_SHOTGUN WEAPON_SAWEDOFF WEAPON_SHOTGSPA WEAPON_UZI WEAPON_MP5 WEAPON_AK47 WEAPON_M4 WEAPON_TEC9 WEAPON_RIFLE WEAPON_SNIPER WEAPON_ROCKETLAUNCHER WEAPON_HEATSEEKER WEAPON_FLAMETHROWER WEAPON_MINIGUN WEAPON_SATCHEL WEAPON_BOMB WEAPON_SPRAYCAN WEAPON_FIREEXTINGUISHER WEAPON_CAMERA WEAPON_PARACHUTE WEAPON_VEHICLE WEAPON_DROWN WEAPON_COLLISION KEY_ACTION KEY_CROUCH KEY_FIRE KEY_SPRINT KEY_SECONDARY_ATTACK KEY_JUMP KEY_LOOK_RIGHT KEY_HANDBRAKE KEY_LOOK_LEFT KEY_SUBMISSION KEY_LOOK_BEHIND KEY_WALK KEY_ANALOG_UP KEY_ANALOG_DOWN KEY_ANALOG_RIGHT KEY_ANALOG_LEFT KEY_UP KEY_DOWN KEY_LEFT KEY_RIGHT HTTP_GET HTTP_POST HTTP_HEAD HTTP_ERROR_BAD_HOST HTTP_ERROR_NO_SOCKET HTTP_ERROR_CANT_CONNECT HTTP_ERROR_CANT_WRITE HTTP_ERROR_CONTENT_TOO_BIG HTTP_ERROR_MALFORMED_RESPONSE _time_included _string_included _Float_included _file_included _datagram_included _core_included _vehicles_included CARMODTYPE_SPOILER CARMODTYPE_HOOD CARMODTYPE_ROOF CARMODTYPE_SIDESKIRT CARMODTYPE_LAMPS CARMODTYPE_NITRO CARMODTYPE_EXHAUST CARMODTYPE_WHEELS CARMODTYPE_STEREO CARMODTYPE_HYDRAULICS CARMODTYPE_FRONT_BUMPER CARMODTYPE_REAR_BUMPER CARMODTYPE_VENT_RIGHT CARMODTYPE_VENT_LEFT _sampdb_included _samp_included MAX_PLAYER_NAME MAX_PLAYERS MAX_VEHICLES INVALID_PLAYER_ID INVALID_VEHICLE_ID NO_TEAM MAX_OBJECTS INVALID_OBJECT_ID MAX_GANG_ZONES MAX_TEXT_DRAWS MAX_MENUS MAX_3DTEXT_GLOBAL MAX_3DTEXT_PLAYER MAX_PICKUPS INVALID_MENU INVALID_TEXT_DRAW INVALID_GANG_ZONE INVALID_3DTEXT_ID DIALOG_STYLE_MSGBOX DIALOG_STYLE_INPUT DIALOG_STYLE_LIST PLAYER_STATE_NONE PLAYER_STATE_ONFOOT PLAYER_STATE_DRIVER PLAYER_STATE_PASSENGER PLAYER_STATE_EXIT_VEHICLE PLAYER_STATE_ENTER_VEHICLE_DRIVER PLAYER_STATE_ENTER_VEHICLE_PASSENGER PLAYER_STATE_WASTED PLAYER_STATE_SPAWNED PLAYER_STATE_SPECTATING PLAYER_MARKERS_MODE_OFF PLAYER_MARKERS_MODE_GLOBAL PLAYER_MARKERS_MODE_STREAMED WEAPON_BRASSKNUCKLE WEAPON_GOLFCLUB WEAPON_NITESTICK WEAPON_KNIFE WEAPON_BAT WEAPON_SHOVEL WEAPON_POOLSTICK WEAPON_KATANA WEAPON_CHAINSAW WEAPON_DILDO WEAPON_DILDO2 WEAPON_VIBRATOR WEAPON_VIBRATOR2 WEAPON_FLOWER WEAPON_CANE WEAPON_GRENADE WEAPON_TEARGAS WEAPON_MOLTOV WEAPON_COLT45 WEAPON_SILENCED WEAPON_DEAGLE WEAPON_SHOTGUN WEAPON_SAWEDOFF WEAPON_SHOTGSPA WEAPON_UZI WEAPON_MP5 WEAPON_AK47 WEAPON_M4 WEAPON_TEC9 WEAPON_RIFLE WEAPON_SNIPER WEAPON_ROCKETLAUNCHER WEAPON_HEATSEEKER WEAPON_FLAMETHROWER WEAPON_MINIGUN WEAPON_SATCHEL WEAPON_BOMB WEAPON_SPRAYCAN WEAPON_FIREEXTINGUISHER WEAPON_CAMERA WEAPON_PARACHUTE WEAPON_VEHICLE WEAPON_DROWN WEAPON_COLLISION KEY_ACTION KEY_CROUCH KEY_FIRE KEY_SPRINT KEY_SECONDARY_ATTACK KEY_JUMP KEY_LOOK_RIGHT KEY_HANDBRAKE KEY_LOOK_LEFT KEY_SUBMISSION KEY_LOOK_BEHIND KEY_WALK KEY_ANALOG_UP KEY_ANALOG_DOWN KEY_ANALOG_LEFT KEY_ANALOG_RIGHT KEY_UP KEY_DOWN KEY_LEFT KEY_RIGHT CLICK_SOURCE_SCOREBOARD floatround_round floatround_floor floatround_ceil floatround_tozero floatround_unbiased seek_start seek_current seek_end EOS cellbits cellmax cellmin charbits charmin charmax ucharmax __Pawn debug overlaysize radian degrees grades MAX_PLAYER_ATTACHED_OBJECTS VEHICLE_PARAMS_UNSET VEHICLE_PARAMS_OFF VEHICLE_PARAMS_ON</Keywords>\r\n"
                    + "\t\t\t<Keywords name=\"Words3\">#assert #define #else #elseif #endif #endinput #error #file #if #include #line #pragma #tryinclude #undef #emit</Keywords>\r\n"
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
    
}
