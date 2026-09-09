package com.tenco.dao;

import com.tenco.dto.Borrow;

import java.util.ArrayList;
import java.util.List;

/**
 * 도서 대출/반납 관련 SQL 실행은 DAO
 */
public class BorrowDAO {

    // 1.1 현재 대출 중인 도서 목록 조회
    // 1.2 JOIN 해서 도서 이름 까지 출력
    public List<Borrow> getBorrowedBooks() {
        List<Borrow> borrowList = new ArrayList<>();
        // 1. JOIN 없이 코드 완성
        // 2. JOIN 결과 출력 완성
        return borrowList;
    }

    // 2. 도서 대출 기능 (트랜잭션)
    // 2.1 - 대상 도서 대출 가능 여부 - SELECT
    // 2.2 - 도서 대출 기록 - INSERT


    // 3. 도서 반납 처리 (트랜잭션)
    // 3.1 대출 기록 확인 - SELECT
    // 3.2 반납 기록 등록 - UPDATE

}
