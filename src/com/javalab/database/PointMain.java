package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * [static 전역변수]
 * JDBC 프로그래밍을 위한 요소들을 모두 멤버변수 즉, 필드 위치로 뽑아올림
 *  - 본 클래스 어디서라도 사용가능한 전역변수가 됨
 *  [모듈화]
 *  - 데이터베이스 커넥션 + PreparedStatement + 쿼리실행 작업 모듈
 *  - 실제로 쿼리를 실행하고 결과를 받아오는 부분 모듈
 *  [미션]
 *   - 전체 상품의 정보를 조회하세요(카테고리명이 나오도록)
 */

public class PointMain {
	//[멤버변수]
	//1. Oracle 드라이버 일므 문자열 상숭
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
	
	//2. Oracle 데이터베이스 접속 경로(url) 문자열 상숭
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	
	//3.데이터베이스 접속 객체
	public static Connection con = null;
	
	//4. query 실행 객체
	public static PreparedStatement pstmt = null;
	
	//5. select 결과 저장 객체
	public static ResultSet rs = null;
	
	//6. Oracle 계정(id/pwd)
	public static String oracleId = "tempdb";
	
	//7. Oracle Password
	public static String oraclePwd = "1234";
	
	
	public static void main(String[] args) {
		//1. 디비 접속 메소드 호출
		connectDB();
		
//		2. 회원들과 보유 포인터 정보 조회
		getMeberAndPoint();
		
//		3. 이소미 회원에게 포인트 15점 추가
		updatePointSomi();
		
//		4. 관리자에게 포인트 30점 추가 집급
		updatePointmanager();
		
//		5. 전체 회원 평균 포인트보다 작은 회원 목록 조회
		getMemberLessThanAvg();
		
//		8. 자원반납
		closeResource(pstmt,rs);
		
//		9. 자원반납
		closeResource();
		
		
	}//end main
	
//	5. 전체 회원 평균 포인트보다 작은 회원 목록 조회
	private static void getMemberLessThanAvg() {
		try {
			String sql = "select m.user_id, m.name, m.pwd, m.email, m.phone,";
			sql += " decode(m.admin, 0, '일반사용자', 1, '관리자') admin,";
			sql += " p.point_id, p.points, to_char(reg_date,'yyyy-mm-dd') reg_date";
			sql += " from member m left outer join point p on m.user_id = p.user_id";
			sql += " where p.points < (select avg(points)";
			sql += "                  from point p)";
			
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				System.out.println(rs.getString("user_id")+"\t"+
						   rs.getString("name")+"\t"+
						   rs.getString("pwd")+"\t"+
						   rs.getString("email")+"\t"+
						   rs.getString("phone")+"\t"+
						   rs.getString("admin")+"\t"+
						   rs.getInt("point_id")+"\t"+
						   rs.getInt("points")+"\t"+
						   rs.getString("reg_date")
					);
			}
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end getMemberLessThanAvg()
	
//	4. 관리자에게 포인트 30점 추가 집급
	private static void updatePointmanager() {
		
		try {
			int intPoint = 30;
			int intAdmin = 1;
			
			String sql = "update point p";
				   sql += " set p.points = p.points + ?";
				   sql += " where p.user_id in(select m.user_id";
				   sql += "                  from member m";
				   sql += "                  where m.admin = ?)";
			
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1,intPoint);
				   pstmt.setInt(2,intAdmin);
				   System.out.println("4. pstmt 객체 생성 성공");
				   
				   // 쿼리 실행
				   // 처리된 결과 반환됨(수정된 행수)
				   //commit이 안되있으면 여기서 끝날수 있음
				   int result = pstmt.executeUpdate();
				   
				   if(result > 0) {
					   System.out.println("4.수정 성공");
				   }else {
					   System.out.println("4.수정 실패");
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
		
	}//end updatePointmanager()
	
//	3. 이소미 회원에게 포인트 15점 추가
	private static void updatePointSomi() {
		
		try {
			//수정할 회원 및 포인트 변수 선언
			int intPoint = 15;
			String strName = "이소미";
			
			String sql = "update point p";
				   sql += " set p.points = p.points+?";
				   sql += " where p.user_id = (select m.user_id";
				   sql += " 			      from member m where m.name = ?)";
				   

		//PreparedStatement
		pstmt = con.prepareStatement(sql);
		pstmt.setInt(1,intPoint);
		pstmt.setString(2,strName);
		
		// 쿼리 실행
		// 처리된 결과 반환됨(수정된 행수)
		int resultRows = pstmt.executeUpdate();
		
		if(resultRows > 0) {
			System.out.println("3.수정 성공");
		}else {
			System.out.println("3.수정 실패");
		}
				   
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
		System.out.println();
	}//end updatePointSomi()
	
//	2. 회원들과 보유 포인터 정보 조회
	private static void getMeberAndPoint() {
		try {
			//SQL 쿼리문 만들기
			String sql = " select m.user_id, m.name, m.pwd, m.email, m.phone,";
					sql += " decode(m.admin, 0, '일반사용자', 1, '관리자') admin,";
					sql += " p.point_id, p.points, to_char(reg_date,'yyyy-mm-dd') reg_date";
					sql += " from member m left outer join point p on m.user_id = p.user_id";
			
			//PreparedStatement  객체 얻기
			pstmt = con.prepareStatement(sql);
			System.out.println("2. 정보 조회 객체 생성 성공");
			
			//pstmt 객체의 executeQuery() 메소드를 통해서 쿼리 실행
			// 데이터 베이스에서 조회된 결과가 ResultSet 객체에 담겨옴
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				System.out.println(rs.getString("user_id")+"\t"+
								   rs.getString("name")+"\t"+
								   rs.getString("pwd")+"\t"+
								   rs.getString("email")+"\t"+
								   rs.getString("phone")+"\t"+
								   rs.getString("admin")+"\t"+
								   rs.getInt("point_id")+"\t"+
								   rs.getInt("points")+"\t"+
								   rs.getString("reg_date")
						);
			}
			
		}catch(SQLException e) {
			System.out.println("EQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
	}//end getMeberAndPoint()

	

	//드라이버 로딩과 커넥션 객체 생성 메소드
	private static void connectDB() {
		try {
			//1. 드라이버 로딩
			Class.forName(DRIVER_NAME);
			System.out.println("1-1.드라이버로드 성공");
			
			//2.데이터베이스 커넥션(연결)
			con = DriverManager.getConnection(DB_URL,oracleId,oraclePwd);
			System.out.println("1-2.커넥션 객체 생성 성공\n");
			
		}catch(ClassNotFoundException e) {
			System.out.println("드라이버 로드 ERR : "+e.getMessage());
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(pstmt, rs);
		}
		
	}//end connectDB

	private static void closeResource() {
		try {
		
			if(con != null) {
				con.close();
			}
		}catch(SQLException e) {
			System.out.println("자원해제 ERR : "+e.getMessage());
		}
	
	}//end closeResource
	
	private static void closeResource(PreparedStatement pstmt2, ResultSet rs2) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(pstmt != null) {
				pstmt.close();
			}
			
		}catch(SQLException e) {
			System.out.println("자원해제 ERR : "+e.getMessage());
		}
	}//end closeResource
	
}//end class 
