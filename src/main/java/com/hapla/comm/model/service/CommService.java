package com.hapla.comm.model.service;

import java.util.ArrayList;
import java.util.Objects;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import com.hapla.comm.model.mapper.CommMapper;
import com.hapla.comm.model.vo.Comm;
import com.hapla.comm.model.vo.Reply;
import com.hapla.common.PageInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommService {
    private final CommMapper mapper;

	public int getListCount(int i, int category) {
		return mapper.getListCount(i, category);
	}

//	public ArrayList<Comm> selectCommList(PageInfo pi, int i, String search) {
//		int offset = (pi.getCurrentPage() -1) * pi.getBoardLimit();
//		RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());
//		return mapper.selectCommList(i, rowBounds);
//	}
	
	public ArrayList<Comm> selectCommList(PageInfo pi, int i, String search, int category) {
	    int offset = (pi.getCurrentPage() - 1) * pi.getBoardLimit();
	    RowBounds rowBounds = new RowBounds(offset, pi.getBoardLimit());

	    // 검색어가 없으면 기존 리스트 조회
	    if (search == null || search.trim().isEmpty()) {
	        return mapper.selectCommList(category, rowBounds);
	    } else {
	        return mapper.searchCommList(category, search, rowBounds);
	    }
	}

	public int insertComm(Comm c) {
		return mapper.insertComm(c);
	}

	public Comm selectComm(int commNo, Integer userNo) {
	    Comm c = mapper.selectComm(commNo);
	    if (c != null) {
	        // 현재 사용자가 본인이 작성한 게시글이 아닌 경우에만 조회수 증가 처리
	        if (userNo == null || !Objects.equals(c.getUserNo(), userNo)) {
	            int result = mapper.updateCount(commNo);
	            if (result > 0) {
	                c.setViews(c.getViews() + 1);
	            }
	        }
	    }
	    return c;
	}

	public ArrayList<Reply> selectReplyList(int commNo) {
		return mapper.selectReplyList(commNo);
	}

	public int insertReply(Reply r) {
		return mapper.insertReply(r);
	}

	public int deleteComm(int commNo) {
		return mapper.deleteComm(commNo);
	}

	public int updateComm(Comm c) {
		return mapper.updateComm(c);
	}

	public int deleteReply(int replyNo) {
		return mapper.deleteReply(replyNo);
	}

	public int updateReply(Reply r) {
		return mapper.updateReply(r);
	}

	public int checkUserLike(int userNo, int commNo) {
		return mapper.checkUserLike(userNo, commNo);
	}

	public void addLike(int userNo, int commNo) {
        mapper.insertLike(userNo, commNo);
    }

    public void removeLike(int userNo, int commNo) {
        mapper.deleteLike(userNo, commNo);
    }

    public int getLikeCount(int commNo) {
        return mapper.countLikes(commNo);
    }

	public int getSearchListCount(String search, int category) {
		return mapper.getSearchListCount(search, category);
	}
	
	public Reply selectReply(int replyNo) {
	    return mapper.selectReply(replyNo);
	}
}
