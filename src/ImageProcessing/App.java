package ImageProcessing;

import java.io.IOException;

public class App {
// Klasa za testiranje
    public static void main(String[] args) throws IOException{
        Engine eng = new Engine();
        String picture = "C:\\Users\\lukaj\\Desktop\\Misc\\Lenna.png";
        int[][] matrix = {{1,3,1},{3,9,3},{1,3,1}};
        eng.load(picture);
        eng.ContrastStretching(154,46,191,191);

        eng.save("C:\\Users\\lukaj\\Desktop\\Misc\\test.jpg");

    }

}


