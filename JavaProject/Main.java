import java.io.*;
import java.util.*;

// ATM Simulator - simple version
// Queue = waiting customers | Stack = transactions | Array = accounts
// Searching = find account | Greedy = notes 500, 200, 100 | File = accounts.txt
public class Main {

    // accounts stored in arrays (same index = same account)
    static String[] accNo = new String[50];
    static String[] name = new String[50];
    static String[] pin = new String[50];
    static int[] balance = new int[50];
    static boolean[] blocked = new boolean[50];
    static int count = 0;

    static Queue<Integer> queue = new LinkedList<>();   // waiting customers (account index)
    static Stack<String> history = new Stack<>();       // transactions of current customer
    static int current = -1;                            // -1 = nobody logged in
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadFile();
        while (true) {
            System.out.println("\n1.Serve next  2.Balance  3.Deposit  4.Withdraw  5.Mini statement");
            System.out.println("6.Logout  7.Add to queue  0.Save & Exit");
            System.out.print("Enter option: ");
            int op = readInt();
            if (op == 0 || op == -1) break;
            else if (op == 1) serveNext();
            else if (op == 7) addToQueue();
            else if (op >= 2 && op <= 6) {
                if (current == -1) System.out.println("Serve a customer first (option 1).");
                else if (op == 2) System.out.println("Balance: " + balance[current]);
                else if (op == 3) deposit();
                else if (op == 4) withdraw();
                else if (op == 5) miniStatement();
                else { System.out.println("Goodbye, " + name[current] + "."); current = -1; }
            } else System.out.println("Invalid option.");
        }
        saveFile();
        System.out.println("Saved. Bye!");
    }

    // ---------- Searching: find account index by number (-1 if not found)
    static int find(String no) {
        for (int i = 0; i < count; i++)
            if (accNo[i].equalsIgnoreCase(no)) return i;
        return -1;
    }

    // ---------- Queue
    static void addToQueue() {
        System.out.print("Account number: ");
        int i = find(sc.nextLine().trim());
        if (i == -1) System.out.println("Account not found.");
        else if (blocked[i]) System.out.println("Card is blocked.");
        else { queue.add(i); System.out.println(name[i] + " added to queue."); }
    }

    static void serveNext() {
        if (current != -1) { System.out.println("Logout the current customer first."); return; }
        if (queue.isEmpty()) { System.out.println("Queue is empty."); return; }
        int i = queue.poll();
        System.out.println("Next customer: " + name[i] + " (" + accNo[i] + ")");
        for (int left = 3; left > 0; ) {
            System.out.print("PIN: ");
            if (sc.nextLine().trim().equals(pin[i])) {
                current = i;
                history.clear();
                System.out.println("Login successful. Welcome, " + name[i] + ".");
                System.out.println("Balance: " + balance[i]);
                return;
            }
            left--;
            if (left > 0) System.out.println("Incorrect PIN. Attempts left: " + left);
        }
        blocked[i] = true;
        System.out.println("Card blocked after 3 wrong attempts.");
    }

    // ---------- Deposit / Withdraw / Statement
    static void deposit() {
        System.out.print("Amount to deposit: ");
        int amt = readInt();
        if (amt <= 0) { System.out.println("Invalid amount."); return; }
        balance[current] += amt;
        history.push("Deposit " + amt + " | Balance " + balance[current]);
        System.out.println("Deposit successful. Balance: " + balance[current]);
    }

    static void withdraw() {
        System.out.print("Amount to withdraw: ");
        int amt = readInt();
        if (amt <= 0 || amt % 100 != 0) { System.out.println("Amount must be a multiple of 100."); return; }
        if (amt > balance[current]) { System.out.println("Insufficient balance."); return; }

        int[] notes = {500, 200, 100};          // Greedy: largest note first
        String result = "";
        int left = amt;
        for (int n : notes) {
            int c = left / n;
            left = left % n;
            if (c > 0) result += (result.isEmpty() ? "" : ", ") + n + " x " + c;
        }
        balance[current] -= amt;
        history.push("Withdraw " + amt + " | Balance " + balance[current]);
        System.out.println("Dispensed: " + result);
        System.out.println("Balance: " + balance[current]);
    }

    static void miniStatement() {
        if (history.isEmpty()) { System.out.println("No transactions yet."); return; }
        System.out.println("Mini Statement:");
        Stack<String> temp = new Stack<>();
        int n = 1;
        while (!history.isEmpty()) {            // pop = latest first
            String t = history.pop();
            System.out.println(n++ + ". " + t);
            temp.push(t);
        }
        while (!temp.isEmpty()) history.push(temp.pop());   // put back
    }

    // ---------- File handling (line: accNo,name,pin,balance,blocked)
    static void loadFile() {
        File f = new File("accounts.txt");
        try {
            if (!f.exists()) {                  // create sample file first time
                PrintWriter pw = new PrintWriter(f);
                pw.println("ACC101,Riya,1234,5000,false");
                pw.println("ACC102,Aman,4321,12000,false");
                pw.close();
            }
            Scanner file = new Scanner(f);
            while (file.hasNextLine()) {
                String[] p = file.nextLine().split(",");
                if (p.length < 5) continue;
                accNo[count] = p[0]; name[count] = p[1]; pin[count] = p[2];
                balance[count] = Integer.parseInt(p[3]);
                blocked[count] = p[4].equals("true");
                count++;
            }
            file.close();
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    static void saveFile() {
        try {
            PrintWriter pw = new PrintWriter("accounts.txt");
            for (int i = 0; i < count; i++)
                pw.println(accNo[i] + "," + name[i] + "," + pin[i] + "," + balance[i] + "," + blocked[i]);
            pw.close();
        } catch (Exception e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    static int readInt() {
        if (!sc.hasNextLine()) return -1;
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (Exception e) { return -2; }
    }
}