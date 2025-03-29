package com.hapla.admin.model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.hapla.admin.model.mapper.AdminMapper;
import com.hapla.admin.model.vo.AdminUsers;
import com.hapla.admin.model.vo.DailyStats;
import com.hapla.admin.model.vo.DashBoard;
import com.hapla.admin.model.vo.Notice;
import com.hapla.admin.model.vo.Report;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;
import com.hapla.common.PageInfo;
import com.hapla.review.model.vo.Review;
import com.hapla.users.model.vo.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class AdminService {
	
	private final AdminMapper mapper;

	public int userListCount(String keyword) {
		return mapper.userListCount(keyword);
	}

	public ArrayList<AdminUsers> selectUserList(PageInfo pi, String keyword, String order) {
		int offset = (pi.getCurrentPage() -1 ) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		
		HashMap<String, Object> list = new HashMap<String, Object>();
		list.put("keyword", keyword);
		list.put("order", order);
		
		return mapper.selectUserList(list, rowBounds);
	}

	public int totalUsersCount() {
		return mapper.totalUsersCount();
	}

	public int totalComm() {
		return mapper.totalComm();
	}

	public int totalReplyCount() {
		return mapper.totalReplyCount();
	}

	public int totalReview() {
		return mapper.totalReview();
	}

	public int commListCount(String nickname) {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("nickname", nickname);
		return mapper.commListCount(map);
	}

	public int replyListCount(String nickname) {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("nickname", nickname);
		return mapper.replyListCount(map);
	}

	public int reviewListCount(String nickname) {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("nickname", nickname);
		return mapper.reviewListCount(map);
	}

	public ArrayList<Comm> selectComm(PageInfo pi, String nickname, String order) {
		int offset = (pi.getCurrentPage() -1 ) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("nickname", nickname);
		map.put("order", order);
		return mapper.selectComm(map,rowBounds);
	}

	public ArrayList<Reply> selectReply(PageInfo pi, String nickname, String order) {
		int offset = (pi.getCurrentPage() -1 ) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("nickname", nickname);
		map.put("order", order);
		return mapper.selectReply(map,rowBounds);
	}

	public ArrayList<Review> selectReview(PageInfo pi, String nickname, String order) {
		int offset = (pi.getCurrentPage() -1 ) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("nickname", nickname);
		map.put("order", order);
		return mapper.selectReview(map,rowBounds);
	}

	public List<String> headers(String type) {
		switch(type) {
		case "comm" :
			return Arrays.asList("작성자", "제목", "작성일", "조회수", "관리");
		case "review" :
			return Arrays.asList("작성자","제목","작성일","별점","관리");
		case "reply" :
			return Arrays.asList("작성자","게시글 제목","작성일", "댓글 내용", "관리");
		default :
			return Arrays.asList();
		}
	}

	public boolean deleteComm(int commNo) {
		return mapper.deleteComm(commNo) >0;
	}

	public boolean deleteReview(int reviewNo) {
		return mapper.deleteReview(reviewNo) > 0;
	}
	public boolean deleteReply(int replyNo) {
		return mapper.deleteReply(replyNo) >0;
	}

	public int totalWait() {
		return mapper.totalWait();
	}

	public int totalAccept() {
		return mapper.totalAccept();
	}

	public int totalReject() {
		return mapper.totalReject();
	}

	public int reportListCount() {
		return mapper.reportListCount();
	}

	public int noticeCount() {
		return mapper.noticeCount();
	}

	public ArrayList<Notice> selectNoticeList(PageInfo pi) {
		int offset = (pi.getCurrentPage() -1 ) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
		
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("Row", offset + pi.getBoardLimit());
		return mapper.selectNoticeList(map,rowBounds);
	}

	public int insertNotice(Notice notice) {
		return mapper.insertNotice(notice);
	}
	// 공지사항 상세 조회, 조회수 증가
	public Notice selectNotice(int noticeNo) {
		Notice notice = mapper.selectNotice(noticeNo);
		if(notice != null) {
			mapper.updateViews(noticeNo);
		}
		return notice;
	}

	public boolean updateNotice(Notice notice) {
		return mapper.updateNotice(notice) >0;
	}

	public boolean deleteNotice(int noticeNo) {
		return mapper.deleteNotice(noticeNo) > 0;
	}

	public ArrayList<Report> selectReportList(PageInfo pi, String keyword, String order) {
		int offset = (pi.getCurrentPage() -1) * pi.getBoardLimit();
		RowBounds rowBounds = new RowBounds(offset,pi.getBoardLimit());
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("keyword", keyword);
		map.put("order", order);
		return mapper.selectReportList(map,rowBounds);
	}


	public int updateMember(Users user) {
		return mapper.updateMember(user);
	}

	// 최신 공지사항 3개 조회
	public ArrayList<Notice> selectRecent() {
	    return mapper.selectRecent();
	}
	
	public Report getReportDetail(int reportNo) {
	    try {
	        // 특정 신고 상세 정보 조회
	        HashMap<String,Object> map = new HashMap<String,Object>();
	        map.put("reportNo", reportNo);
	        ArrayList<Report> list = mapper.selectReportList(map, new RowBounds(0, 1));
	        return list != null && !list.isEmpty() ? list.get(0) : null;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	public int getCommNo(int reportNo) {
	    try {
	        return mapper.getCommNo(reportNo);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
	
	public int getReplyNo(int reportNo) {
	    try {
	        return mapper.getReplyNo(reportNo);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}

	public void deleteCommReport(int commNo) {
	    try {
	        mapper.deleteCommReport(commNo);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public void deleteReplyReport(int replyNo) {
	    try {
	        mapper.deleteReplyReport(replyNo);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public int updateStatus(Report report) {
	    try {
	        return mapper.updateStatus(report);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return 0;
	    }
	}
	//댓글이 속한 게시글 번호 조회
	public int getCommNoByReplyNo(int replyNo) {
		return mapper.getCommNoByReplyNo(replyNo);
	}

	public ArrayList<DashBoard> userCount() {
		return mapper.userCount();
	}

	public ArrayList<DashBoard> dailyUserCount() {
		return mapper.dailyUserCount();
	}
}
