import java.util.*;
import java.io.*;

/*
  Main.java

  Box Game solution using a 3D cubelet model.
  - Faces indexed as:
      base  (1) -> y = N-1 (down)
      back  (2) -> z = 0
      top   (3) -> y = 0
      front (4) -> z = N-1
      left  (5) -> x = 0
      right (6) -> x = N-1
  - Orientation indices for facelets on each cubelet:
      0: UP (towards -y)
      1: DOWN (towards +y)
      2: LEFT (towards -x)
      3: RIGHT (towards +x)
      4: FRONT (towards +z)
      5: BACK  (towards -z)
*/

public class Main {
    static final int U = 0, D = 1, L = 2, R = 3, F = 4, B = 5;

    static class Cubelet {
        // face colors or 0 if absent
        char[] face = new char[6];

        Cubelet() {
            Arrays.fill(face, 0);
        }

        Cubelet copy() {
            Cubelet c = new Cubelet();
            System.arraycopy(this.face, 0, c.face, 0, 6);
            return c;
        }
    }

    static int N;

    static Cubelet[][][] newCube(int n) {
        Cubelet[][][] a = new Cubelet[n][n][n];
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                for (int z = 0; z < n; z++)
                    a[x][y][z] = new Cubelet();
        return a;
    }

    static Cubelet[][][] deepCopyCube(Cubelet[][][] c) {
        int n = c.length;
        Cubelet[][][] cp = newCube(n);
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                for (int z = 0; z < n; z++)
                    cp[x][y][z] = c[x][y][z].copy();
        return cp;
    }

    static void assignFromFaces(Cubelet[][][] cube, Map<String, char[][]> faces) {
        int n = N;
        // front face (z = n-1): face grid rows 0..n-1 top->bottom -> y 0..n-1
        // columns 0..n-1 left->right -> x 0..n-1
        char[][] front = faces.get("front");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[c][r][n - 1].face[F] = front[r][c];

        // back face (z = 0): when viewing back face from front, the orientation needs
        // flipping.
        // We'll map back grid row r, col c -> cube[n-1-c][r][0].face[B]
        char[][] back = faces.get("back");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[n - 1 - c][r][0].face[B] = back[r][c];

        // top face (y = 0): top grid row r, col c -> cube[c][0][n-1-r].face[U]
        char[][] top = faces.get("top");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[c][0][n - 1 - r].face[U] = top[r][c];

        // base (bottom) y = n-1: base grid row r, col c -> cube[c][n-1][r].face[D]
        char[][] base = faces.get("base");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[c][n - 1][r].face[D] = base[r][c];

        // left face x = 0: left grid row r, col c -> cube[0][r][n-1-c].face[L]
        char[][] left = faces.get("left");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[0][r][n - 1 - c].face[L] = left[r][c];

        // right face x = n-1: right grid row r, col c -> cube[n-1][r][c].face[R]
        char[][] right = faces.get("right");
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                cube[n - 1][r][c].face[R] = right[r][c];

        // Note: This mapping aims to be consistent: front faces +z, back -z, top -y,
        // base +y, left -x, right +x.
        // The orientation of back, left, top mappings include flips so adjacent edges
        // align.
    }

    // helper rotate cubelets positions and their face orientations when rotating a
    // layer
    // rotate a slice around X axis (x fixed) clockwise when looking from +X towards
    // origin?
    // We'll implement specific axis rotations needed below.
    static void rotateLayerAroundX(Cubelet[][][] cube, int x, boolean clockwise) {
        // rotates layer with coordinate x around X axis (i.e., y-z plane rotation).
        // clockwise means rotate so that +y goes to +z when viewed from +X? We'll
        // implement consistent rotation:
        int n = N;
        Cubelet[][][] tmp = newCube(n);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                tmp[x][i][j] = cube[x][i][j];
        // perform rotation on tmp[x] (2D y (rows) by z (cols))
        for (int y = 0; y < n; y++)
            for (int z = 0; z < n; z++) {
                int ny = clockwise ? n - 1 - z : z;
                int nz = clockwise ? y : n - 1 - y;
                cube[x][ny][nz] = rotateCubeletOrientation(tmp[x][y][z], 'X', clockwise);
            }
    }

