package fggc.speedometer.util;

public class FGGCLoadbar {
    public static final FGGCLoadbar LEFT = new FGGCLoadbar(true, false);
    public static final FGGCLoadbar RIGHT = new FGGCLoadbar(false, true);
    
    private static final char BAR1 = '▏';
    private static final char BAR2 = '▎';
    private static final char BAR3 = '▍';
    private static final char BAR4 = '▌';
    private static final char BAR5 = '▋';
    private static final char BAR6 = '▊';
    private static final char BAR7 = '▉';
    private static final char BAR8 = '█';

    private static final byte size = 10;

    private boolean left;
    private boolean right;
    private double divSize = 100d / size;

    private FGGCLoadbar(boolean left, boolean right){
        this.left = left;
        this.right = right;
    }

    public String getLoadbar(double percent){
        if(this.divSize == 0) return "";
        String loadbar = "";
        double x = percent;
        while(x >= divSize){
            loadbar += BAR8;
            x -= divSize;
        }
        if(left) {
            loadbar = partBar(x) + loadbar;
            //while(loadbar.length() < size) loadbar = SPACE + loadbar;
            //loadbar = BAR1 + loadbar;
        }
        if(right) {
            loadbar += partBar(x);
            //while(loadbar.length() < size) loadbar += SPACE;
            //loadbar += BAR1;
        }
        return loadbar;
    }

    private char partBar(double partValue){
        double div = divSize / 8;
        switch((byte)Math.floor(partValue / div)){
            case 1:
                return BAR1;
            case 2:
                return BAR2;
            case 3:
                return BAR3;
            case 4:
                return BAR4;
            case 5:
                return BAR5;
            case 6:
                return BAR6;
            case 7:
                return BAR7;
            default:
                return ' ';
        }
    }
}
