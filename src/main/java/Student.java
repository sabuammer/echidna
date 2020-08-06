public class Student {
    private final String studentId;
    private final String major;
    private final char gender;

    public Student(String studentId, String major, char gender) {
        this.studentId = studentId;
        this.major = major;
        this.gender = gender;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getMajor() {
        return major;
    }

    public char getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", major='" + major + '\'' +
                ", gender=" + gender +
                '}';
    }
}
