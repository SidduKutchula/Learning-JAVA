public class Siddu {
    public static void main(String args[]) {
        int[] arr = { 1, 3, 5, 7, 9, 11 };
        int target = 5;
        int ans = linearSearch(arr, target);
        System.out.println(ans);
    }

    static int linearSearch(int arr[], int target) {
        if (arr.length == 0)
            return -1;
        for (int i = 0 ; i<=arr.length ; i++) {
            if (arr[i] == target)
                return i;
        }
        return -1;
    }
}   