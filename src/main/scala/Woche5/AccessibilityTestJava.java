package Woche5;

import java.util.Arrays;
import java.util.List;

public class AccessibilityTestJava {
    public static void main(String args[]){
        mOne();
        mTwo();
        mThree();
        mFour();
    }
    public static void mOne() {
        int i=0;
        i+=2;
        if (i==3){
            System.out.println("Das sollte nicht erreichbar sein!");
        }
    }
    public static void mTwo(){
        int i=0;
        List<Integer> seq = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        for (int ignored : seq) {
            i++;
        }
        if (i>=10){
            System.out.println("Das sollte nicht erreichbar sein!");
        }
    }
    public static void mThree(){
        //In der Methode ist ALLES erreichbar
        var i=0;
        i+=1;
        int j=10;
        i+=j;
        if(i>=11){
            System.out.println("Das ist erreichbar!");
        }
    }
    public static void mFour(){
        var i=0;
        if(Math.random() > 0.5) i=2;
        if(i==1){
            System.out.println("Das sollte nicht erreichbar sein!");
        }
    }
}
