import java.io.*;

/*
    ALLOWS THE OS TO READ / WRITE
*/
public class FileHandler {

    BufferedReader buffReader;
    BufferedWriter buffWriter;

    public void readFile(String fileName, String[] memory, int startingAddress){

        try{

            FileReader file = new FileReader("./programs/"+fileName);

            buffReader = new BufferedReader(file);

            String line;
            while( ( line = buffReader.readLine() )  != null ){

                memory[startingAddress++] = line;

            }

            buffReader.close();

        }catch (Exception e){

            System.out.println(e);
        }

    }

    public void writeFile(String fileName, String content){


        try{

            String directory = "./outputs/";
            String filePathName = fileName.split("\\.")[0] + "-output.txt";

            File file = new File(directory + filePathName);

            FileWriter fileWriter = new FileWriter( file );

            buffWriter = new BufferedWriter(fileWriter);

            buffWriter.write(content);

            buffWriter.close();

        }catch (Exception e){

            System.out.println(e);
        }

    }


}