    static void rotateLayerAroundY(Cubelet[][][] cube, int y, boolean clockwise) {
        int n = N;
        Cubelet[][][] tmp = newCube(n);
        for (int x = 0; x < n; x++)
            for (int z = 0; z < n; z++)
                tmp[x][y][z] = cube[x][y][z];
        for (int x = 0; x < n; x++)
            for (int z = 0; z < n; z++) {
                int nx = clockwise ? z : n - 1 - z;
                int nz = clockwise ? n - 1 - x : x;
                cube[nx][y][nz] = rotateCubeletOrientation(tmp[x][y][z], 'Y', clockwise);
            }
    }

    static void rotateLayerAroundZ(Cubelet[][][] cube, int z, boolean clockwise) {
        int n = N;
        Cubelet[][][] tmp = newCube(n);
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                tmp[x][y][z] = cube[x][y][z];
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++) {
                int nx = clockwise ? n - 1 - y : y;
                int ny = clockwise ? x : n - 1 - x;
                cube[nx][ny][z] = rotateCubeletOrientation(tmp[x][y][z], 'Z', clockwise);
            }
    }

    // rotate cubelet face indices according to axis rotation
    static Cubelet rotateCubeletOrientation(Cubelet src, char axis, boolean clockwise) {
        Cubelet t = new Cubelet();
        // copy then remap indexes
        // axes: X rotates around x (left-right), Y around y (top-bottom), Z around z
        // (back-front)
        // For rotation around X (positive direction from - to +?), we map faces:
        // U->B->D->F->U or similar; this mapping chosen to match our coordinate system.
        if (axis == 'X') {
            if (clockwise) {
                // rotate y,z: U -> F, F -> D, D -> B, B -> U (but check directions signs)
                t.face[U] = src.face[B];
                t.face[F] = src.face[U];
                t.face[D] = src.face[F];
                t.face[B] = src.face[D];
                t.face[L] = src.face[L];
                t.face[R] = src.face[R];
            } else {
                t.face[U] = src.face[F];
                t.face[F] = src.face[D];
                t.face[D] = src.face[B];
                t.face[B] = src.face[U];
                t.face[L] = src.face[L];
                t.face[R] = src.face[R];
            }
        } else if (axis == 'Y') {
            if (clockwise) {
                // rotate z,x: F->R->B->L->F
                t.face[F] = src.face[L];
                t.face[R] = src.face[F];
                t.face[B] = src.face[R];
                t.face[L] = src.face[B];
                t.face[U] = src.face[U];
                t.face[D] = src.face[D];
            } else {
                t.face[F] = src.face[R];
                t.face[R] = src.face[B];
                t.face[B] = src.face[L];
                t.face[L] = src.face[F];
                t.face[U] = src.face[U];
                t.face[D] = src.face[D];
            }
        } else { // 'Z'
            if (clockwise) {
                // rotate x,y: U->L->D->R->U
                t.face[U] = src.face[R];
                t.face[L] = src.face[U];
                t.face[D] = src.face[L];
                t.face[R] = src.face[D];
                t.face[F] = src.face[F];
                t.face[B] = src.face[B];
            } else {
                t.face[U] = src.face[L];
                t.face[L] = src.face[D];
                t.face[D] = src.face[R];
                t.face[R] = src.face[U];
                t.face[F] = src.face[F];
                t.face[B] = src.face[B];
            }
        }
        return t;
    }

    // After operations, build faces from cubelets to compare face uniformity and
    // detect faulty cell differences.
    static Map<String, char[][]> extractFaces(Cubelet[][][] cube) {
        int n = N;
        Map<String, char[][]> faces = new HashMap<>();
        char[][] front = new char[n][n];
        char[][] back = new char[n][n];
        char[][] top = new char[n][n];
        char[][] base = new char[n][n];
        char[][] left = new char[n][n];
        char[][] right = new char[n][n];

        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                front[r][c] = cube[c][r][n - 1].face[F] == 0 ? '?' : cube[c][r][n - 1].face[F];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                back[r][c] = cube[n - 1 - c][r][0].face[B] == 0 ? '?' : cube[n - 1 - c][r][0].face[B];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                top[r][c] = cube[c][0][n - 1 - r].face[U] == 0 ? '?' : cube[c][0][n - 1 - r].face[U];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                base[r][c] = cube[c][n - 1][r].face[D] == 0 ? '?' : cube[c][n - 1][r].face[D];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                left[r][c] = cube[0][r][n - 1 - c].face[L] == 0 ? '?' : cube[0][r][n - 1 - c].face[L];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < n; c++)
                right[r][c] = cube[n - 1][r][c].face[R] == 0 ? '?' : cube[n - 1][r][c].face[R];

        faces.put("front", front);
        faces.put("back", back);
        faces.put("top", top);
        faces.put("base", base);
        faces.put("left", left);
        faces.put("right", right);
        return faces;
    }

    static boolean anyFaceUniform(Map<String, char[][]> faces) {
        for (char[][] f : faces.values()) {
            char ch = f[0][0];
            boolean ok = true;
            for (int i = 0; i < N && ok; i++)
                for (int j = 0; j < N; j++)
                    if (f[i][j] != ch) {
                        ok = false;
                        break;
                    }
            if (ok)
                return true;
        }
        return false;
    }

    // Execute instruction types:
    // 1) turn left (whole cube yaw left)
    // 2) turn right
    // 3) rotate front (roll towards front)
    // 4) rotate back
    // 5) rotate left
    // 6) rotate right
    // 7) "<side> idx <dir>" rotate row/column of that side by idx in direction
    static void executeInstruction(Cubelet[][][] cube, String instr) {
        instr = instr.trim();
        if (instr.equals("turn left")) {
            // front->left, left->back, back->right, right->front
            // top rotated right, base rotated left
            // Achieve by rotating around Y axis (up axis) by +90 (turn left means yaw left?
            // we will rotate around Y)
            // According to mapping, "turn left" moves front to left so cube rotated CCW
            // around Y (when viewed from top).
            rotateWholeAroundY(cube, false); // counter-clockwise
            // Also rotate top face right and base left: those are secondary rotations - but
            // whole rotation already rotates top/base orientations.
        } else if (instr.equals("turn right")) {
            rotateWholeAroundY(cube, true);
        } else if (instr.equals("rotate front")) {
            // rotate around X axis? rotate front face to base etc: front->base, base->back,
            // back->top, top->front
            // That's rotation around X? Actually it's rotation around horizontal axis
            // left-right (X) by +90 (front moves to base)
            rotateWholeAroundX(cube, true);
        } else if (instr.equals("rotate back")) {
            rotateWholeAroundX(cube, false);
        } else if (instr.equals("rotate left")) {
            // rotate around Z? left becomes base etc: top->left, left->base, base->right,
            // right->top
            rotateWholeAroundZ(cube, false);
        } else if (instr.equals("rotate right")) {
            rotateWholeAroundZ(cube, true);
        } else {
            // type 7: "<side> idx <dir>"
            String[] p = instr.split(" ");
            if (p.length != 3)
                return;
            String side = p[0];
            int idx = Integer.parseInt(p[1]); // 1-indexed
            String dir = p[2];
            // perform row/col rotation relative to viewing the side frontally
            rotateSideRowColumn(cube, side, idx, dir);
        }
    }

    static void rotateWholeAroundY(Cubelet[][][] cube, boolean clockwise) {
        // rotate the whole cube around Y axis (up-down) by 90 deg
        // we can rotate all layers by calling rotateLayerAroundY for each y with same
        // direction but positions permuted
        int n = N;
        Cubelet[][][] tmp = deepCopyCube(cube);
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                for (int z = 0; z < n; z++) {
                    int nx, nz;
                    if (clockwise) { // rotate so front->right
                        nx = n - 1 - z;
                        nz = x;
                    } else {
                        nx = z;
                        nz = n - 1 - x;
                    }
                    cube[nx][y][nz] = rotateCubeletOrientation(tmp[x][y][z], 'Y', clockwise);
                }
    }

    static void rotateWholeAroundX(Cubelet[][][] cube, boolean clockwise) {
        int n = N;
        Cubelet[][][] tmp = deepCopyCube(cube);
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                for (int z = 0; z < n; z++) {
                    int ny, nz;
                    if (clockwise) { // front -> base
                        ny = z;
                        nz = n - 1 - y;
                    } else {
                        ny = n - 1 - z;
                        nz = y;
                    }
                    cube[x][ny][nz] = rotateCubeletOrientation(tmp[x][y][z], 'X', clockwise);
                }
    }

    static void rotateWholeAroundZ(Cubelet[][][] cube, boolean clockwise) {
        int n = N;
        Cubelet[][][] tmp = deepCopyCube(cube);
        for (int x = 0; x < n; x++)
            for (int y = 0; y < n; y++)
                for (int z = 0; z < n; z++) {
                    int nx, ny;
                    if (clockwise) {
                        nx = n - 1 - y;
                        ny = x;
                    } else {
                        nx = y;
                        ny = n - 1 - x;
                    }
                    cube[nx][ny][z] = rotateCubeletOrientation(tmp[x][y][z], 'Z', clockwise);
                }
    }

    // Rotate a specific side's row/col relative to that side viewed frontally.
    static void rotateSideRowColumn(Cubelet[][][] cube, String side, int idx1, String dir) {
        // idx1 is 1-indexed
        int idx = idx1 - 1;
        // For each side, determine which axis and which layer(s) shift among four
        // faces.
        // We'll implement by rotating corresponding layer around correct axis (for
        // turning a row/col of that face).
        // For operations: when rotating a row on 'top' left/right, it's equivalent to
        // rotating layer y=0 around Z axis.
        // We'll map:
        // side 'top': row r left/right -> rotate layer y=0 around Z (row number idx
        // maps to z position) -> but top has rows - complicated.
        // To keep consistent, we translate instruction to rotation around proper axis
        // on proper layer:
        // We will interpret:
        // - '<side> <row> left/right' : rotate layer parallel to x-axis? Hard mapping.
        // Simpler approach: perform direct swap of facelets on the 4 adjacent faces
        // computed per side and row/col.
        int n = N;
        // Build face arrays from cube
        Map<String, char[][]> faces = extractFaces(cube);
        char[][] f = faces.get(side);
        // We'll perform the row/col rotation on the face's own grid and then update the
        // cubelets accordingly by writing back.
        // Directions: left/right operate on rows; up/down operate on columns.
        if (dir.equals("left") || dir.equals("right")) {
            // rotate row idx on face
            char[] row = new char[n];
            for (int j = 0; j < n; j++)
                row[j] = f[idx][j];
            char[] newRow = new char[n];
            if (dir.equals("left")) {
                for (int j = 0; j < n; j++)
                    newRow[j] = row[(j + 1) % n];
            } else {
                for (int j = 0; j < n; j++)
                    newRow[j] = row[(j - 1 + n) % n];
            }
            // write back into face grid f
            for (int j = 0; j < n; j++)
                f[idx][j] = newRow[j];
        } else {
            // up/down -> column idx
            char[] col = new char[n];
            for (int i = 0; i < n; i++)
                col[i] = f[i][idx];
            char[] newCol = new char[n];
            if (dir.equals("up")) {
                for (int i = 0; i < n; i++)
                    newCol[i] = col[(i + 1) % n];
            } else {
                for (int i = 0; i < n; i++)
                    newCol[i] = col[(i - 1 + n) % n];
            }
            for (int i = 0; i < n; i++)
                f[i][idx] = newCol[i];
        }
        // Now write back all faces grids to cubelets by reassigning face colors for
        // corresponding cubelets.
        // Easiest: reconstruct entire cube face colors mapping by extracting original
        // cubelets and replacing only the face values according to updated face grids.
        // We'll extract current cubelets and update their face colors for the six faces
        // to match updated faces mapping.
        // Get current cubelets (we'll not move cubelets themselves)
        // Update face colors for positions corresponding to face grids
        // front
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[c2][r][n - 1].face[F] = faces.get("front")[r][c2];
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[n - 1 - c2][r][0].face[B] = faces.get("back")[r][c2];
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[c2][0][n - 1 - r].face[U] = faces.get("top")[r][c2];
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[c2][n - 1][r].face[D] = faces.get("base")[r][c2];
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[0][r][n - 1 - c2].face[L] = faces.get("left")[r][c2];
        for (int r = 0; r < n; r++)
            for (int c2 = 0; c2 < n; c2++)
                cube[n - 1][r][c2].face[R] = faces.get("right")[r][c2];
    }

    // Build initial cubelets from input faces map
    static Cubelet[][][] buildCubeFromFaces(Map<String, char[][]> faces) {
        Cubelet[][][] cube = newCube(N);
        assignFromFaces(cube, faces);
        return cube;
    }

    // Check whether by applying all instructions (except possibly one) cube has any
    // uniform face
    // Also handle faulty single-cell color change: we'll implement search that
    // tries to change one cubelet face to another color and test.
    static Result analyze(Map<String, char[][]> facesInput, List<String> instrs) {
        Cubelet[][][] initial = buildCubeFromFaces(facesInput);

        // First, check without any correction: try skipping each instruction
        for (int skip = 0; skip < instrs.size(); skip++) {
            Cubelet[][][] cpy = deepCopyCube(initial);
            for (int i = 0; i < instrs.size(); i++)
                if (i != skip)
                    executeInstruction(cpy, instrs.get(i));
            Map<String, char[][]> outFaces = extractFaces(cpy);
            if (anyFaceUniform(outFaces)) {
                // Not faulty
                return new Result(false, instrs.get(skip));
            }
        }

        // If none works, try assuming single faulty cubelet (one facelet color was
        // altered).
        // Brute-force: for each facelet position among 6*N*N, try replacing its color
        // with another color from the multiset (or with all capital letters present)
        // and test.
        // Collect all unique colors originally present
        Set<Character> colorSet = new HashSet<>();
        for (char[][] g : facesInput.values())
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    colorSet.add(g[i][j]);

        // For each position p (faceName, r,c) try fixing by setting it to each color
        // from colorSet (or restoring original)
        List<FacePos> positions = new ArrayList<>();
        String[] names = { "base", "back", "top", "front", "left", "right" };
        for (String nm : names) {
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    positions.add(new FacePos(nm, i, j));
        }

        for (FacePos pos : positions) {
            char orig = facesInput.get(pos.face)[pos.r][pos.c];
            for (char candidate : colorSet) {
                if (candidate == orig)
                    continue;
                // create corrected faces map
                Map<String, char[][]> corrected = new HashMap<>();
                for (String nm : names) {
                    char[][] f = new char[N][N];
                    for (int i = 0; i < N; i++)
                        for (int j = 0; j < N; j++)
                            f[i][j] = facesInput.get(nm)[i][j];
                    corrected.put(nm, f);
                }
                corrected.get(pos.face)[pos.r][pos.c] = candidate;
                // try skipping each instruction
                for (int skip = 0; skip < instrs.size(); skip++) {
                    Cubelet[][][] cpy = buildCubeFromFaces(corrected);
                    for (int i = 0; i < instrs.size(); i++)
                        if (i != skip)
                            executeInstruction(cpy, instrs.get(i));
                    Map<String, char[][]> outFaces = extractFaces(cpy);
                    if (anyFaceUniform(outFaces)) {
                        // Found faulty & instruction
                        return new Result(true, instrs.get(skip));
                    }
                }
            }
        }

        // Not possible
        return new Result(false, null);
    }

    static class Result {
        boolean faulty;
        String instruction;

        Result(boolean f, String s) {
            faulty = f;
            instruction = s;
        }
    }

    static class FacePos {
        String face;
        int r, c;

        FacePos(String f, int r, int c) {
            this.face = f;
            this.r = r;
            this.c = c;
        }
    }

    // Read input and run
    public static void main(String[] args) throws Exception {
        FastScanner fs = new FastScanner(System.in);
        if (!fs.hasNext()) {
            return;
        }
        N = fs.nextInt();
        int K = fs.nextInt();
        fs.nextLine();

        String[] names = { "base", "back", "top", "front", "left", "right" };
        Map<String, char[][]> faces = new HashMap<>();
        for (String nm : names) {
            char[][] f = new char[N][N];
            for (int i = 0; i < N; i++) {
                String line = fs.nextLine();
                while (line.trim().isEmpty())
                    line = fs.nextLine();
                String[] parts = line.trim().split("\\s+");
                for (int j = 0; j < N; j++)
                    f[i][j] = parts[j].charAt(0);
            }
            faces.put(nm, f);
        }

        List<String> instrs = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            String line = fs.nextLine();
            while (line.trim().isEmpty())
                line = fs.nextLine();
            instrs.add(line.trim());
        }

        Result res = analyze(faces, instrs);
        if (res.instruction == null) {
            System.out.println("Not Possible");
        } else {
            if (res.faulty) {
                System.out.println("Faulty");
                System.out.println(res.instruction);
            } else {
                System.out.println(res.instruction);
            }
        }
    }

    // Fast scanner
    static class FastScanner {
        BufferedReader br;
        StringTokenizer st;

        FastScanner(InputStream is) {
            br = new BufferedReader(new InputStreamReader(is));
        }

        String next() throws IOException {
            while (st == null || !st.hasMoreTokens()) {
                String line = br.readLine();
                if (line == null)
                    return null;
                st = new StringTokenizer(line);
            }
            return st.nextToken();
        }

        boolean hasNext() throws IOException {
            br.mark(1);
            int c = br.read();
            if (c == -1)
                return false;
            br.reset();
            return true;
        }

        int nextInt() throws IOException {
            return Integer.parseInt(next());
        }

        String nextLine() throws IOException {
            return br.readLine();
        }
    }
}
