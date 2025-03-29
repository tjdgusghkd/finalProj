package com.hapla.admin.model.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;


import com.hapla.admin.model.vo.AdminUsers;
import com.hapla.admin.model.vo.DailyStats;
import com.hapla.admin.model.vo.DashBoard;
import com.hapla.admin.model.vo.Notice;
import com.hapla.admin.model.vo.Report;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;
import com.hapla.review.model.vo.Review;
import com.hapla.users.model.vo.Users;

@Mapper
public interface AdminMapper {

	int userListCount(String keyword);

	ArrayList<AdminUsers> selectUserList(HashMap<String, Object> list, RowBounds rowBounds);

	int totalUsersCount();

	int totalComm();

	int totalReplyCount();

	int totalReview();

	int commListCount(HashMap<String, Object> map);

	int replyListCount(HashMap<String, Object> map);

	int reviewListCount(HashMap<String, Object> map);

	ArrayList<Comm> selectComm(HashMap<String, Object> map, RowBounds rowBounds);

	ArrayList<Reply> selectReply(HashMap<String, Object> map, RowBounds rowBounds);

	ArrayList<Review> selectReview(HashMap<String, Object> map, RowBounds rowBounds);

	int deleteComm(int commNo);

	int deleteReview(int reviewNo);

	int deleteReply(int replyNo);

	int totalWait();

	int totalAccept();

	int totalReject();

	int reportListCount();

	int noticeCount();

	ArrayList<Notice> selectNoticeList(HashMap<String, Object> map, RowBounds rowBounds);

	int insertNotice(Notice notice);

	Notice selectNotice(int noticeNo);

	int updateViews(int noticeNo);

	int updateNotice(Notice notice);

	int deleteNotice(int noticeNo);

	ArrayList<Report> selectReportList(HashMap<String, Object> map, RowBounds rowBounds);

	int updateStatus(Report report);

	int getCommNo(int reportNo);

	void deleteCommReport(int commNo);

	int updateMember(Users user);
	
	
	// 최신 공지사항 3개 조회
	ArrayList<Notice> selectRecent();

	int getReplyNo(int reportNo);

	void deleteReplyReport(int replyNo);

	int getCommNoByReplyNo(int replyNo);

	ArrayList<DashBoard> userCount();

	ArrayList<DashBoard> dailyUserCount();

}
