package com.tenco.dao;

import com.tenco.dto.Borrow;
import com.tenco.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public void borrowBook(int bookId, int studnetId) throws SQLException {

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

            // 2. 대출 가능 여부 확인
            String checkSql = """
                    SELECT available FROM books WHERE id = ?
                    """;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("존재하지 않는 도서입니다 ID : " + bookId);
                    }
                    if (!rs.getBoolean("available")) {
                        throw new SQLException("현재 대출 중인 도서입니다. 반납 후 이용가능합니다.");
                    }
                }

            }
            // 3. 코드가 여기까지 내려온다면 대출 가능한 bookId이다 -> 대출 기록 처리
            String borrowSql = """
                    INSERT INTO borrows (book_id, student_id, borrow_date)
                    VALUES (?, ?, ?)
                    """;
            int rows;
            try (PreparedStatement borrowPstmt = conn.prepareStatement(borrowSql)) {
                borrowPstmt.setInt(1, bookId);
                borrowPstmt.setInt(2, studnetId);
                // java.time.LocalDate를 JDBC가 이해하는 java.sql.Date로 변환해주어야한다.
                borrowPstmt.setDate(3, Date.valueOf(LocalDate.now()));
                rows = borrowPstmt.executeUpdate();
            }
            if (rows < 0) {
                throw new SQLException("적용된 기록이 없습니다.");
            }
            // 4. 도서 상태 변경 (대출 불가로 해당 도서 처리)
            String updateSql = """
                    UPDATE books SET available = FALSE
                    WHERE id = ?
                    """;
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, bookId);
                updatePstmt.executeUpdate();
            }
            // 5. 여기까지 모두 성공했답면 확정 처리
            conn.commit();

        } catch (SQLException e) {
            // 5. 하나라도 실패시 rollback 처리
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException(e);
            // conn.rollback();
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // 다시 변경 반드시 처리
                conn.close();
            }
        }
    }

    // 3. 도서 반납 처리 (트랜잭션)
    // [처리 순서]
    // 1. DB 연결을 얻고 자동 커밋을 끈다 (트랜잭션 시작)
    // 2. 이 학생이 이 도서를 빌린 뒤 아직 반납하지 않은 기록이 있는지 먼저 확인 - SELECT
    // 3. 찾은 대출 기록의 return_date 를 오늘 날짜로 UPDATE 한다. - UPDATE
    // 4. books 테이블에 available 을 TRUE로 변경  - UPDATE
    // 5. 2~4 까지 모두 성공하면 commit, 하나라도 실패시 rollback 처리
    // 6. 자동 커밋을 원래대로 되돌리고 연결을 닫는다.
    public void returnBook(int bookId, int studentId) throws SQLException {
        Connection conn = null;

        try {
            // 1. 트랜잭션 시작
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // 2. 해당 학생이 대출 중인 기록이 있는지 확인
            // 같은 학생이 같은 책을 예전에 빌렸다 반납한 기록이 있어도 그건 제외 해야 한다.
            String checkSql = """
                    SELECT id FROM borrow WHERE book_id = ? AND student_id = ? AND return_date IS NULL
                    """;
            int borrowId;
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("해당 대출 기록이 없거나 이미 반납되었습니다.");
                    }
                    // 추후 단계에서 어느 행을 수정할지 알아야 하므로 id를 미리 꺼내 둔다.
                    borrowId = rs.getInt("id");
                }
            }

            // 3. 반납일 대출테이블에 기록 - 업데이트
            String updateBorrowSql = """
                    UPDATE borrows SET return_date = ? WHERE id = ?
                    """;
            try (PreparedStatement updateBorrowPstmt = conn.prepareStatement(updateBorrowSql)) {
                updateBorrowPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                updateBorrowPstmt.setInt(2, borrowId);
                updateBorrowPstmt.executeUpdate();
            }

            // 4. 도서 상태 변경 (대출 가능 상태로 만들어야 함)
            String updateBookSql = """
                    UPDATE books SET available = TRUE WHERE id = ?
                    """;
            try (PreparedStatement updateBookPstmt = conn.prepareStatement(updateBookSql)) {
                updateBookPstmt.setInt(1, bookId);
                updateBookPstmt.executeUpdate();
            }

            // 5. 모두 성공시 커밋
            conn.commit();

        } catch (SQLException e) {
            // 하나라도 실패시 rollbackㅊ ㅊ ㅓ리
            if (conn != null) {
                conn.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            // 6. 뒷정리
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

    }

}
