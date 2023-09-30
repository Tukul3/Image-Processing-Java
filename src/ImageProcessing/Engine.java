package ImageProcessing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class Engine {

    private int[][][] img;
    private int height;
    private int width;
    private int[][][] copy;


    public void load(String path) throws IOException {
        File file = new File(path);
        BufferedImage rawImg = ImageIO.read(file);
        height = rawImg.getHeight();
        width = rawImg.getWidth();
        img = new int[width][height][3];


        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int pixel = rawImg.getRGB(x,y);
                Color color = new Color(pixel,true);

                img[x][y][0] = color.getRed();
                img[x][y][1] = color.getGreen();
                img[x][y][2] = color.getBlue();
            }
        }
        getCopy();
    }

    public void save(String name) throws IOException{
        int height = img[0].length;
        int width = img.length;
        BufferedImage fileImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int r = img[x][y][0];
                int g = img[x][y][1];
                int b = img[x][y][2];

                //System.out.println(r + "," + g + "," + b);
                Color color = new Color(r,g,b);
                fileImg.setRGB(x,y,color.getRGB());
            }
        }
        File file = new File(name);
        ImageIO.write(fileImg,"jpg",file);
    }

    public BufferedImage getBuffImg(){
        int height = img[0].length;
        int width = img.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int r = img[x][y][0];
                int g = img[x][y][1];
                int b = img[x][y][2];

                //System.out.println(r + "," + g + "," + b);
                Color color = new Color(r,g,b);
                image.setRGB(x,y,color.getRGB());
            }
        }
        return image;
    }

    public void getCopy(){
        copy = new int[width][height][3];
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                copy[j][i][0] = img[j][i][0];
                copy[j][i][1] = img[j][i][1];
                copy[j][i][2] = img[j][i][2];
            }
        }

    }

    public void Inversion(){
        int height = img[0].length;
        int width = img.length;
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                img[x][y][0] = Math.abs(img[x][y][0]-255);
                img[x][y][1] = Math.abs(img[x][y][1]-255);
                img[x][y][2] = Math.abs(img[x][y][2]-255);
            }
        }
    }

    public void GammaCorrection(double gamma){
        for (int y=0; y < height; y++){
            for (int x=0; x < width; x++){
                img[x][y][0] = (int)(Math.pow(copy[x][y][0]/255f,1f/gamma) * 255f);
                img[x][y][1] = (int)(Math.pow(copy[x][y][1]/255f,1f/gamma) * 255f);
                img[x][y][2] = (int)(Math.pow(copy[x][y][2]/255f,1f/gamma) * 255f);
            }
        }
    }


    public void Grayscale(){
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int avg = (img[x][y][0] + img[x][y][1] + img[x][y][2])/3;
                img[x][y][0] = avg;
                img[x][y][1] = avg;
                img[x][y][2] = avg;
            }
        }
    }

    public void GaussBlur(){
        int [][] blurKernel = {{1,3,1},{3,9,3},{1,3,1}};
        img = convolution(img,blurKernel);
        height -= 2;
        width -= 2;
    }

    public void applyDilation(){
        BlackWhiteConversion();
        img = Dilation();
    }

    public void applyErosion(){
        BlackWhiteConversion();
        img = Erosion();
    }

    public void EdgeDetection(){
        Grayscale();
        GaussBlur();
        int [][] kernelx = {{-1,0,1},{-2,0,2},{-1,0,1}};
        int [][] kernely = {{-1,-2,-1},{0,0,0},{1,2,1}};
        int [][][] gx = convolution(img,kernelx);
        int [][][] gy = convolution(img,kernely);
        int [][][] t =combine(gx,gy);
        int[][][] trimmedimg = trim(t);
        height -= 2;
        width -= 2;
        int[] allPix = OneDimArr(trimmedimg);
        int[] vals = minmax(allPix);
        img = Normalisation(vals[1],vals[0],trimmedimg);
        //img = checkThreshold(img,165);
    }

    public void ContrastStretching(float r1,float s1,float r2, float s2){
        int height = img[0].length;
        int width = img.length;
        for(int i=0;i < width;i++){
            for(int j=0;j < height;j++){
                for(int k=0;k<3;k++){
                    if(copy[i][j][k] < r1){
                        img[i][j][k] = (int)((s1/r1)*copy[i][j][k]);
                    }
                    else if((r1 <= copy[i][j][k]) && (r2 >= copy[i][j][k])){
                        img[i][j][k] = (int)(((s2-s1)/(r2-r1))*(copy[i][j][k] - r1) + s1);
                    }
                    else{
                        img[i][j][k] = (int)(((255-s2)/(255-r2))*(copy[i][j][k] - r2) + s2);
                    }
                }
            }
        }
    }

    public int[][][] Dilation(){
        int[][][] copy = new int[img.length][img[0].length][3];
        for (int y = 0; y < img[0].length; y++) {
            for (int x = 0; x < img.length; x++) {
                int list[][][] = new int[3][3][3];
                boolean flag = false;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        int ny = y + k;
                        int nx = x + l;
                        if (nx < 0 || ny < 0 || nx >= img.length || ny >= img[0].length) {
                            flag = true;
                            continue;
                        }
                        list[k + 1][l + 1] = img[nx][ny];
                    }
                    if (flag) {
                        continue;
                    }

                }
                if (!flag) {
                    int[] vals = OneDimArr(list);
                    int[] max = minmax(vals);
                    copy[x][y][0] = max[0];
                    copy[x][y][1] = max[0];
                    copy[x][y][2] = max[0];
                }

            }
        }
        return copy;
    }

    public int[][][] Erosion(){
        int[][][] copy = new int[img.length][img[0].length][3];
        for (int y = 0; y < img[0].length; y++) {
            for (int x = 0; x < img.length; x++) {
                int list[][][] = new int[3][3][3];
                boolean flag = false;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        int ny = y + k;
                        int nx = x + l;
                        if (nx < 0 || ny < 0 || nx >= img.length || ny >= img[0].length) {
                            flag = true;
                            continue;
                        }
                        list[k + 1][l + 1] = img[nx][ny];
                    }
                    if (flag) {
                        continue;
                    }

                }
                if (!flag) {
                    int[] vals = OneDimArr(list);
                    int[] max = minmax(vals);
                    copy[x][y][0] = max[1];
                    copy[x][y][1] = max[1];
                    copy[x][y][2] = max[1];
                }

            }
        }
        return copy;
    }

    public int[][][] convolution(int[][][] img, int [][] kernel) {
        int[][][] copyImg = new int[img.length][img[0].length][3];
        for (int y = 0; y < img[0].length; y++) {
            for (int x = 0; x < img.length; x++) {
                int list[][][] = new int[3][3][3];
                boolean flag = false;
                for (int k = -1; k < 2; k++) {
                    for (int l = -1; l < 2; l++) {
                        int ny = y + k;
                        int nx = x + l;
                        if (nx < 0 || ny < 0 || nx >= img.length || ny >= img[0].length) {
                            flag = true;
                            continue;
                        }
                        list[k + 1][l + 1] = img[nx][ny];
                    }
                    if (flag) {
                        continue;
                    }

                }
                if (!flag) {
                    int[] sum = applyKernel(list,kernel);

                    copyImg[x][y][0] = sum[0];
                    copyImg[x][y][1] = sum[1];
                    copyImg[x][y][2] = sum[2];
                }
            }
        }
        int[][][] trimmedimg = trim(copyImg);
        int[] allPix = OneDimArr(trimmedimg);
        int[] vals = minmax(allPix);
        return Normalisation(vals[1],vals[0], trimmedimg);
    }



    public int[] applyKernel(int[][][] list, int [][] matrix) {
        int[] sum = {0,0,0};


        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3;j++){
                int red =matrix[i][j] * list[i][j][0];
                int green = matrix[i][j] * list[i][j][1];
                int blue = matrix[i][j] * list[i][j][2];

                sum[0]+=red;
                sum[1]+=green;
                sum[2]+=blue;
            }
        }
        return sum;
    }



    public int[] minmax(int[] list) {
        int max = list[0];
        int min = list[0];
        int[] res = new int[2];

        for (int i = 0; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
            }
            if (list[i] < min) {
                min = list[i];
            }
        }
        res[0] = max;
        res[1] = min;
        return res;
    }

    public int[] OneDimArr(int[][][] list){
        int[] converted = new int[list.length * list[0].length * list[0][0].length];
        int count = 0;

        for (int i = 0; i < list.length; i++) {
            for (int j = 0; j < list[0].length; j++) {
                for (int k = 0; k < list[0][0].length; k++) {
                    converted[count] = list[i][j][k];
                    count++;
                }
            }
        }
        return converted;
    }

    public int[][][] Normalisation(int min, int max,int[][][] img){
        for(int i = 0; i < img.length; i++){
            for(int j = 0; j < img[0].length; j++){
                int red = img[i][j][0];
                int green = img[i][j][1];
                int blue = img[i][j][2];
                img[i][j][0] = (int)(((double)(red - min) * 255) / (max - min));
                img[i][j][1] = (int)(((double)(green - min) * 255) / (max - min));
                img[i][j][2] = (int)(((double)(blue - min) * 255) / (max - min));
            }
        }
        return img;
    }

    public int[][][] trim(int[][][] img){
        int height = img[0].length;
        int width = img.length;
        int [][][] trimcopy = new int[width-2][height-2][3];
        for(int i = 1; i < width-1; i++){
            for(int j = 1; j < height-1;j++){
                img[i-1][j-1] = img[i][j];
                trimcopy[i-1][j-1] = img[i][j];
            }
        }
        return trimcopy;
    }

    public int[][][] combine(int [][][] x, int [][][] y) {
        int [][][] result = new int[x.length][x[0].length][3];
        for(int i = 0;i < y.length; i++){
            for(int j = 0;j < x[0].length; j++){
                for(int k = 0;k < x[0][0].length; k++){
                    int power = (int)(Math.pow((x[i][j][k] + y[i][j][k]),2));
                    result[i][j][k] = (int)(Math.sqrt(power));
                }
            }
        }
        return result;
    }




    public int[][][] checkThreshold(int[][][] img, int threshold){  //unused, compliment to edge detection
        for (int i = 0; i < img.length; i++){
            for (int j = 0; j < img[0].length; j++){
                if (img[i][j][0] < threshold){
                    img[i][j][0] = 0;
                    img[i][j][1] = 0;
                    img[i][j][2] = 0;
                } else {
                    img[i][j][0] = 255;
                    img[i][j][1] = 255;
                    img[i][j][2] = 255;
                }
            }
        }
        return img;
    }

    public void replaceCopy(){
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                for(int k=0;k<3;k++){
                    copy[j][i][k] = img[j][i][k];
                }
            }
        }
    }


    public void BlackWhiteConversion(){
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                for(int k = 0; k < 3;k++){
                    int m = img[x][y][0] + img[x][y][1] + img[x][y][2];
                    if(m>=383)
                    {
                        img[x][y][k] = 255;
                    }
                    else{
                        img[x][y][k] = 0;
                    }
                }
            }
        }
    }

    }





