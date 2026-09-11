package com.tenco.view;

import com.tenco.dto.Admin;
import com.tenco.dto.Book;
import com.tenco.dto.Borrow;
import com.tenco.dto.Student;
import com.tenco.service.LibraryService;
import com.tenco.util.DatabaseUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

// 사용자의 입출력을 처리하는 View 클래스

// [역할]
// 키보드 입력을 받아 Service에 넘기고, 결과를 화면에 출력한다.
// SQL을 직접 실행하지 않고, 업무 규칙도 판단하지 않습니다.
// "빈 값인가", "숫자인가" 같은 입력 형식을 검사하고 서비스단에 맞는 객체내 값을 구해서 일을 위임한다.

// 스캐너를 통해서 하고자하는 메뉴를 고를 수 있도록 코드를 작성한다.
// 만약 1번이 도서 추가라면 해당하는 로직을 수행할 수 있도록 만든다.
public class LibraryView2 {
    private final LibraryService libraryService = new LibraryService();
    private final Scanner scanner = new Scanner(System.in);


    // 현재 로그인한 학생 정보가 null이 아니라면 로그인된 상태로 보면 된다.
    // 만약 null이라면 로그인이 필요한 기능에서 로그인 요청을 먼저 유도 해야 한다.
    private Integer currentStudentId = null;
    private String currentStudentName = null;
    private Student currentStudent = null;

    private Integer currentAdminId = null;
    private String currentAdminName = null;

    // 프로그램 메인 루프
    // [처리순서]
    // 1.메뉴를 출력한다.
    // 2. 번호를 입력 받는다.
    // 3. 번호에 맞는 메서드를 호출한다.
    // 4. 호출 중 SQLException 이 나면 에러 메세지를 출력하고 다시 1번으로 돌아간다.
    // 5. 0번을 입력하면 프로그램 종료 또는 return 루프 빠져 나간다.

    // 메뉴 출력
    // 사용자 입력값 받기

