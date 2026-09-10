package com.tenco.dao;

import com.tenco.dto.Borrow;
import com.tenco.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 도서 대출/반납 관련 SQL 실행은 DAO
 */
public class BorrowDAO {

    // 1.1 현재 대출 중인 도서 목록 조회
    // 사용자에게 보여주려면 도서 제목과 학생이름이 필요하므로
    // books, students 테이블에서 JOIN 해서 한번에 가져와야 한다.
    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();

        String sql = """
                select b.id, b.book_id, bk.title, b.student_id, s.name, b.borrow_date, b.return_date
                from borrows b
                inner join books bk on b.book_id = bk.id
                inner join students s on b.student_id = s.id
                where b.return_date is null
                order by b.borrow_date asc;
                """;

        try (Connection conn = DatabaseUtil.getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(sql)){
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    borrowList.add(Borrow.builder()
                            .id(rs.getInt("id"))
                            .bookId(rs.getInt("book_id"))
                            .bookTitle(rs.getString("title"))
                            .studentId(rs.getInt("student_id"))
                            .studentName(rs.getString("name"))
                            .borrowDate(rs.getDate("borrow_date").toLocalDate())
                            .build());
                }
            }
        }

        return borrowList;
    }

    // 2. 도서 대출 기능 (트랜잭션)
    // [처리 순서]
    // 1. DB 연결을 얻고 자동 커밋을 끈다 (트랜잭션 시작)
    // 2. 도서가 존재하고 대출 가능한 상태인지 확인 - SELECT
    // 3. borrow 테이블에 대출 기록 - INSERT
    // 4. books 테이블에 available 을 FALSE로 변경 - UPDATE
    // 5. 2~4 모두 성공하면 commit, 하나라도 실패하면 rollback
    // 6. 자동 커밋을 원래대로 되돌리고 연결을 닫는다.
    public void borrowBook(int bookId, int studnetId) {

        Connection conn = null;
        // try-with-resources로 선언하지 않은 이유
        // catch 블록에서 rollback을 호출하려면 conn 변수가 catch안에서도 보여야 합니다.
        // 그래서 try 바깥에 선언하고 finally에서 직접 닫습니다.

        try {
            // 1. 트랜잭션 시작
            conn = DatabaseUtil.getConnection();
            // 기본값 autoCommit = true 이고 , 이 상태에서는 SQL 한줄 한줄 마다 즉시 확정(반영)이 됩니다.
            // 이 값 false로 변경하면 commit()을 호출하기 전까지 모든 변경 사항이 임시 상태로 남습니다.
            conn.setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
            // conn.rollback();
        }
    }

    // 3. 도서 반납 처리 (트랜잭션)
    // 3.1 대출 기록 확인 - SELECT
    // 3.2 반납 기록 등록 - UPDATE

}
