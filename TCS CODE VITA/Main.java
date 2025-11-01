import java.util.*;

class Codevita {

    static boolean allSame(char[][] face) {
        char ch = face[0][0];
        for (int i = 0; i < face.length; i++) {
            for (int j = 0; j < face[i].length; j++) {
                if (face[i][j] != ch) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean checkFault(Map<String, char[][]> cube) {
        Map<Character, Integer> count = new HashMap<>();
        int n = cube.get("front").length;
        for (String key : cube.keySet()) {
            char[][] f = cube.get(key);
            for (char[] r : f) {
                for (char c : r) {
                    count.put(c, count.getOrDefault(c, 0) + 1);
                }
            }
        }
        for (int val : count.values()) {
            if (val != n * n)
                return true;
        }
        return false;
    }

    static void rotate(char[][] face, String dir) {
        int n = face.length;
        char[][] temp = new char[n][n];
        if (dir.equals("right")) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    temp[j][n - 1 - i] = face[i][j];
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    temp[n - 1 - j][i] = face[i][j];
                }
            }
        }
        for (int i = 0; i < n; i++) {
            face[i] = Arrays.copyOf(temp[i], n);
        }
    }

    static void simulate(Map<String, char[][]> cube, String inst) {
        String[] arr = inst.split(" ");
        if (arr.length != 3)
            return;
        String side = arr[0];
        String dir = arr[2];
        if (side.equals("front")) {
            rotate(cube.get("front"), dir);
        }
    }

    static String findWrong(Map<String, char[][]> cube, List<String> steps) {
        for (int i = 0; i < steps.size(); i++) {
            Map<String, char[][]> copy = new HashMap<>();
            for (String face : cube.keySet()) {
                int n = cube.get(face).length;
                char[][] newFace = new char[n][n];
                for (int r = 0; r < n; r++) {
                    newFace[r] = Arrays.copyOf(cube.get(face)[r], n);
                }
                copy.put(face, newFace);
            }
            for (int j = 0; j < steps.size(); j++) {
                if (i == j)
                    continue;
                simulate(copy, steps.get(j));
            }
            for (char[][] f : copy.values()) {
                if (allSame(f))
                    return steps.get(i);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        int N = 3;
        String[] names = { "base", "back", "top", "front", "left", "right" };
        Map<String, char[][]> cube = new HashMap<>();

        for (String n : names) {
            char[][] f = new char[N][N];
            for (int i = 0; i < N; i++) {
                Arrays.fill(f[i], 'R');
            }
            cube.put(n, f);
        }

        List<String> moves = new ArrayList<>();
        moves.add("front 1 right");
        moves.add("front 1 left");

        if (checkFault(cube)) {
            System.out.println("Faulty");
            String wrong = findWrong(cube, moves);
            if (wrong != null)
                System.out.println(wrong);
        } else {
            String wrong = findWrong(cube, moves);
            if (wrong != null)
                System.out.println(wrong);
            else
                System.out.println("Not Possible");
        }
    }
}