    // 예외 처리가 반드시 필요 하다
//            switch ("선택번호") {
//                case 1: 기능 호출
//            }
    public void start() {
        while (true) {
            try {
                menuPrint();
                System.out.println("메뉴 선택 : ");
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        addbook();
                        break;
                    case 2:
                        getAllBooks();
                        break;
                    case 3:
                        searchBooksByTitle();
                        break;
                    case 4:
                        addStudent();
                        break;
                    case 5:
                        getAllStudents();
                        break;
                    case 6:
                        borrowBook();
                        break;
                    case 7:
                        getBorrowedBooks();
                        break;
                    case 8:
                        returnBook();
                        break;
                    case 9:
                        authenticate();
                        break;
                    case 10:
                        logout();
                        break;
                    case 0:
                        System.out.println("도서관리 프로그램을 종료합니다");
                        DatabaseUtil.close(); // 커넥션 풀 종료
                        scanner.close();
                        return;
                    default:
                        System.out.println("잘못된 입력입니다. 0~10사이 숫자를 입력해주세요");
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 0. 메뉴 띄우기
    private void menuPrint() {
        System.out.println("======= 메뉴 =========");
        System.out.println("===========================");
        System.out.println("1. 도서등록  2. 전체 도서 조회  3. 도서 제목 검색");
        System.out.println("4. 학생등록  5. 전체 학생 조회  6. 도서 대출");
        System.out.println("7. 대출 도서 조회  8. 도서 반납  9. 로그인");
        System.out.println("10. 로그아웃  0. 프로그램 종료");
        System.out.println("=================================================");
    }

    // 1. 도서추가
    private void addbook() throws SQLException {
        System.out.println("==도서 등록==");
        System.out.println("도서 제목: ");
        String title = scanner.nextLine();
        System.out.println("저자: ");
        String author = scanner.nextLine();

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);

        libraryService.addBook(book);
        System.out.println("도서가 정상적으로 등록되었습니다.");
    }

    // 2. 전체 도서 조회
    private void getAllBooks() throws SQLException {
        System.out.println("== 전체 도서 목록 ==");
        List<Book> books = libraryService.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("등록된 도서가 없습니다.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }

    // 3. 도서 제목 검색
    private void searchBooksByTitle() throws SQLException {
        System.out.println("== 도서 제목 검색 ==");
        System.out.println("검색할 도서 제목 : ");
        String title = scanner.nextLine();

        List<Book> books = libraryService.searchBooksByTitle(title);
        if (books.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return;
        }
        for (Book book : books) {
            System.out.println(book);
        }
    }

    // 4. 학생 등록
    private void addStudent() throws SQLException {
        System.out.println("== 학생 등록 ==");
        System.out.println("학생 이름: ");
        String name = scanner.nextLine();
        System.out.println("학번: ");
        String studentId = scanner.nextLine();

        Student student = new Student();
        student.setName(name);
        student.setStudentId(studentId);

        libraryService.addStudent(student);
        System.out.println("학생이 정상적으로 등록되었습니다.");
    }

    /// 5. 전체 학생 조회
    private void getAllStudents() throws SQLException {
        System.out.println("== 전체 학생 조회 ==");
        List<Student> students = libraryService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("등록된 학생이 없습니다.");
            return;
        }
        for (Student student : students) {
            System.out.println(student);
        }

    }

    // 6. 도서 대출
    private void borrowBook() throws SQLException {
        if (!checkLogin()) return;

        System.out.println("== 도서 대출 ==");
        System.out.println("대출할 도서 Id : ");
        int bookId = Integer.parseInt(scanner.nextLine().trim());

        libraryService.borrowBook(bookId, currentStudentId);
        System.out.println("도서 대출이 완료되었습니다.");
    }

    // 7. 대출 중인 도서 조회
    private void getBorrowedBooks() throws SQLException {
        System.out.println("== 대출 목록 조회 ==");
        List<Borrow> borrowList = libraryService.getBorrowedBooks();
        if (borrowList.isEmpty()) {
            System.out.println("대출 중인 도서가 없습니다.");
            return;
        }
        for (Borrow borrow : borrowList) {
            System.out.println(borrow);
        }
    }

    // 8. 도서 반납
    private void returnBook() throws SQLException {
        System.out.println("== 도서 반납 ==");
        System.out.println("반납할 도서 Id: ");
        int bookId = Integer.parseInt(scanner.nextLine().trim());

        libraryService.returnBook(bookId, currentStudentId);
        System.out.println("도서 반납이 완료되었습니다.");
    }

    // 9. 로그인
    private void authenticate() throws SQLException {
        System.out.println("== 로그인 ==");
        System.out.println("학번 입력 : ");
        String studentId = scanner.nextLine();

        Student student = libraryService.getStudentByStudentId(studentId);
        if (student == null) {
            System.out.println("존재하지 않는 학벅입니다.");
        } else {
            this.currentStudent = student;
            System.out.println(currentStudentName + "님 로그인성공");
        }
    }

    // 10. 로그아웃
    private void logout() {
        if (currentStudent == null) {
            System.out.println("로그인 상태가 아닙니다.");
            return;
        }
        System.out.println(currentStudent.getName() + "님 로그아웃합니다.");
        this.currentStudent = null;
    }

    // 6.1 로그인 여부 검사
    private boolean checkLogin() {
        if (currentStudent == null) {
            System.out.println("로그인이 필요합니다. 로그인 해주세요");
            return false;
        }
        return true;
    }

    // 관리자 로그인
    // 1. 로그인 상태이면 중단.
    // 2. ID와 비밀번호 입력 (비밀번호는 공백도 문자이므로  trim 하지 않음)
    // 3. Service 에 인증을 맡기고 null 실패

    private void adminLogin() throws SQLException {
        if (isLoggedIn()) {
            System.out.println("이미 로그인 중입니다. 먼저 로그아웃해주세요 (메뉴 10번)");
            return;
        }
        System.out.println("아이디 : ");
        String adminId = scanner.nextLine().trim();
        System.out.println("비밀번호 : ");
        String password = scanner.nextLine();

        Admin admin = libraryService.authenticateAdmin(adminId, password);
        if (admin == null) {
            System.out.println("관리자 ID 또는 비밀번호가 올바르지 않습니다.");
        } else {
            currentAdminId = admin.getId();
            currentAdminName = admin.getName();
            System.out.println(currentAdminName+ "관리자님, 환영합니다.");
        }
    }

    private boolean isLoggedIn() {
        return currentAdminId != null || currentAdminId != null;
    }

    // 해당 기능을 각각의 메서드로 설계

}
