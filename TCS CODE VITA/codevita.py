import java.util.*;

public class Main {

    static boolean isUniform(char[][] f) {
        char c = f[0][0];
        for (char[] r : f)
            for (char x : r)
                if (x != c) return false;
        return true;
    }

    static boolean isFaulty(Map<String, char[][]> cube) {
        Map<Character,Integer> cnt = new HashMap<>();
        int n = cube.get("front").length;
        for (char[][] f : cube.values())
            for (char[] r : f)
                for (char c : r)
                    cnt.put(c, cnt.getOrDefault(c,0)+1);
        for (int v : cnt.values())
            if (v != n*n) return true;
        return false;
    }

    static void rotateFace(char[][] face, String dir) {
        int n = face.length;
        char[][] tmp = new char[n][n];
        if (dir.equals("right"))
            for (int i=0;i<n;i++) for (int j=0;j<n;j++)
                tmp[j][n-1-i]=face[i][j];
        else
            for (int i=0;i<n;i++) for (int j=0;j<n;j++)
                tmp[n-1-j][i]=face[i][j];
        for (int i=0;i<n;i++) face[i]=tmp[i].clone();
    }

    static void simulate(Map<String,char[][]> cube,String instr){
        String[] p=instr.split(" ");
        if(p.length!=3)return;
        String side=p[0],dir=p[2];
        int row=Integer.parseInt(p[1]);
        int n=cube.get("front").length;

        if(side.equals("front")){
            if(dir.equals("right")) rotateFace(cube.get("front"),"right");
            else if(dir.equals("left")) rotateFace(cube.get("front"),"left");
        }
    }

    static String findWrong(Map<String,char[][]> cube,List<String> instrs){
        for(int i=0;i<instrs.size();i++){
            Map<String,char[][]> t=new HashMap<>();
            for(String k:cube.keySet()){
                int n=cube.get(k).length;
                char[][] cp=new char[n][n];
                for(int r=0;r<n;r++) cp[r]=cube.get(k)[r].clone();
                t.put(k,cp);
            }
            for(int j=0;j<instrs.size();j++){
                if(j==i)continue;
                simulate(t,instrs.get(j));
            }
            for(char[][] f:t.values())
                if(isUniform(f))return instrs.get(i);
        }
        return null;
    }

    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        int N=sc.nextInt(),K=sc.nextInt();
        sc.nextLine();

        String[] names={"base","back","top","front","left","right"};
        Map<String,char[][]> cube=new HashMap<>();
        for(String nm:names){
            char[][] f=new char[N][N];
            for(int i=0;i<N;i++){
                String[] row=sc.nextLine().trim().split(" ");
                for(int j=0;j<N;j++) f[i][j]=row[j].charAt(0);
            }
            cube.put(nm,f);
        }

        List<String> instrs=new ArrayList<>();
        for(int i=0;i<K;i++) instrs.add(sc.nextLine().trim());

        if(isFaulty(cube)){
            System.out.println("Faulty");
            String w=findWrong(cube,instrs);
            if(w!=null)System.out.println(w);
        }else{
            String w=findWrong(cube,instrs);
            if(w!=null)System.out.println(w);
            else System.out.println("Not Possible");
        }
    }
}
