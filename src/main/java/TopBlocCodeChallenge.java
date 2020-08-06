import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class TopBlocCodeChallenge {
    private static final String SERVER_CHALLENGE_PATH = "http://54.90.99.192:5000/challenge";

    public static void main(String[] args) {
        // Parse the excel files to create list of students and hashmaps of student ids to test and retake scores
        ArrayList<Student> students = parseStudentInfo();
        HashMap<String, Integer> idToTestScore = parseTestScores("Test Scores.xlsx");
        HashMap<String, Integer> idToRetakeScore = parseTestScores("Test Retake Scores.xlsx");

        for (Map.Entry<String, Integer> entry : idToTestScore.entrySet()) {
            String studentId = entry.getKey();
            int score = entry.getValue();

            if (idToRetakeScore.containsKey(studentId)) {
                int retakeScore = idToRetakeScore.get(studentId);
                if (retakeScore > score) {
                    // Update the original hashmap to have the higher retake score
                    idToTestScore.put(studentId, retakeScore);
                }
            }
        }

        // Calculate class average using the idToTestScore hashmap with the final test scores for each student
        int classAverage = (int) Math.round(
                (double) idToTestScore.values().stream().reduce(0, Integer::sum)
                        / idToTestScore.size()
        );

        // Create the array of female student ids
        ArrayList<String> femaleStudentIdsList = new ArrayList<>();
        for (Student student : students) {
            if (student.getGender() == 'F') {
                femaleStudentIdsList.add(student.getStudentId());
            }
        }

        String[] femaleStudentIds = femaleStudentIdsList.toArray(new String[0]);
        Arrays.sort(femaleStudentIds);

        // Build the JSON POST request and make the request
        try {
            JSONObject jo = new JSONObject();
            jo.put("id", "sabuammer@gmail.com");
            jo.put("name", "Salem Abuammer");
            jo.put("average", classAverage);
            jo.put("studentIds", femaleStudentIds);
            String json = jo.toString();

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_CHALLENGE_PATH))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Student> parseStudentInfo() {
        ArrayList<Student> students = new ArrayList<>();
        try {
            File studentInfoFile = new File(
                    TopBlocCodeChallenge
                            .class
                            .getResource("Student Info.xlsx")
                            .toURI()
            );

            FileInputStream inputStream = new FileInputStream(studentInfoFile);
            Workbook workbook = new XSSFWorkbook(inputStream);
            DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.rowIterator();
            Row headerRow = rowIterator.next(); // Ignore the header row of the file

            while (rowIterator.hasNext()) {
                Row currRow = rowIterator.next();
                ArrayList<String> rowContents = new ArrayList<>();

                for (Cell currCell : currRow) {
                    String cellVal = formatter.formatCellValue(currCell);
                    rowContents.add(cellVal);
                }

                String studentId = rowContents.get(0);
                String major = rowContents.get(1);
                char gender = rowContents.get(2).charAt(0);

                Student currStudent = new Student(studentId, major, gender);
                students.add(currStudent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return students;
    }

    private static HashMap<String, Integer> parseTestScores(String resourceName) {
        HashMap<String, Integer> idToTestScore = new HashMap<>();
        try {
            File testScoresFile = new File(
                    TopBlocCodeChallenge
                            .class
                            .getResource(resourceName)
                            .toURI()
            );

            FileInputStream inputStream = new FileInputStream(testScoresFile);
            Workbook workbook = new XSSFWorkbook(inputStream);
            DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.rowIterator();
            Row headerRow = rowIterator.next(); // Ignore the header row of the file

            while (rowIterator.hasNext()) {
                Row currRow = rowIterator.next();
                ArrayList<String> rowContents = new ArrayList<>();

                for (Cell currCell : currRow) {
                    String cellVal = formatter.formatCellValue(currCell);
                    rowContents.add(cellVal);
                }

                String studentId = rowContents.get(0);
                int score = Integer.parseInt(rowContents.get(1));

                idToTestScore.put(studentId, score);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idToTestScore;
    }
}