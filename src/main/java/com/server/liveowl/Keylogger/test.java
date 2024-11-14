package com.server.liveowl.Keylogger;

import java.io.FileWriter;
import java.io.IOException;

public class test {
private static final String LOG_DIRECTORY = "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/fd720a2e_keylogs.txt";

public static void main(String[] args) throws IOException {
    String input = "P U B L I C Space S T A D A H D Backspace Backspace Backspace Backspace T I C Space O Backspace V O I D Space M A I N Shift 9 Shift 0 Shift Open Bracket Enter I N T Space X Space Equals Space 5 Semicolon Enter I N T Space Y Space Equals Space 1 0 Enter Shift Close Bracket";
    String[] resultArray = input.split(" ");
    StringBuilder code = new StringBuilder();

    for (int i = 0; i < resultArray.length; i++) {
        String element = resultArray[i];

        // Xử lý các trường hợp "Shift Shift Shift 9"
        if (element.equals("Shift")) {
            // Bỏ qua các "Shift" liên tiếp và chỉ xử lý ký tự cuối cùng
            while (i + 1 < resultArray.length && resultArray[i + 1].equals("Shift")) {
                i++; // Bỏ qua các lần lặp "Shift"
            }
            // Kiểm tra ký tự tiếp theo sau chuỗi "Shift"
            if (i + 1 < resultArray.length) {
                String nextElement = resultArray[i + 1];
                switch (nextElement) {
                    case "9":
                        code.append("(");
                        i++; // Bỏ qua "9" vì đã xử lý
                        continue;
                    case "0":
                        code.append(")");
                        i++;
                        continue;
                    case "Open":
                        code.append("{");
                        i += 2;
                        continue;
                    case "Close":
                        code.append("}");
                        i += 2;
                        continue;
                }
            }
            continue;
        }

        // Xử lý các từ khóa khác và chuyển đổi chúng
        switch (element) {
            case "Space":
                code.append(" ");
                break;
            case "Enter":
                code.append("\n");
                break;
            case "Equals":
                code.append("=");
                break;
            case "Semicolon":
                code.append(";");
                break;
            case "Backspace":
                if (code.length() > 0) {
                    code.deleteCharAt(code.length() - 1); // Xóa ký tự cuối cùng
                }
                break;
            default:
                code.append(element.toLowerCase());
                break;
        }
    }

    // Ghi mã đã chuyển đổi vào file
    try (FileWriter writer = new FileWriter(LOG_DIRECTORY, true)) {
        writer.write(code.toString());
        writer.flush();
    }
}
}
