package corvidina.fire_bearer;

import org.json.JSONObject;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;

public class Application {
    public static void main(String[] args){
        try {
            File file = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().getFile());

            String directory = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(file.getName()));

            JFrame frame = new JFrame("Choose JSON File to Execute");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

            // Set the current directory
            fileChooser.setCurrentDirectory(new File(directory));

            // Set file selection mode (files only, directories only, or both)
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter textFilter = new FileNameExtensionFilter("Json Files", "json");
            fileChooser.setFileFilter(textFilter);

            // Show the dialog and get the result
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                //System.out.println("Selected file: " + selectedFile.getAbsolutePath());
                Application a = new Application(selectedFile);
                Display display = new Display(60,a);
                display.run();
            }
            frame.dispose();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " application");
        }
    }
    public static void write(){
        JSONObject json = new JSONObject();
        json.put("num_air_jumps",2);
        File file = new File("C:\\Users\\spide\\Desktop\\metroidvania\\s.json");
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json.toString());
            fileWriter.close();
            System.out.println(json.toString());

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    private String characterImage;
    private String platformImage;
    private String backgroundImage;
    private String killAreaImage;
    private float gravity_multiplier;
    private float speed_multiplier;
    private float jump_strength_multiplier;
    private int num_air_jumps;
    public Application(File file){
        JSONObject json = null;
        String fileDirectory = "";
        if(file!=null) {
            try {
                StringBuilder contents = new StringBuilder();
                Scanner s = new Scanner(file);
                while(s.hasNextLine()){
                    contents.append(s.nextLine());
                }
                String c = contents.toString();
                json = new JSONObject(c);

                fileDirectory = file.getAbsolutePath().substring(0, file.getAbsolutePath().indexOf(file.getName()));
            } catch (Exception e){
                System.out.println(e.getMessage());
                file=null;
                json=null;
            }
        }
        try {
            characterImage=fileDirectory+json.getString("character_image");
            //System.out.println(characterImage);
        } catch (Exception ignored) {
            characterImage=null;
        }
        try {
            platformImage=fileDirectory+json.getString("platform_image");
        } catch (Exception ignored) {
            platformImage=null;
        }
        try {
            backgroundImage=fileDirectory+json.getString("background_image");
        } catch (Exception ignored) {
            backgroundImage=null;
        }
        try {
            killAreaImage= fileDirectory+json.getString("kill_area_image");
        } catch (Exception ignored){
            killAreaImage=null;
        }
        try {
            gravity_multiplier = json.getFloat("gravity_multiplier");
        } catch (Exception ignored){
            gravity_multiplier = 1;
        }
        try {
            speed_multiplier=json.getFloat("speed_multiplier");
        } catch (Exception ignored){
            speed_multiplier=1;
        }
        try {
            num_air_jumps=json.getInt("num_air_jumps");
        } catch (Exception ignored){
            num_air_jumps=1;
        }
        try {
            jump_strength_multiplier=json.getInt("jump_strength_multiplier");
        } catch (Exception ignored){
            jump_strength_multiplier=1;
        }
    }
    public String getCharacterImage(){
        return characterImage;
    }
    public String getPlatformImage(){
        return platformImage;
    }
    public String getBackgroundImage(){
        return backgroundImage;
    }
    public String getKillAreaImage(){
        return killAreaImage;
    }
    public int getNumAirJumps(){
        return num_air_jumps;
    }
    public float getGravityMultiplier(){
        return gravity_multiplier;
    }
    public float getSpeedMultiplier(){
        return speed_multiplier;
    }
    public float getJumpStrengthMultiplier(){
        return jump_strength_multiplier;
    }
}
