package com.tenco;

import com.tenco.dao.BookDAO;
import com.tenco.dao.BorrowDAO;
import com.tenco.dao.StudentDAO;
import com.tenco.dto.Book;
import com.tenco.dto.Borrow;
import com.tenco.dto.Student;
import com.tenco.view.LibraryView;

import java.sql.SQLException;
import java.util.List;

// 프로그램 진입점
public class Main {
    public static void main(String[] args) throws SQLException {
        LibraryView libraryView = new LibraryView();
        libraryView.start();


        // 학생 전체 조회 테스트
//        StudentDAO studentDAO = new StudentDAO();
//        Student student = studentDAO.getStudentByStudentId("20230003");
//        System.out.println(student);

        // 도서 DAO 테스트
//        BookDAO bookDAO = new BookDAO();
//        Book book2 = new Book(10, "테스트", "저가3",
//                "출판사이름", 2025, "1231123", true);

//ㅈ        Book book = Book.builder()
//                .title("테스트3")
//                .author("저자3")
//                .publisher("코리아출판")
//                .publicationYear(2026)
//                .isbn("12341234123")
//                .build();
//        bookDAO.addBook(book);


//
//        // List<Book> bookList = bookDAO.getAllBooks();
//        List<Book> bookList = bookDAO.searchBooksByTitle("알고리즘");
//        System.out.println("전체 조회된 row 수 : " +  bookList.size());
//        System.out.println("0번째 저장된 Book 객체 정보 출력 해보기 " + bookList.get(0).toString());

//        BorrowDAO borrowDAO = new BorrowDAO();
//        List<Borrow> borrowList = borrowDAO.getBorrowedBooks();
//        for (int i = 0; i < borrowList.size(); i++) {
//            System.out.println(borrowList.get(i).toString());
//        }
//
//        borrowDAO.returnBook(5,100);
//
//
////        borrowDAO.borrowBook(1,1);


    }
}